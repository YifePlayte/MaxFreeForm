package com.yifeplayte.maxfreeform.hook.hooks.securitycenter

import android.annotation.SuppressLint
import android.content.Context
import android.util.ArrayMap
import com.github.kyuubiran.ezxhelper.utils.*
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import de.robv.android.xposed.XposedBridge
import org.lsposed.hiddenapibypass.HiddenApiBypass

object GetDefaultBubbles : BaseHook() {
    @SuppressLint("PrivateApi")
    override fun init() {
        try {
            findMethod("com.miui.bubbles.settings.BubblesSettings") {
                name == "getDefaultBubbles"
            }.hookBefore { param ->
                val classBubbleApp = loadClass("com.miui.bubbles.settings.BubbleApp")
                val arrayMap = ArrayMap<String, Any>()
                val mContext = param.thisObject.getObject("mContext") as Context
                val mCurrentUserId = param.thisObject.getObject("mCurrentUserId") as Int
                val freeformSuggestionList = HiddenApiBypass.invoke(
                    Class.forName("android.util.MiuiMultiWindowUtils"),
                    null,
                    "getFreeformSuggestionList",
                    mContext
                ) as List<*>
                if (freeformSuggestionList.isNotEmpty()) {
                    for (str in freeformSuggestionList) {
                        val bubbleApp = classBubbleApp.getConstructor(
                            String::class.java, Int::class.java
                        ).newInstance(str, mCurrentUserId)
                        classBubbleApp.getMethod("setChecked", Boolean::class.java)
                            .invoke(bubbleApp, true)
                        arrayMap[str as String] = bubbleApp
                    }
                }
                param.result = arrayMap
                // XposedBridge.log("MaxFreeFormTest: getDefaultBubbles called! Result:$arrayMap")
            }
            XposedBridge.log("MaxFreeForm: Hook getDefaultBubbles success!")
        } catch (e: Throwable) {
            XposedBridge.log("MaxFreeForm: Hook getDefaultBubbles failed!")
            XposedBridge.log(e)
        }
    }

}
