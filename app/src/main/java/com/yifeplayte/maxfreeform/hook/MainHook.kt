package com.yifeplayte.maxfreeform.hook

import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.LogExtensions.logexIfThrow
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.hook.hooks.android.RemoveSmallWindowRestrictions
import com.yifeplayte.maxfreeform.hook.hooks.android.UnlockForegroundPin
import com.yifeplayte.maxfreeform.hook.hooks.android.UnlockFreeformQuantityLimit
import com.yifeplayte.maxfreeform.hook.hooks.android.UnlockSideHideFreeform
import com.yifeplayte.maxfreeform.hook.hooks.home.AddFreeformShortcut
import com.yifeplayte.maxfreeform.hook.hooks.home.UnlockEnterSmallWindow
import com.yifeplayte.maxfreeform.hook.hooks.securitycenter.RemoveConversationBubbleSettingsRestriction
import com.yifeplayte.maxfreeform.hook.hooks.systemui.CanNotificationSlide
import com.yifeplayte.maxfreeform.hook.hooks.systemui.HideFreeformTopBar
import com.yifeplayte.maxfreeform.hook.hooks.systemui.RemoveConversationBubbleSettingsRestrictionUI
import com.yifeplayte.maxfreeform.hook.hooks.systemui.RemoveFreeformBottomBar
import com.yifeplayte.maxfreeform.hook.hooks.systemui.RemoveFreeformTopBar
import com.yifeplayte.maxfreeform.hook.utils.XSharedPreferences.getBoolean
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

private const val TAG = "MaxFreeForm"
val PACKAGE_NAME_HOOKED = setOf(
    "android",
    "com.miui.home",
    "com.android.systemui",
    "com.miui.securitycenter",
)

@Suppress("unused")
class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName in PACKAGE_NAME_HOOKED) {

            // Init EzXHelper
            EzXHelper.initHandleLoadPackage(lpparam)
            EzXHelper.setLogTag(TAG)
            EzXHelper.setToastTag(TAG)

            // Init hooks
            when (EzXHelper.hostPackageName) {
                "android" -> {
                    initHook(UnlockFreeformQuantityLimit)
                    initHook(UnlockSideHideFreeform, "unlock_side_hide_freeform")
                    initHook(RemoveSmallWindowRestrictions, "remove_small_window_restrictions")
                    initHook(UnlockForegroundPin, "unlock_foreground_pin")
                }

                "com.miui.home" -> {
                    initHook(UnlockEnterSmallWindow)
                    initHook(AddFreeformShortcut, "add_freeform_shortcut")
                }

                "com.android.systemui" -> {
                    initHook(CanNotificationSlide, "can_notification_slide")
                    initHook(
                        RemoveConversationBubbleSettingsRestrictionUI,
                        "remove_conversation_bubble_settings_restriction"
                    )
                    initHook(HideFreeformTopBar, "hide_freeform_top_bar")
                    initHook(RemoveFreeformTopBar, "remove_freeform_top_bar")
                    initHook(RemoveFreeformBottomBar, "remove_freeform_bottom_bar")
                }

                "com.miui.securitycenter" -> {
                    initHook(
                        RemoveConversationBubbleSettingsRestriction,
                        "remove_conversation_bubble_settings_restriction"
                    )
                }
            }
        }
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelper.initZygote(startupParam)
    }

    private fun initHook(hook: BaseHook, key: String, defValue: Boolean = false) =
        initHook(hook, getBoolean(key, defValue))

    private fun initHook(hook: BaseHook, enable: Boolean = true) {
        if (enable) runCatching {
            if (hook.isInit) return
            hook.init()
            hook.isInit = true
            Log.ix("Inited hook: ${hook.javaClass.simpleName}")
        }.logexIfThrow("Failed init hook: ${hook.javaClass.simpleName}")
    }
}
