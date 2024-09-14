package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.hook.utils.XSharedPreferences.getStringSet
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

@Suppress("unused")
object RemoveFreeformTopBarForList : BaseHook() {
    override val key = "remove_freeform_top_bar"
    override val isEnabled get() = IS_HYPER_OS
    override fun hook() {
        val clazzMiuiMultiWinSwitchConfig =
            loadClass("com.android.wm.shell.miuimultiwinswitch.MiuiMultiWinSwitchConfig")
        clazzMiuiMultiWinSwitchConfig.methodFinder().filterByName("getDotBlackList").single()
            .createHook {
                before {
                    it.result = getStringSet("remove_freeform_top_bar_list", setOf())
                }
            }
    }
}