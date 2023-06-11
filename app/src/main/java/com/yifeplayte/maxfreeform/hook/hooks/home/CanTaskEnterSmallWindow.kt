package com.yifeplayte.maxfreeform.hook.hooks.home

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHooks
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook

object CanTaskEnterSmallWindow : BaseHook() {
    override fun init() {
        loadClass("com.miui.home.launcher.RecentsAndFSGestureUtils").methodFinder()
            .filterByName("canTaskEnterSmallWindow").toList().createHooks {
            returnConstant(true)
        }
    }
}
