package com.yifeplayte.maxfreeform.hook.android

import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookReturnConstant
import com.yifeplayte.maxfreeform.hook.BaseHook
import de.robv.android.xposed.XposedBridge

object ShouldStopStartFreeform : BaseHook() {
    override fun init() {
        try {
            findMethod("com.android.server.wm.MiuiFreeFormManagerService") {
                name == "shouldStopStartFreeform"
            }.hookReturnConstant(false)
            XposedBridge.log("MaxFreeForm: Hook shouldStopStartFreeform success!")
        } catch (e: Throwable) {
            XposedBridge.log("MaxFreeForm: Hook shouldStopStartFreeform failed!")
        }
    }

}
