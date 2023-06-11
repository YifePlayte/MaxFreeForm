package com.yifeplayte.maxfreeform.hook.hooks.android

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.hook.utils.XSharedPreferences.getBoolean

object MultiFreeFormSupported : BaseHook() {
    override fun init() {
        val methodMultiFreeFormSupported =
            loadClass("android.util.MiuiMultiWindowUtils").methodFinder().filterByName("multiFreeFormSupported").first()
        if (getBoolean("recents_to_small_freeform", false)) {
            methodMultiFreeFormSupported.createHook {
                before { param ->
                    param.result = !Throwable().stackTrace.any {
                        it.className == "com.android.server.wm.MiuiFreeFormGestureController\$FreeFormReceiver"
                    }
                }
            }
            Log.ix("Inited hook: RecentsToSmallFreeform")
        } else {
            methodMultiFreeFormSupported.createHook {
                returnConstant(true)
            }
        }
    }
}
