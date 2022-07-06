package com.yifeplayte.maxfreeform.hook.hooks.android

import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookReturnConstant
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import de.robv.android.xposed.XposedBridge

object GetMaxMiuiFreeFormStackCountForFlashBack : BaseHook() {
    override fun init() {
        try {
            findMethod("com.android.server.wm.MiuiFreeFormStackDisplayStrategy") {
                name == "getMaxMiuiFreeFormStackCountForFlashBack"
            }.hookReturnConstant(256)
            XposedBridge.log("MaxFreeForm: Hook getMaxMiuiFreeFormStackCountForFlashBack success!")
        } catch (e: Throwable) {
            XposedBridge.log("MaxFreeForm: Hook getMaxMiuiFreeFormStackCountForFlashBack failed!")
            XposedBridge.log(e)
        }
    }

}
