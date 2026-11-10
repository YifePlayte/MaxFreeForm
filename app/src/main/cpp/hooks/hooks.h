//
// MaxFreeForm native hook: feature hook declarations.
//
// Every feature owns one source file under hooks/ and exposes a single
// installer that receives the mapped Dart AOT image.  The native entry
// (maxfreeform_native.cpp) calls each installer after libapp.so has been mapped.
//
// Installers must be idempotent and fail closed: when the expected pattern is
// missing or ambiguous the original launcher behavior is preserved.

#pragma once

#include <nhk/dart_image.h>

namespace maxfreeform {
namespace dart = nhk::dart;
}

namespace maxfreeform::hooks {

// Make the launcher's built-in small-window and new-task shortcut menu entries
// valid for every app, matching the legacy AddFreeformShortcut Java hook.
//
// HyperOS 4's launcher already contains the two menu item classes; they are
// hidden unless the package-specific eligibility checks pass.  Bypassing the
// two isValid checks exposes the existing native items without constructing
// Dart objects from native code.
bool InstallAddFreeformShortcutHook(const dart::Image& image);

// Use the add-to-workspace artwork at full alpha for the multiple-window icon,
// without changing theme tint or disabled states. Shares add_freeform_shortcut.
bool InstallFreeformShortcutIconHook(const dart::Image& image);

}  // namespace maxfreeform::hooks
