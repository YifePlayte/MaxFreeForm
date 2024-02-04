package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.android

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

object UnlockSideHideFreeform : BaseHook() {
    override val key = "unlock_side_hide_freeform"
    override val isEnabled get() = !IS_HYPER_OS and super.isEnabled
    override fun hook() {
        loadClass("android.util.MiuiMultiWindowUtils").methodFinder()
            .filterByName("multiFreeFormSupported").first().createHook {
                returnConstant(true)
            }
    }
}