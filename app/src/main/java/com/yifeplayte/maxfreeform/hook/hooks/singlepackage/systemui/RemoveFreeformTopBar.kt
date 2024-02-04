package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui

import android.content.ComponentName
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

object RemoveFreeformTopBar : BaseHook() {
    override val key = "remove_freeform_top_bar"
    override val isEnabled get() = IS_HYPER_OS and super.isEnabled
    override fun hook() {
        val clazzMiuiBaseWindowDecoration =
            loadClass("com.android.wm.shell.miuimultiwinswitch.miuiwindowdecor.MiuiBaseWindowDecoration")
        clazzMiuiBaseWindowDecoration.methodFinder().filterByName("getBooleanProperty")
            .filterByAssignableParamTypes(String::class.java, ComponentName::class.java).first()
            .createHook {
                before {
                    if (it.args[0] != "miui.window.DOT_ENABLED") return@before
                    it.result = false
                }
            }
    }
}