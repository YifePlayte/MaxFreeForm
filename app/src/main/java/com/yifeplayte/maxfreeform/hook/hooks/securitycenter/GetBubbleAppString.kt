package com.yifeplayte.maxfreeform.hook.hooks.securitycenter

import com.github.kyuubiran.ezxhelper.utils.*
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import de.robv.android.xposed.XposedBridge


object GetBubbleAppString : BaseHook() {
    override fun init() {
        try {
            val classBubble = loadClass("com.miui.bubbles.Bubble")
            findMethod("com.miui.bubbles.settings.BubblesSettings") {
                name == "getBubbleAppString"
            }.hookBefore {
                val stringBuilder = StringBuilder()
                val mActiveBubbles = it.thisObject.getObject("mActiveBubbles")
                for (bubble in mActiveBubbles as HashSet<*>) {
                    stringBuilder.append(
                        classBubble.getMethod("getPackageName").invokeAs<String>(bubble)
                    )
                    stringBuilder.append(":")
                    stringBuilder.append(bubble.getObject("userId"))
                    stringBuilder.append(",")
                }
                // XposedBridge.log("MaxFreeFormTest: getBubbleAppString called! Result:$stringBuilder")
                it.result = stringBuilder.toString()
            }
            XposedBridge.log("MaxFreeForm: Hook getBubbleAppString success!")
        } catch (e: Throwable) {
            XposedBridge.log("MaxFreeForm: Hook getBubbleAppString failed!")
            XposedBridge.log(e)
        }
    }

}
