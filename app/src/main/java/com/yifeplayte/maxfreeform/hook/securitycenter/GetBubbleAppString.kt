package com.yifeplayte.maxfreeform.hook.securitycenter

import com.github.kyuubiran.ezxhelper.utils.*
import com.yifeplayte.maxfreeform.hook.BaseHook
import de.robv.android.xposed.XposedBridge


object GetBubbleAppString : BaseHook() {
    override fun init() {
        try {
            val bubbleClass = loadClass("com.miui.bubbles.Bubble")
            findMethod("com.miui.bubbles.settings.BubblesSettings") {
                name == "getBubbleAppString"
            }.hookBefore {
                val stringBuilder = StringBuilder()
                val mActiveBubbles = it.thisObject.getObject("mActiveBubbles")
                for (bubble in mActiveBubbles as HashSet<*>) {
                    stringBuilder.append(
                        bubbleClass.getMethod("getPackageName").invokeAs<String>(bubble)
                    )
                    stringBuilder.append(":")
                    stringBuilder.append(bubble.getObject("userId"))
                    stringBuilder.append(",")
                }
                it.result = stringBuilder.toString()
            }
            XposedBridge.log("MaxFreeForm: Hook getBubbleAppString success!")
        } catch (e: Throwable) {
            XposedBridge.log("MaxFreeForm: Hook getBubbleAppString failed!")
        }
    }

}
