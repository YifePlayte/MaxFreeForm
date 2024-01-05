package com.yifeplayte.maxfreeform.hook.hooks.systemui

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook

object RemoveFreeformBottomBar : BaseHook() {
    override fun init() {
        val clazzMiuiBaseWindowDecoration =
            loadClass("com.android.wm.shell.miuimultiwinswitch.miuiwindowdecor.MiuiBaseWindowDecoration")
        clazzMiuiBaseWindowDecoration.methodFinder().filterByName("createBottomCaption").first()
            .createHook {
                returnConstant(null)
            }
    }
}