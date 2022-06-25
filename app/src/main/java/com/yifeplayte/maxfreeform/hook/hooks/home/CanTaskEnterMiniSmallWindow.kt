package com.yifeplayte.maxfreeform.hook.hooks.home

import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookReturnConstant
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import de.robv.android.xposed.XposedBridge

object CanTaskEnterMiniSmallWindow : BaseHook() {
    override fun init() {
        try {
            findAllMethods("com.miui.home.launcher.RecentsAndFSGestureUtils") {
                name == "canTaskEnterMiniSmallWindow"
            }.hookReturnConstant(true)
            XposedBridge.log("MaxFreeForm: Hook canTaskEnterMiniSmallWindow success!")
        } catch (e: Throwable) {
            XposedBridge.log("MaxFreeForm: Hook canTaskEnterMiniSmallWindow failed!")
        }
    }

}
