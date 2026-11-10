# AGENTS.md

## Project Context

MaxFreeForm is a personal LSPosed module (API 102) for HyperOS. It has two hook
families:

- Java/Kotlin hooks under `app/src/main/java/com/yifeplayte/maxfreeform/hook/`
  for ordinary Android processes with dex code, built on ezhooktool.
- Native hooks under `app/src/main/cpp/` for the HyperOS 4 Flutter launcher
  (`com.miui.home`). The Android 17 launcher is a Dart AOT application
  (`libapp.so`) with no ART runtime, so Java hooks never load in it.

When a feature exists on both launcher generations, both implementations must
use the same preference key. The Java hook covers the legacy launcher; the
native hook covers the Flutter launcher; the settings UI switch stays shared.

## Repository Layout

```text
app/src/main/java/com/yifeplayte/maxfreeform/
  App.kt                                   XposedService binding, UI process
  hook/MainHook.kt                         the only XposedModule; lifecycle
  hook/hooks/Base*.kt                      hook base classes
  hook/hooks/singlepackage/                one group object per host package
  hook/hooks/singlepackage/<group>/        BaseHook implementations (lowercase)
  hook/hooks/multipackage/*.kt             BaseMultiHook implementations
  hook/hooks/singlepackage/android/        system_server hooks
  hook/utils/                              Log, preferences, host helpers
  activity/pages/*.kt                      settings pages
  activity/sections/*.kt                   settings sections; keys match hooks
  activity/dialogs/*.kt                    restart/reset dialogs
  activity/ui/*.kt                         Miuix composables
  utils/                                   Build, ClassScanner, prefs, hot reload
app/src/main/cpp/
  maxfreeform_native.cpp                   module descriptor + hook callbacks
  config/native_config.h                   feature bits + switch transport config
  log.h                                    MAXFREEFORM_LOGW / LOGE only
  hooks/hooks.h                            native feature declarations
  hooks/*_hook.cpp                         one file per native feature
native-hook-kit/                           shared source-level native scaffold
app/src/main/resources/META-INF/xposed/    module.prop, java_init.list,
                                           native_init.list, scope.list
```

## Java/Dex Hook System

### Entry and lifecycle

- `META-INF/xposed/java_init.list` declares
  `com.yifeplayte.maxfreeform.hook.MainHook` as the entry.
- `MainHook` follows the libxposed API 102 lifecycle:
  - `onModuleLoaded`: set `EzReflect.logger = Log`, call
    `EzXposed.initOnModuleLoaded`, then install hooks on target ready.
  - `onPackageLoaded` / `onPackageReady`: initialize `EzXposed` for the host
    package.
  - `onSystemServerStarting`: system_server support.
  - `onHotReloading` / `onHotReloaded`: delegate to ezhooktool and reinstall
    hooks after a reload so fresh preference values are read.
- `installHooks()` iterates the single/multi/sub-package registries and calls
  `init()` on each. `PACKAGE_NAME_HOOKED` is the union of all registry package
  names and drives scope/restart behavior.
- Settings changes call `reloadAllTargets()`; the UI also offers per-scope
  restarts. The native launcher is a separate process and may need
  `am force-stop com.miui.home` or a scope restart to pick up a newly enabled
  native feature.

### Hook kinds

| Base | Scope | Pick when |
| --- | --- | --- |
| `BaseHook` | one package | normal single-process feature |
| `BasePackage` | one package | group object owning `BaseHook`s |
| `BaseMultiHook` | several packages | same logic installed per package |
| `BaseSubHook` | one sub-package | feature lives in a plugin ClassLoader |
| `BaseSubPackage` | one sub-package | group object for a plugin |

- Every hook has a snake_case `key`. `isEnabled` reads the remote preference
  `config` (`XSharedPreferences.getBoolean(key, false)`).
- `init()` is single-shot and wraps `hook()` in `runCatching`; a failed hook
  must log and leave the host running.
