// Show the add-to-workspace artwork on the multiple-window shortcut entry.
//
// The launcher decodes menu assets with its bundled Skia/libwebp, so the only
// input it sees is the encoded WebP. Identify the stock multiple-window asset by
// its length and FNV-1a digest, then hand libwebp the add-to-workspace artwork
// instead. Theme tint, scaling and disabled-state rendering stay untouched.

#include "hooks.h"
#include "freeform_shortcut_icon_data.h"
#include "../config/native_config.h"
#include "../log.h"

#include <nhk/madvise_guard.h>
#include <nhk/native_api.h>
#include <lsplt.hpp>

#include <pthread.h>
#include <string.h>
#include <sys/mman.h>

namespace maxfreeform::hooks {
namespace {

// LLVM promotes libwebp's WebPData and drops the constant partial/version
// arguments, so this private WebPDemuxInternal entry takes (bytes, size, state).
using DemuxFn = void* (*)(const void* bytes, size_t length, int* state);

// Verified unique in the bundled and shared runtimes of 7722, 7726, 7748,
// 7751, 8565, 8583 and 8602. No per-build address is used.
constexpr uint32_t kEntryWords[] = {
    0xaa0003e8u, 0xaa1f03e0u, 0x12800009u, 0xb9000049u,
    0xb4000b88u, 0xb4000b61u, 0xd10183ffu, 0xa9035ffeu,
    0xa90457f6u, 0xa9054ff4u, 0xf100503fu, 0x54000062u,
    0x2a1f03e8u, 0x1400004du, 0xb9400109u, 0x52892a4au,
    0xaa0203f5u, 0x72a8c8cau, 0x6b0a013fu, 0x54000141u,
    0xb9400909u, 0x5288aaeau, 0x72aa084au, 0x6b0a013fu,
    0x540000a1u, 0xb9400516u, 0x110026c9u, 0x7100453fu,
    0x540008a2u,
};
// Known "not a WebP" return block, 0x168 bytes into the same function.
constexpr uintptr_t kNullReturnOffset = 0x168u;
constexpr uint32_t kNullReturnWords[] = {
    0xaa1f03e0u, 0xb9000048u, 0xa9454ff4u, 0xa94457f6u,
    0xa9435ffeu, 0x910183ffu, 0xd65f03c0u,
};
constexpr size_t kHookSize = 32u;
constexpr char kRuntimeName[] = "libhyper_os_flutter.so";

void* g_original = nullptr;
uint32_t g_rejected_logged = 0u;

// Return the single executable location that carries the verified entry. The
// launcher maps the Flutter runtime either from the system image or from its
// APK, so key on the library name, not one fixed path.
uintptr_t FindEntry() {
    const auto maps = lsplt::MapInfo::Scan();
    uintptr_t found = 0u;
    uint32_t count = 0u;
    for (const auto& map : maps) {
        if ((map.perms & (PROT_READ | PROT_EXEC)) !=
                (PROT_READ | PROT_EXEC)) continue;
        if (strstr(map.path.c_str(), kRuntimeName) == nullptr) continue;
        for (uintptr_t at = map.start;
             at + sizeof(kEntryWords) <= map.end; at += sizeof(uint32_t)) {
            if (memcmp(reinterpret_cast<const void*>(at), kEntryWords,
                       sizeof(kEntryWords)) != 0) continue;
            const uintptr_t tail = at + kNullReturnOffset;
            if (tail + sizeof(kNullReturnWords) > map.end ||
                    memcmp(reinterpret_cast<const void*>(tail),
                           kNullReturnWords, sizeof(kNullReturnWords)) != 0) {
                return 0u;
            }
            found = at;
            ++count;
        }
    }
    if (count != 1u) {
        MAXFREEFORM_LOGE("multiple_window icon: %u WebP demux entries, need 1",
                         count);
        return 0u;
    }
    return found;
}

void* CreateDemux(const void* buffer, size_t length, int* state) {
    const auto original = reinterpret_cast<DemuxFn>(
            __atomic_load_n(&g_original, __ATOMIC_ACQUIRE));
    if (original == nullptr) {
        if (state != nullptr) *state = -1;
        return nullptr;
    }
    if (state == nullptr ||
            !config::Enabled(config::kAddFreeformShortcut) ||
            !shortcut_icon::MatchesOriginal(buffer, length)) {
        return original(buffer, length, state);
    }
    // libwebp keeps a pointer to these bytes. The arrays are immutable and live
    // for the whole process, so cached/asynchronous frame decoding stays valid.
    void* result = original(shortcut_icon::kOpaqueWebp,
                            sizeof(shortcut_icon::kOpaqueWebp), state);
    if (result != nullptr) {
        MAXFREEFORM_LOGW("multiple_window icon uses add_workspace artwork");
        return result;
    }
    if (__atomic_exchange_n(&g_rejected_logged, 1u, __ATOMIC_ACQ_REL) == 0u) {
        MAXFREEFORM_LOGW("multiple_window icon replacement rejected (state=%d)",
                         *state);
    }
    // A failed demux owns nothing, so fall back to the original asset.
    return original(buffer, length, state);
}

}  // namespace

bool InstallFreeformShortcutIconHook(const dart::Image&) {
    if (!config::Enabled(config::kAddFreeformShortcut)) return false;
    if (__atomic_load_n(&g_original, __ATOMIC_ACQUIRE) != nullptr) {
        return true;
    }
    static pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;
    pthread_mutex_lock(&lock);
    bool ok = __atomic_load_n(&g_original, __ATOMIC_ACQUIRE) != nullptr;
    const uintptr_t entry = ok ? 0u : FindEntry();
    if (!ok && entry != 0u &&
            nhk::ProtectHookRange(entry, kHookSize) &&
            nhk::InstallInlineHook(reinterpret_cast<void*>(entry),
                                   reinterpret_cast<void*>(&CreateDemux),
                                   &g_original) == nhk::kHookSuccess &&
            __atomic_load_n(&g_original, __ATOMIC_ACQUIRE) != nullptr) {
        MAXFREEFORM_LOGW("multiple_window icon: WebP demux hooked at %p",
                         reinterpret_cast<void*>(entry));
        ok = true;
    }
    pthread_mutex_unlock(&lock);
    if (!ok && entry == 0u) {
        MAXFREEFORM_LOGE("multiple_window icon: hook not installed");
    }
    return ok;
}

}  // namespace maxfreeform::hooks
