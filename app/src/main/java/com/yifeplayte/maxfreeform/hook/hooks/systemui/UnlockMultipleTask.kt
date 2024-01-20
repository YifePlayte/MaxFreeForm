package com.yifeplayte.maxfreeform.hook.hooks.systemui

import com.github.kyuubiran.ezxhelper.ClassUtils
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHooks
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

object UnlockMultipleTask : BaseHook() {
    override fun init() {
        if (!IS_HYPER_OS) return
        runCatching {
            ClassUtils.loadClass("android.app.ActivityTaskManager").methodFinder()
                .filterByName("supportMultipleTask").toList().createHooks {
                    returnConstant(true)
                }
        }
    }
}