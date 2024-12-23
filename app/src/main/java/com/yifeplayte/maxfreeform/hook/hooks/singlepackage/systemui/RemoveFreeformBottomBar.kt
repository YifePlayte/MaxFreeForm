package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClassOrNull
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

@Suppress("unused")
object RemoveFreeformBottomBar : BaseHook() {
    override val key = "remove_freeform_bottom_bar"
    override val isEnabled get() = IS_HYPER_OS and super.isEnabled
    override fun hook() {
        loadClassOrNull("com.android.wm.shell.miuimultiwinswitch.miuiwindowdecor.MiuiBaseWindowDecoration")?.methodFinder()
            ?.filterByName("createBottomCaption")?.first()?.createHook {
                returnConstant(null)
            }
        loadClassOrNull("com.android.wm.shell.multitasking.miuimultiwinswitch.miuiwindowdecor.MiuiBottomDecoration")
            ?.methodFinder()?.filterByName("needBottomCaption")?.first()
            ?.createHook {
                returnConstant(false)
            }
    }
}