- `BasePackage` scans
  `javaClass.packageName + "." + javaClass.simpleName.lowercase()`.
  Example: `singlepackage/Home.kt` (`BasePackage("com.miui.home")`) owns
  `singlepackage/home/`. `system_server` uses `BasePackage("system")`, which
  scans `singlepackage/android/`.
- `ClassScanner.scanObjectOf<T>` reflects the module's own dex and returns
  singleton objects directly in the requested package; inner classes are
  skipped. A new group object only needs to exist; `MainHook` discovers it.

### Adding a Java hook

1. Choose the group/package; reuse an existing group when possible.
2. Add `object <Feature> : BaseHook()` with the desired `key` and `hook()`.
3. Use ezhooktool: `loadClass`, `findMethod`, `createHook`,
   `before`/`after`, `param.result`, `callMethod`, `getFieldOrNull`, etc.
4. Add an `SPSwitch` in the matching section with the same key and add strings
   in `res/values` and `res/values-zh`.
5. Reuse an existing preference key when reimplementing a feature on a new
   launcher generation.

## Native Hook System (Flutter MiuiHome)

### Entry and lifecycle

- `META-INF/xposed/native_init.list` declares
  `libmaxfreeform_native.so`; it is the only native entry.
- LSPosed initializes the HYOS native entry in the root spawner
  (`/system_ext/bin/hyos_spawner`) before the launcher child is forked.
  The exported `native_init` is provided by the shared `NativeHookKit`; the
  module implements `GetModuleDescriptor()` with `OnNativeInit` and
  `OnLibraryLoaded`.  Business hooks install only from the `libapp.so` load
  callback in the launcher child.
- Image verification is handle-bound and fail closed:
  1. process is exactly `com.miui.home` (or the HYOS spawner for entry only);
  2. the reported handle exports `_kDartIsolateSnapshotInstructions`;
  3. `dladdr` reports a launcher APK path
     (`/data/app/.../base.apk!/lib/arm64-v8a/libapp.so`,
     `/product/priv-app/MiuiHome/...`, or
     `/system_ext/priv-app/MiuiHome/...`);
  4. `AcquireImageAtBase` finds the same ELF by load bias.
- Do not replace this with a loose `libapp.so` name match for patching.

### Switch values (system_server endpoint)

HYOS processes have no ART runtime, so they cannot read RemotePreferences
directly. `hook/hooks/singlepackage/android/NativeConfigEndpoint.kt` runs in
system_server, hooks `android.content.IClipboard$Stub.onTransact`, claims the
private transaction `0x004D4646` ("MFF"), verifies the real
uid/pid/`/proc/<pid>/cmdline` against `com.miui.home`, and answers
`ack, available, flags, revision` with ACK `0x4D464631` ("MFF1").

- Feature bits are mirrored in `cpp/config/native_config.h`; keep the bit order
  and preference-key order identical in Kotlin and C++. The binder query itself
  is provided by the shared `NativeHookKit`; configure it with the module
  descriptor (transaction/ACK/known bits) in `OnNativeInit`.
- `add_freeform_shortcut` works on phones and Pad through two narrow gates:
  `ValidSmallWindowShortcutMenu.isValid`'s form-factor false return and
  `ValidMultipleSmallWindowShortcutMenu.isValid`'s `not SupportSmallWindow`
  false return.  Every other validator check still runs, so ineligible items
  never reach the menu.  The endpoint no longer masks the bit per form factor.
- Both validators emit one log-and-return block per rejection reason.  The gate
  scan masks the object-pool base and slot, the stack offsets and both call
  targets, groups the matching blocks into consecutive runs, and uses the
  block count to tell the validators apart: small window has 8 blocks, multiple
  window has 9 or 10.  The second block of each run is the gate.  This works
  across 8.01.02.7722, 7726, 7748, 7751, 8565 and 8583 with no per-build
  offsets.
- Fail closed: a gated native hook installs only after an authoritative
  `available=1` answer containing its bit. An unreachable endpoint leaves the
  hook off and starts the bounded retry worker.
