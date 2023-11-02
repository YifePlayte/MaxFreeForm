package com.yifeplayte.maxfreeform.hook.hooks.securitycenter

import android.content.Context
import android.util.ArrayMap
import com.github.kyuubiran.ezxhelper.ClassUtils.invokeStaticMethodBestMatch
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.ObjectUtils.getObjectOrNullAs
import com.github.kyuubiran.ezxhelper.ObjectUtils.invokeMethodBestMatch
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

object RemoveConversationBubbleSettingsRestriction : BaseHook() {
    override fun init() {
        if (IS_HYPER_OS) return
        val clazzBubbleApp = loadClass("com.miui.bubbles.settings.BubbleApp")
        val constructorBubbleApp =
            clazzBubbleApp.getConstructor(String::class.java, Int::class.java)
        val clazzMiuiMultiWindowUtils = loadClass("android.util.MiuiMultiWindowUtils")
        loadClass("com.miui.bubbles.settings.BubblesSettings").methodFinder()
            .filterByName("getDefaultBubbles").first().createHook {
                before { param ->
                    val arrayMap = ArrayMap<String, Any>()
                    val mContext = getObjectOrNullAs<Context>(param.thisObject, "mContext")
                    val mCurrentUserId = getObjectOrNullAs<Int>(param.thisObject, "mCurrentUserId")
                    val freeformSuggestionList = invokeStaticMethodBestMatch(
                        clazzMiuiMultiWindowUtils, "getFreeformSuggestionList", null, mContext
                    ) as List<*>
                    freeformSuggestionList.associateWith { pkg ->
                        constructorBubbleApp.newInstance(pkg, mCurrentUserId).apply {
                            invokeMethodBestMatch(this, "setChecked", null, true)
                        }
                    }.forEach { (pkg, bubbleApp) ->
                        arrayMap[pkg as String] = bubbleApp
                    }
                    param.result = arrayMap
                }
            }
    }
}
