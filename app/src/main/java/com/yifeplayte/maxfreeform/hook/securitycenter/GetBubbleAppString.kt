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
                val iterator = (mActiveBubbles as HashSet<*>).iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    stringBuilder.append(
                        bubbleClass.getMethod("getPackageName").invokeAs<String>(next)
                    )
                    stringBuilder.append(":")
                    stringBuilder.append(next.getObject("userId"))
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
