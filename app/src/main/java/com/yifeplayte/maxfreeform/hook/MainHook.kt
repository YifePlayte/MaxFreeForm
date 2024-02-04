package com.yifeplayte.maxfreeform.hook

import com.github.kyuubiran.ezxhelper.EzXHelper
import com.yifeplayte.maxfreeform.hook.hooks.multipackage.RemoveConversationBubbleSettingsRestriction
import com.yifeplayte.maxfreeform.hook.hooks.multipackage.RemoveSmallWindowRestrictions
import com.yifeplayte.maxfreeform.hook.hooks.singlepackage.Android
import com.yifeplayte.maxfreeform.hook.hooks.singlepackage.Home
import com.yifeplayte.maxfreeform.hook.hooks.singlepackage.SystemUI
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

private const val TAG = "MaxFreeForm"
private val singlePackagesHooked = setOf(
    Android,
    Home,
    SystemUI,
)
private val multiPackagesHooked = setOf(
    RemoveConversationBubbleSettingsRestriction,
    RemoveSmallWindowRestrictions,
)
val PACKAGE_NAME_HOOKED: Set<String>
    get() {
        val packageNameHooked = mutableSetOf<String>()
        singlePackagesHooked.forEach { packageNameHooked.add(it.packageName) }
        multiPackagesHooked.forEach { packageNameHooked.addAll(it.hooks.keys) }
        return packageNameHooked
    }

@Suppress("unused")
class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {

        // init EzXHelper
        if (lpparam.isFirstApplication) {
            EzXHelper.initHandleLoadPackage(lpparam)
            EzXHelper.setLogTag(TAG)
            EzXHelper.setToastTag(TAG)
        }

        // single package
        singlePackagesHooked.forEach { it.init() }

        // multiple package
        multiPackagesHooked.forEach { it.init() }
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {

        // init EzXHelper
        EzXHelper.initZygote(startupParam)
        EzXHelper.setLogTag(TAG)
        EzXHelper.setToastTag(TAG)
    }
}
