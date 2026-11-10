// MaxFreeForm native hook: LSPosed native entry for HyperOS 4 MiuiHome.
//
// This file is the single native_init entry point declared in
// META-INF/xposed/native_init.list.  It owns only the generic lifecycle:
//
//   * accepting the HYOS process family (spawner + launcher child)
//   * resolving the mapped Dart AOT image (libapp.so)
//   * dispatching the registered feature hook once per process
//
// The launcher has no ART runtime, so Java hooks never load there.  The feature
// hook patches the launcher's native Dart AOT image instead.
//
// The HYOS entry is initialized in the root spawner (cmdline `usap64`) before
// the package process is forked, so both the spawner and the actual launcher
// process are accepted here.  The process-family identity checks follow the
// approach established by the WOMMO and MiuiBackGestureHook research projects.

#include "hooks/hooks.h"
#include "config/native_config.h"
#include "log.h"

#include <nhk/log.h>
#include <nhk/module.h>
#include <nhk/process.h>
#include <nhk/text.h>

#include <dlfcn.h>
#include <errno.h>
#include <fcntl.h>
#include <inttypes.h>
#include <pthread.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

namespace {

constexpr char kLauncherProcessName[] = "com.miui.home";
constexpr char kSpawnerPath[] = "/system_ext/bin/hyos_spawner";
constexpr char kDartLibraryName[] = "libapp.so";

// Only these mapped paths are accepted as the launcher's Dart AOT image.
// Launcher libraries are mapped straight out of the APK, so dladdr reports the
// in-archive path.  Everything else fails closed before any patch is written.
constexpr char kDataAppPrefix[] = "/data/app/";
constexpr char kDataDartLibraryTail[] = "/base.apk!/lib/arm64-v8a/libapp.so";
constexpr char kProductHomePrefix[] = "/product/priv-app/MiuiHome/";
constexpr char kSystemExtHomePrefix[] = "/system_ext/priv-app/MiuiHome/";
constexpr char kSystemDartLibraryTail[] =
        "/MiuiHome.apk!/lib/arm64-v8a/libapp.so";

bool IsLauncherProcess() {
    return nhk::process::SelfCommandLineIs(kLauncherProcessName);
}

// LSPosed's HYOS native entry is initialized in the root spawner process
// (cmdline `usap64`) before it forks the launcher child.  The executable
// identity is the hard process-family boundary.
bool IsHyosSpawnerProcessFamily() {
    return nhk::process::SelfExecutableIs(kSpawnerPath);
}

// The feature hook installed once per launcher process.  A hook with a non-zero
// feature bit is installed only while the module's switch is on; the switch
// value comes from system_server (native_config.h).
using DartHookInstaller = bool (*)(const maxfreeform::dart::Image& image);

struct DartHook {
    const char* name;
    uint32_t feature;
    DartHookInstaller install;
};

constexpr DartHook kDartHooks[] = {
    {"add_freeform_shortcut", maxfreeform::config::kAddFreeformShortcut,
     &maxfreeform::hooks::InstallAddFreeformShortcutHook},
    {"add_freeform_shortcut_icon", maxfreeform::config::kAddFreeformShortcut,
     &maxfreeform::hooks::InstallFreeformShortcutIconHook},
};

bool AcceptLauncherDartPath(const char* path) {
    if (path == nullptr) return false;
    if (nhk::text::StartsWith(path, kDataAppPrefix) &&
            nhk::text::EndsWith(path, kDataDartLibraryTail)) {
        return true;
    }
    if (!nhk::text::EndsWith(path, kSystemDartLibraryTail)) return false;
    return nhk::text::StartsWith(path, kProductHomePrefix) ||
            nhk::text::StartsWith(path, kSystemExtHomePrefix);
}

uint32_t g_image_verified_state = 0u;

// The verified launcher image handle and the pending-gate state used to retry
// the gated hook once the configuration endpoint answers.  The loader callback
// writes them, the retry worker reads them, so every access is atomic.
void* g_dart_handle = nullptr;
bool g_dart_retry_pending = false;
bool g_dart_retry_done = false;
// A gated hook invoked repeatedly reports its switch state once per process.
uint32_t g_skip_logged = 0u;
// Per-feature entry logs, matching the Java BaseHook "Inited hook" /
// "Failed init hook" lines.  Success and failure are tracked separately so a
// later retry can still report a success after an initial failure.
uint32_t g_hook_success_logged = 0u;
uint32_t g_hook_failure_logged = 0u;

void RetryPendingGatedHooks();


void LogSwitchOff(const char* name, uint32_t feature) {
    if (feature == 0u) return;
    if ((__atomic_fetch_or(&g_skip_logged, feature, __ATOMIC_ACQ_REL) &
                feature) != 0u) {
        return;
    }
    MAXFREEFORM_LOGW("hook '%s' skipped: switch off (flags=0x%" PRIx32
                     " ready=%u)",
                     name, maxfreeform::config::Flags(),
                     maxfreeform::config::Ready() ? 1u : 0u);
}

void LogDartHookResult(size_t index, const char* name, bool ok) {
    if (index >= 32u) return;
    const uint32_t bit = 1u << index;
    uint32_t* logged = ok ? &g_hook_success_logged : &g_hook_failure_logged;
    if ((__atomic_fetch_or(logged, bit, __ATOMIC_ACQ_REL) & bit) != 0u) return;
    if (ok) {
        MAXFREEFORM_LOGW("Inited hook: %s", name);
    } else {
        MAXFREEFORM_LOGE("Failed init hook: %s", name);
    }
}

// The service manager caches a failed lookup for a few seconds, so a retry from
// the next library callback can still hit the cached miss.  This bounded worker
// is the only thread the module creates: it spaces the retries out, then exits.
constexpr uint32_t kConfigRetryAttempts = 5u;
constexpr long kConfigRetryDelaySeconds = 2;
uint32_t g_config_retry_started = 0u;

void* ConfigRetryWorker(void*) {
    pthread_setname_np(pthread_self(), "mff-config");
    for (uint32_t attempt = 0u; attempt < kConfigRetryAttempts; ++attempt) {
        timespec delay{kConfigRetryDelaySeconds, 0};
        while (nanosleep(&delay, &delay) != 0 && errno == EINTR) {
        }
        if (!maxfreeform::config::Refresh()) continue;
        MAXFREEFORM_LOGW("native config: endpoint answered on retry %u",
                         attempt + 1u);
        RetryPendingGatedHooks();
        return nullptr;
    }
    MAXFREEFORM_LOGW("native config: endpoint silent after %u retries",
                     kConfigRetryAttempts);
    return nullptr;
}

void StartConfigRetryWorker() {
    if (__atomic_exchange_n(&g_config_retry_started, 1u, __ATOMIC_ACQ_REL) !=
            0u) {
        return;
    }
    pthread_t thread{};
    if (pthread_create(&thread, nullptr, ConfigRetryWorker, nullptr) == 0) {
        pthread_detach(thread);
        return;
    }
    __atomic_store_n(&g_config_retry_started, 0u, __ATOMIC_RELEASE);
    MAXFREEFORM_LOGE("native config: retry worker unavailable");
}

bool RunDartHooks(void* handle) {
    if (handle != nullptr) {
        __atomic_store_n(&g_dart_handle, handle, __ATOMIC_RELEASE);
    }
    maxfreeform::dart::Image image{};
    if (!maxfreeform::dart::AcquireVerifiedDartImage(
                handle, &AcceptLauncherDartPath, &image)) {
        return false;
    }
    if (__atomic_exchange_n(&g_image_verified_state, 1u, __ATOMIC_ACQ_REL) ==
            0u) {
        MAXFREEFORM_LOGW("launcher dart image verified");
    }

    // One bounded query per process: the launcher has no ART runtime, so the
    // switch value can only come from the module's system_server endpoint.
    const bool config_ready = maxfreeform::config::EnsureReady();

    bool all_ok = true;
    bool gated_skipped = false;
    for (size_t index = 0u; index < sizeof(kDartHooks) / sizeof(kDartHooks[0]);
            ++index) {
        const DartHook& hook = kDartHooks[index];
        if (hook.feature != 0u &&
                !maxfreeform::config::Enabled(hook.feature)) {
            LogSwitchOff(hook.name, hook.feature);
            gated_skipped = true;
            continue;
        }
        const bool installed = hook.install(image);
        LogDartHookResult(index, hook.name, installed);
        if (!installed) all_ok = false;
    }
    // A skipped hook only means "off" when the endpoint answered.  Otherwise the
    // next mapped library retries with the stored image.
    if (gated_skipped && !config_ready) {
        __atomic_store_n(&g_dart_retry_done, false, __ATOMIC_RELAXED);
        __atomic_store_n(&g_dart_retry_pending, true, __ATOMIC_RELEASE);
        StartConfigRetryWorker();
    }
    return all_ok;
}

// The endpoint lives in system_server and is installed by the module's system
// hook; a launcher that starts before it answers must not lose its gated
// feature for the whole process lifetime.  The bounded worker retries on a
// timer, and every later library load is a free extra chance.
void RetryPendingDartHooks() {
    if (!__atomic_load_n(&g_dart_retry_pending, __ATOMIC_ACQUIRE) ||
            __atomic_load_n(&g_dart_retry_done, __ATOMIC_ACQUIRE)) {
        return;
    }
    if (!maxfreeform::config::EnsureReady()) return;
    __atomic_store_n(&g_dart_retry_done, true, __ATOMIC_RELEASE);
    MAXFREEFORM_LOGW("native config: endpoint answered late, retrying gated hook");
    RunDartHooks(__atomic_load_n(&g_dart_handle, __ATOMIC_ACQUIRE));
}

void RetryPendingGatedHooks() { RetryPendingDartHooks(); }

bool OnNativeInit() {
    maxfreeform::config::Configure();
    const bool launcher = IsLauncherProcess();
    const bool spawner = IsHyosSpawnerProcessFamily();
    MAXFREEFORM_LOGW("native_init: launcher=%u spawner=%u",
                     launcher ? 1u : 0u, spawner ? 1u : 0u);
    if (!launcher && !spawner) {
        MAXFREEFORM_LOGW("rejected non-hooked HYOS process");
        return false;
    }
    if (launcher) {
        void* dart_handle = dlopen(kDartLibraryName, RTLD_NOLOAD);
        if (dart_handle != nullptr) {
            RunDartHooks(dart_handle);
        }
    }
    return true;
}

// Called by LSPosed whenever a new library is loaded into the process.
void OnLibraryLoaded(const char* name, void* handle) {
    if (name == nullptr || handle == nullptr) return;
    if (!IsLauncherProcess()) return;

    if (nhk::text::EndsWith(name, kDartLibraryName)) {
        MAXFREEFORM_LOGW("libapp.so loaded, installing native hooks");
        RunDartHooks(handle);
        return;
    }
    // Any later library is a free retry point for a hook held back by an
    // endpoint that had not answered yet.
    RetryPendingGatedHooks();
}

}  // namespace

namespace nhk {

const ModuleDescriptor& GetModuleDescriptor() {
    static const ModuleDescriptor kDescriptor = MakeDescriptor(
            "MaxFreeFormNative", OnNativeInit, OnLibraryLoaded);
    return kDescriptor;
}

}  // namespace nhk

extern "C" {

__attribute__((constructor)) void MaxFreeFormNativeLoaded() {
    MAXFREEFORM_LOGW("native library loaded");
}

}  // extern "C"

