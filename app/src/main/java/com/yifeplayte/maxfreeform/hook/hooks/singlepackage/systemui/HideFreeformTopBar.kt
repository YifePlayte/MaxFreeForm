package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui

import android.view.View
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

@Suppress("unused")
object HideFreeformTopBar : BaseHook() {
    override val key = "hide_freeform_top_bar"
    override val isEnabled get() = IS_HYPER_OS and super.isEnabled
    override fun hook() {
        val clazzMiuiBaseWindowDecoration =
            loadClass("com.android.wm.shell.miuimultiwinswitch.miuiwindowdecor.MiuiBaseWindowDecoration")
        clazzMiuiBaseWindowDecoration.methodFinder().filterByName("createTopCaption").first()
            .createHook {
                after {
                    val view = it.result as View
                    view.alpha = 0f
                }
            }
    }
}