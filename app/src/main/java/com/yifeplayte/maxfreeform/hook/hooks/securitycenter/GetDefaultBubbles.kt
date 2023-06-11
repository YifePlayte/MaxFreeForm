package com.yifeplayte.maxfreeform.hook.hooks.securitycenter

import android.annotation.SuppressLint
import android.content.Context
import android.util.ArrayMap
import com.github.kyuubiran.ezxhelper.ClassUtils
import com.github.kyuubiran.ezxhelper.ClassUtils.invokeStaticMethodBestMatch
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook

object GetDefaultBubbles : BaseHook() {
    @SuppressLint("PrivateApi")
    override fun init() {
        loadClass("com.miui.bubbles.settings.BubblesSettings").methodFinder().filterByName("getDefaultBubbles").first()
            .createHook {
                before { param ->
                    val classBubbleApp = loadClass("com.miui.bubbles.settings.BubbleApp")
                    val arrayMap = ArrayMap<String, Any>()
                    val mContext = param.thisObject.objectHelper().getObjectOrNullAs<Context>("mContext")
                    val mCurrentUserId = param.thisObject.objectHelper().getObjectOrNullAs<Int>("mCurrentUserId")
                    val freeformSuggestionList = invokeStaticMethodBestMatch(
                        loadClass("android.util.MiuiMultiWindowUtils"), "getFreeformSuggestionList", null, mContext
                    ) as List<*>
                    if (freeformSuggestionList.isNotEmpty()) {
                        freeformSuggestionList.forEach {
                            val bubbleApp = classBubbleApp.getConstructor(String::class.java, Int::class.java)
                                .newInstance(it, mCurrentUserId)
                            bubbleApp.objectHelper().invokeMethodBestMatch("setChecked", null, true)
                            arrayMap[it as String] = bubbleApp
                        }
                    }
                    param.result = arrayMap
                }
            }
    }
}
