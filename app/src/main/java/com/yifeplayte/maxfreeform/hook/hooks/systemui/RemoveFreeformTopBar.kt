package com.yifeplayte.maxfreeform.hook.hooks.systemui

import android.content.ComponentName
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook

object RemoveFreeformTopBar : BaseHook() {
    override fun init() {
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