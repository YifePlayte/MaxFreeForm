package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui

import android.content.ComponentName
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClassOrNull
import com.github.kyuubiran.ezxhelper.ClassUtils.loadFirstClassOrNull
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHooks
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

@Suppress("unused")
object RemoveFreeformTopBar : BaseHook() {
    override val key = "remove_freeform_top_bar"
    override val isEnabled get() = IS_HYPER_OS and super.isEnabled
    override fun hook() {
        loadClassOrNull("com.android.wm.shell.miuimultiwinswitch.miuiwindowdecor.MiuiBaseWindowDecoration")?.methodFinder()
            ?.filterByName("getBooleanProperty")
            ?.filterByAssignableParamTypes(String::class.java, ComponentName::class.java)?.first()
            ?.createHook {
                before {
                    if (it.args[0] != "miui.window.DOT_ENABLED") return@before
                    it.result = false
                }
            }
        loadFirstClassOrNull(
            "com.android.wm.shell.multitasking.miuimultiwinswitch.miuiwindowdecor.MiuiTopDecoration",
            "com.android.wm.shell.multitasking.miuimultiwinswitch.miuiwindowdecor.decoration.MiuiDecorationDot",
        )?.methodFinder()
            ?.filterByName("getBooleanProperty")?.filterNonAbstract()?.toList()?.createHooks {
                before {
                    it.result = false
                }
            }
    }
}