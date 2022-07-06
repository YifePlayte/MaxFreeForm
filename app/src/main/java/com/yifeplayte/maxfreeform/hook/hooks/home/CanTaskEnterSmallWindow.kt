package com.yifeplayte.maxfreeform.hook.hooks.home

import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookReturnConstant
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import de.robv.android.xposed.XposedBridge

object CanTaskEnterSmallWindow : BaseHook() {
    override fun init() {
        try {
            findAllMethods("com.miui.home.launcher.RecentsAndFSGestureUtils") {
                name == "canTaskEnterSmallWindow"
            }.hookReturnConstant(true)
            XposedBridge.log("MaxFreeForm: Hook canTaskEnterSmallWindow success!")
        } catch (e: Throwable) {
            XposedBridge.log("MaxFreeForm: Hook canTaskEnterSmallWindow failed!")
            XposedBridge.log(e)
        }
    }

}
