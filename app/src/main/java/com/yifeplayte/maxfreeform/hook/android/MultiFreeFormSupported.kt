package com.yifeplayte.maxfreeform.hook.android

import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookReturnConstant
import com.yifeplayte.maxfreeform.hook.BaseHook
import de.robv.android.xposed.XposedBridge

object MultiFreeFormSupported : BaseHook() {
    override fun init() {
        try {
            findMethod("android.util.MiuiMultiWindowUtils") {
                name == "multiFreeFormSupported"
            }.hookReturnConstant(true)
            XposedBridge.log("MaxFreeForm: Hook multiFreeFormSupported success!")
        } catch (e: Throwable) {
            XposedBridge.log("MaxFreeForm: Hook multiFreeFormSupported failed!")
        }
    }

}
