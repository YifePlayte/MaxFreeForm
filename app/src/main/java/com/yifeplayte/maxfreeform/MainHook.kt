package com.yifeplayte.maxfreeform

import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.Log.logexIfThrow
import com.yifeplayte.maxfreeform.hook.BaseHook
import com.yifeplayte.maxfreeform.hook.android.GetMaxMiuiFreeFormStackCount
import com.yifeplayte.maxfreeform.hook.android.GetMaxMiuiFreeFormStackCountForFlashBack
import com.yifeplayte.maxfreeform.hook.android.MultiFreeFormSupported
import com.yifeplayte.maxfreeform.hook.android.ShouldStopStartFreeform
import com.yifeplayte.maxfreeform.hook.home.CanTaskEnterMiniSmallWindow
import com.yifeplayte.maxfreeform.hook.home.CanTaskEnterSmallWindow
import com.yifeplayte.maxfreeform.hook.securitycenter.IsSbnBelongToActiveBubbleApp
import com.yifeplayte.maxfreeform.hook.systemui.CanNotificationSlide
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
                    initHooks(MultiFreeFormSupported)
                }
                "com.miui.home" -> {
                    initHooks(CanTaskEnterSmallWindow)
                    initHooks(CanTaskEnterMiniSmallWindow)
                }
                "com.android.systemui" -> {
                    initHooks(CanNotificationSlide)
                }
                "com.miui.securitycenter" -> {
                    initHooks(IsSbnBelongToActiveBubbleApp)
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
