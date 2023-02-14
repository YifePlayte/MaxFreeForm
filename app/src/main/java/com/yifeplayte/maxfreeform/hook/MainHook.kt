package com.yifeplayte.maxfreeform.hook

import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.Log.logexIfThrow
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.hook.hooks.android.*
import com.yifeplayte.maxfreeform.hook.hooks.home.CanTaskEnterMiniSmallWindow
import com.yifeplayte.maxfreeform.hook.hooks.home.CanTaskEnterSmallWindow
import com.yifeplayte.maxfreeform.hook.hooks.home.StartSmallWindow
import com.yifeplayte.maxfreeform.hook.hooks.securitycenter.GetDefaultBubbles
import com.yifeplayte.maxfreeform.hook.hooks.systemui.CanNotificationSlide
import com.yifeplayte.maxfreeform.utils.XSharedPreferences
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

private const val TAG = "MaxFreeForm"
private val PACKAGE_NAME_HOOKED = setOf(
    "android",
    "com.miui.home",
    "com.android.systemui",
    "com.miui.securitycenter"
)

class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName in PACKAGE_NAME_HOOKED) {
            // Init EzXHelper
            EzXHelperInit.initHandleLoadPackage(lpparam)
            EzXHelperInit.setLogTag(TAG)
            EzXHelperInit.setToastTag(TAG)
            // Init hooks
            when (lpparam.packageName) {
                "android" -> {
                    initHooks(GetMaxMiuiFreeFormStackCount)
                    initHooks(GetMaxMiuiFreeFormStackCountForFlashBack)
                    initHooks(ShouldStopStartFreeform)
                    if (XSharedPreferences.getBoolean("side_hide", true)) {
                        initHooks(MultiFreeFormSupported)
                    }
                    if (XSharedPreferences.getBoolean("remove_small_window_restrictions", true)) {
                        initHooks(RemoveSmallWindowRestrictions)
                    }
                }
                "com.miui.home" -> {
                    initHooks(CanTaskEnterSmallWindow)
                    initHooks(CanTaskEnterMiniSmallWindow)
                    initHooks(StartSmallWindow)
                }
                "com.android.systemui" -> {
                    if (XSharedPreferences.getBoolean("can_notification_slide", true)) {
                        initHooks(CanNotificationSlide)
                    }
                }
                "com.miui.securitycenter" -> {
                    if (XSharedPreferences.getBoolean("side_hide_notification", true)) {
                        // initHooks(IsSbnBelongToActiveBubbleApp)
                        // initHooks(GetBubbleAppString)
                        initHooks(GetDefaultBubbles)
                    }
                }
            }
        }
    }

    private fun initHooks(vararg hook: BaseHook) {
        hook.forEach {
            runCatching {
                if (it.isInit) return@forEach
                it.init()
                it.isInit = true
                Log.i("Inited hook: ${it.javaClass.simpleName}")
            }.logexIfThrow("Failed init hook: ${it.javaClass.simpleName}")
        }
    }

}
