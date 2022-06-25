package com.yifeplayte.maxfreeform.hook.hooks.securitycenter

import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookReturnConstant
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import de.robv.android.xposed.XposedBridge

object IsSbnBelongToActiveBubbleApp : BaseHook() {
    override fun init() {
        try {
            findMethod("com.miui.bubbles.settings.BubblesSettings") {
                name == "isSbnBelongToActiveBubbleApp"
            }.hookReturnConstant(true)
            XposedBridge.log("MaxFreeForm: Hook isSbnBelongToActiveBubbleApp success!")
        } catch (e: Throwable) {
            XposedBridge.log("MaxFreeForm: Hook isSbnBelongToActiveBubbleApp failed!")
        }
    }

}