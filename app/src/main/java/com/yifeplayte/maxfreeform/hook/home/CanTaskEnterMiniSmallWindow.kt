package com.yifeplayte.maxfreeform.hook.home

import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.hookReturnConstant
import com.yifeplayte.maxfreeform.hook.BaseHook
import de.robv.android.xposed.XposedBridge

object CanTaskEnterMiniSmallWindow : BaseHook() {
    override fun init() {
        try {
            findMethod("com.miui.home.launcher.RecentsAndFSGestureUtils") {
                name == "canTaskEnterMiniSmallWindow"
            }.hookReturnConstant(true)
            XposedBridge.log("MaxFreeForm: Hook canTaskEnterMiniSmallWindow success!")
        } catch (e: Throwable) {
            XposedBridge.log("MaxFreeForm: Hook canTaskEnterMiniSmallWindow failed!")
        }
    }
}
