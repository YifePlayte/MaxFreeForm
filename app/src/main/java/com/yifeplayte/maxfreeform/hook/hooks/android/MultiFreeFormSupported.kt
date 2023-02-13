package com.yifeplayte.maxfreeform.hook.hooks.android

import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.hookReturnConstant
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.XSharedPreferences
import de.robv.android.xposed.XposedBridge

object MultiFreeFormSupported : BaseHook() {
    override fun init() {
        try {
            if (XSharedPreferences.getBoolean("recents_to_small_freeform", false)) {
                findMethod("android.util.MiuiMultiWindowUtils") {
                    name == "multiFreeFormSupported"
                }.hookBefore {
                    val ex = Throwable()
                    val stackTrace = ex.stackTrace
                    var mResult = true
                    for (i in stackTrace) {
                        if (i.className == "com.android.server.wm.MiuiFreeFormGestureController\$FreeFormReceiver") {
                            mResult = false
                            break
                        }
                    }
                    it.result = mResult
                }
                XposedBridge.log("MaxFreeForm: Hook multiFreeFormSupported with recents_to_small_freeform success!")
            } else {
                findMethod("android.util.MiuiMultiWindowUtils") {
                    name == "multiFreeFormSupported"
                }.hookReturnConstant(true)
                XposedBridge.log("MaxFreeForm: Hook multiFreeFormSupported success!")
            }
        } catch (e: Throwable) {
            XposedBridge.log("MaxFreeForm: Hook multiFreeFormSupported failed!")
            XposedBridge.log(e)
        }
    }

}