- The endpoint is infrastructure, not a user feature: `isEnabled` is always
  true, and the preference key is not exposed as a switch.
- The transaction code must not collide with other modules. WOMMO uses "WOM";
  MaxFreeForm uses "MFF".

### Multiple-window shortcut icon

- `freeform_shortcut_icon_hook.cpp` shares the `add_freeform_shortcut` feature
  bit. It only swaps the stock multiple-window artwork for the add-to-workspace
  artwork at full alpha; menu validity, tint, scaling and disabled states are
  untouched, and the real add-to-workspace asset passes through unchanged.
- The launcher decodes menu assets with its bundled Skia/libwebp, so the hook
  rewrites the encoded WebP at that entry. The NDK `AImageDecoder` import is NOT
  this asset's path and never proves replacement.
- Target the LLVM-promoted private entry `(bytes, size, state)`, not public
  four-argument `WebPDemuxInternal`. Require a unique entry fingerprint plus its
  known null-return block; fail closed otherwise.
- Identify the stock asset by length plus a 64-bit FNV-1a digest instead of
  embedding a copy for `memcmp`. The digest only selects which bytes to hand to
  libwebp; the launcher's own decoder validates the image afterwards. Keep the
  replacement array immutable for the process lifetime. No runtime WebP
  dependency is added.
- Protect the patched entry with the shared LSPlt `madvise` guard before writing.
  The compat AAR provides the legacy `MapInfo::Scan()` ABI used to locate it.
- The same two assets are byte-identical in 7722, 7726, 7748, 7751, 8565, 8583
  and 8602, and the entry fingerprint is unique in each checked runtime.
- The swap appears on image decode in a fresh launcher process. Build success is
  not a substitute for the on-device menu check.

### Patching Dart AOT

- The shared `NativeHookKit` (`native-hook-kit`) provides image lookup, unique
  word pattern scanning, masked patterns, executable-range validation, and
  verified patches. Hooks include `<nhk/dart_image.h>` and
  `<nhk/page_guard.h>` directly; do not reintroduce private copies of the kit.
- A feature hook must:
  1. locate a unique pattern in both supported launcher versions;
  2. validate the exact instruction words and a known true/false return block;
  3. fail closed on missing/ambiguous patterns;
  4. write only after `ProtectHookRange` (or the documented shared guard) is
     established;
  5. be idempotent across repeated `libapp.so` load callbacks.
- Never force an entire `isValid`-style predicate true at entry. HyperOS 4 has
  phone and Pad menu paths with different context classes; bypassing the type
  checks inserts invalid items and crashes `ShortcutMenuContext.isValid`.
  Patch the narrow final gate/return instead, after the class/context checks.
- Cross-version verification is mandatory. The reference pair is
  `8.01.02.7726` and `8.01.02.7751`; the old APK is on the device under
  `/storage/emulated/0/Download/Nagram/`. Check pattern uniqueness and return
  offsets in both images before shipping.

### Patch persistence and module coexistence

HyperOS memory cleanup can issue `madvise(MADV_DONTNEED)`, dropping modified
copy-on-write code pages and silently reverting patches. The `madvise` guard is
the shared synchronization point between native modules, so it needs explicit
coexistence rules:

- **Do not add another inline hook on libc `madvise`.** The kit uses the
  external `lsplt-standalone` AAR and a GOT chain; each module's guard calls
  the backup it received from LSPlt. This keeps WOMMO, MaxFreeForm and
  MiuiBackGestureHook independent while sharing the same slot.
- The module APK contains `lib/arm64-v8a/liblsplt.so`; the native target needs
  `DT_RUNPATH=$ORIGIN` so LSPosed can resolve it from the APK. CMake/Ninja
  reduces a literal `$ORIGIN` to `$`, so the build passes it through
  `cpp/linker-rpath.txt`.
