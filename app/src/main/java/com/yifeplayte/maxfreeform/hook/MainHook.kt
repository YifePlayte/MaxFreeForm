package com.yifeplayte.maxfreeform.hook

import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.LogExtensions.logexIfThrow
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.hook.hooks.android.GetMaxMiuiFreeFormStackCount
import com.yifeplayte.maxfreeform.hook.hooks.android.GetMaxMiuiFreeFormStackCountForFlashBack
import com.yifeplayte.maxfreeform.hook.hooks.android.MultiFreeFormSupported
import com.yifeplayte.maxfreeform.hook.hooks.android.RemoveSmallWindowRestrictions
import com.yifeplayte.maxfreeform.hook.hooks.android.ShouldStopStartFreeform
import com.yifeplayte.maxfreeform.hook.hooks.home.CanTaskEnterMiniSmallWindow
import com.yifeplayte.maxfreeform.hook.hooks.home.CanTaskEnterSmallWindow
import com.yifeplayte.maxfreeform.hook.hooks.home.StartSmallWindow
import com.yifeplayte.maxfreeform.hook.hooks.securitycenter.GetDefaultBubbles
import com.yifeplayte.maxfreeform.hook.hooks.systemui.CanNotificationSlide
import com.yifeplayte.maxfreeform.hook.utils.XSharedPreferences.getBoolean
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

private const val TAG = "MaxFreeForm"
val PACKAGE_NAME_HOOKED = setOf(
    "android",
    "com.miui.home",
    "com.android.systemui",
    "com.miui.securitycenter"
)

class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName in PACKAGE_NAME_HOOKED) {
            // Init EzXHelper
            EzXHelper.initHandleLoadPackage(lpparam)
            EzXHelper.setLogTag(TAG)
            EzXHelper.setToastTag(TAG)
            // Init hooks
            when (lpparam.packageName) {
                "android" -> {
                    initHook(GetMaxMiuiFreeFormStackCount)
                    initHook(GetMaxMiuiFreeFormStackCountForFlashBack)
                    initHook(ShouldStopStartFreeform)
                    initHook(MultiFreeFormSupported, "side_hide", true)
                    initHook(RemoveSmallWindowRestrictions, "remove_small_window_restrictions", true)
                }

                "com.miui.home" -> {
                    initHook(CanTaskEnterSmallWindow)
                    initHook(CanTaskEnterMiniSmallWindow)
                    initHook(StartSmallWindow)
                }

                "com.android.systemui" -> {
                    initHook(CanNotificationSlide, "can_notification_slide", true)
                }

                "com.miui.securitycenter" -> {
                    initHook(GetDefaultBubbles, "side_hide_notification", true)
                }
            }
        }
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
