package com.yifeplayte.maxfreeform.hook.hooks.android

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook

object UnlockSideHideFreeform : BaseHook() {
    override fun init() {
        loadClass("android.util.MiuiMultiWindowUtils").methodFinder().filterByName("multiFreeFormSupported").first()
            .createHook {
                returnConstant(true)
            }
    }
}