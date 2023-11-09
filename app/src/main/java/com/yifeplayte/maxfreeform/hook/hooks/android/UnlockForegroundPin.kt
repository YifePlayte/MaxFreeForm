package com.yifeplayte.maxfreeform.hook.hooks.android

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

object UnlockForegroundPin : BaseHook() {
    override fun init() {
        if (!IS_HYPER_OS) return
        loadClass("com.android.server.wm.MiuiFreeFormGestureController").methodFinder()
            .filterByName("needForegroundPin").first().createHook {
                returnConstant(true)
            }
    }
}