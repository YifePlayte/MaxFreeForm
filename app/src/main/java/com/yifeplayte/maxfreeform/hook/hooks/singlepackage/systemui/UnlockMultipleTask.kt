package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui

import com.github.kyuubiran.ezxhelper.ClassUtils
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHooks
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

@Suppress("unused")
object UnlockMultipleTask : BaseHook() {
    override val key = "unlock_multiple_task"
    override val isEnabled get() = IS_HYPER_OS and super.isEnabled
    override fun hook() {
        runCatching {
            ClassUtils.loadClass("android.app.ActivityTaskManager").methodFinder()
                .filterByName("supportMultipleTask").toList().createHooks {
                    returnConstant(true)
                }
        }
    }
}