- The pinned compat AAR is shared by soname. All guards operate on the same
  LSPlt registry, and each module preserves its own protected pages. Keep every
  consumer on the same AAR version; diverging `liblsplt.so` copies reintroduce
  the soname/ABI collision.
- If LSPlt cannot load, the kit may fall back to its inline guard. Log which
  backend was used and do not claim the patch is durable unless the guard is
  active.
- Protect the GOT/PLT slot pages themselves as well as the patched code pages.
- Multiple modules patching different bytes on the same page is fine; the guard
  must protect the union. Two modules patching the same bytes is not
  cooperative: use unique fingerprints and abort/log instead of writing over
  another module's hook.

### Adding a native feature hook

1. Create `cpp/hooks/<feature>_hook.cpp` with an idempotent installer taking
   `const maxfreeform::dart::Image&`.
2. Declare it in `cpp/hooks/hooks.h`.
3. Register it in `kDartHooks[]` in `cpp/maxfreeform_native.cpp` with its
   feature bit.
4. Add the source to `MAXFREEFORM_NATIVE_SOURCES` in `cpp/CMakeLists.txt`.
5. If it has a user switch, add the bit in `native_config.h`, the preference
   key in `NativeConfigEndpoint.kt`, and the `SPSwitch` with the same key.
6. `RunDartHooks` emits one `Inited hook: <name>` or
   `Failed init hook: <name>` line per feature per process; keep the installer
   idempotent and add an "already patched" path.
7. Verify on a clean launcher process after `am force-stop com.miui.home` and
   read the actual patched words back from `/proc/<pid>/mem` when diagnosing.

## Build and Verification

```text
ANDROID_HOME=$HOME/Android/Sdk sh gradlew :app:assembleDebug
ANDROID_HOME=$HOME/Android/Sdk sh gradlew :app:assembleRelease
```

- `minSdk = 33`, `compileSdk/targetSdk = 37`, Compose + Miuix.
- Native build: NDK/CMake, `arm64-v8a`, `maxfreeform_native`, and the sibling
  `native-hook-kit` source tree. `NATIVE_HOOK_KIT_DIR` can override the path.
- The app depends on
  `org.lsposed.lsplt:lsplt-standalone:2.1.1-7d609ac-compat`, resolved from the
  `native-hook-kit/third_party/maven` submodule repository; prefab must stay
  enabled. The AAR is pinned, 16KB-aligned, and carries a legacy
  `MapInfo::Scan()` overload for the older MiuiBackGestureHook ABI. No network
  access or LSPlt checkout is required.
- Check the APK contains `lib/arm64-v8a/libmaxfreeform_native.so`,
  `lib/arm64-v8a/liblsplt.so`, `META-INF/xposed/native_init.list`,
  `java_init.list`, and the API 102 metadata.
- Check `readelf -d libmaxfreeform_native.so` reports `RUNPATH=$ORIGIN`; without
  it LSPosed cannot load `liblsplt.so` from the APK.
- Install with `adb install -r`, then `am force-stop com.miui.home` and press
  Home. Read `MaxFreeFormNative` WARN/ERROR logs; the launcher has no ART.
- Test both the current launcher and, when the pattern changes, the old 7726
  APK. Do not ship a native pattern that is not unique in both.
- For a launcher crash, read `/data/tombstones/tombstone_*` and map the
  backtrace to `libapp.so` offsets before changing another hook.
- Do not push or publish unless requested. Keep commits focused and auditable.

## Preferences and UI

- UI preferences use `config` via `SharedPreferences.mSP`; hook code reads the
  same store through `XSharedPreferences`.
- Dependent UI uses `SPSwitch(switchState = ...)` plus `AnimatedVisibility`;
  do not leave a dependent page visible while its parent switch is off.
- `Home` section keys:
  - `add_freeform_shortcut`
  - legacy `UnlockEnterSmallWindow` is non-HyperOS only.
- HyperOS 4 native hooks must not assume Java classes exist in
  `com.miui.home`; verify the process has no `libart.so` before adding a Java
  workaround.
