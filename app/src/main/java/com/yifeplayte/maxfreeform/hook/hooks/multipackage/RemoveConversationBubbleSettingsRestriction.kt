package com.yifeplayte.maxfreeform.hook.hooks.multipackage

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import android.util.ArrayMap
import com.github.kyuubiran.ezxhelper.ClassUtils
import com.github.kyuubiran.ezxhelper.EzXHelper.hostPackageName
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.ObjectUtils
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseMultiHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

@Suppress("unused")
object RemoveConversationBubbleSettingsRestriction : BaseMultiHook() {
    override val key = "remove_conversation_bubble_settings_restriction"
    override val hooks = mapOf(
        "com.android.systemui" to { systemUi() },
        "com.miui.securitycenter" to { securityCenter() },
    )
    override val isEnabled
        get() = when (hostPackageName) {
            "com.android.systemui" -> IS_HYPER_OS
            "com.miui.securitycenter" -> !IS_HYPER_OS
            else -> false
        } and super.isEnabled

    private fun systemUi() {
        if (!IS_HYPER_OS) return
        val clazzBubbleApp = ClassUtils.loadClass("miui.app.MiuiBubbleApp")
        val constructorBubbleApp =
            clazzBubbleApp.getConstructor(String::class.java, Int::class.java)
        val clazzMiuiMultiWindowUtils = ClassUtils.loadClass("android.util.MiuiMultiWindowUtils")
        val clazzMiuiBubbleSettings =
            ClassUtils.loadClass("com.android.wm.shell.miuifreeform.miuibubbles.settings.MiuiBubbleSettings")
        ClassUtils.loadClass("com.android.wm.shell.miuifreeform.miuibubbles.MiuiBubbleController").declaredConstructors.first()
            .createHook {
                after { param ->
                    val arrayMap = ArrayMap<String, Any>()
                    val mContext =
                        ObjectUtils.getObjectOrNullAs<Context>(param.thisObject, "mContext")
                    val mCurrentUserId =
                        ClassUtils.invokeStaticMethodBestMatch(UserHandle::class.java, "myUserId")
                    val freeformSuggestionList = ClassUtils.invokeStaticMethodBestMatch(
                        clazzMiuiMultiWindowUtils, "getFreeformSuggestionList", null, mContext
                    ) as List<*>
                    freeformSuggestionList.associateWith { pkg ->
                        constructorBubbleApp.newInstance(pkg, mCurrentUserId).apply {
                            ObjectUtils.invokeMethodBestMatch(this, "setChecked", null, true)
                        }
                    }.forEach { (pkg, bubbleApp) ->
                        arrayMap[pkg as String] = bubbleApp
                    }
                    val mBubbleAppMaps = ClassUtils.getStaticObjectOrNullAs<ArrayMap<String, Any>>(
                        clazzMiuiBubbleSettings, "mBubbleAppMaps"
                    )
                    mBubbleAppMaps?.putAll(arrayMap)
                    ClassUtils.invokeStaticMethodBestMatch(
                        clazzMiuiBubbleSettings, "updateBubbleAppStates", null, mContext
                    )
                    val stringMiuiBubbleAppSettings = buildString {
                        mBubbleAppMaps?.values?.forEach {
                            if (isNotEmpty()) append(',')
                            append(ObjectUtils.invokeMethodBestMatch(it, "getSpString"))
                        }
                    }
                    Settings.Secure.putString(
                        mContext?.contentResolver,
                        "miui_bubble_app_settings",
                        stringMiuiBubbleAppSettings
                    )
                }
            }
    }

    private fun securityCenter() {
        if (IS_HYPER_OS) return
        val clazzBubbleApp = ClassUtils.loadClass("com.miui.bubbles.settings.BubbleApp")
        val constructorBubbleApp =
            clazzBubbleApp.getConstructor(String::class.java, Int::class.java)
        val clazzMiuiMultiWindowUtils = ClassUtils.loadClass("android.util.MiuiMultiWindowUtils")
        ClassUtils.loadClass("com.miui.bubbles.settings.BubblesSettings").methodFinder()
            .filterByName("getDefaultBubbles").first().createHook {
                before { param ->
                    val arrayMap = ArrayMap<String, Any>()
                    val mContext =
                        ObjectUtils.getObjectOrNullAs<Context>(param.thisObject, "mContext")
                    val mCurrentUserId =
                        ObjectUtils.getObjectOrNullAs<Int>(param.thisObject, "mCurrentUserId")
                    val freeformSuggestionList = ClassUtils.invokeStaticMethodBestMatch(
                        clazzMiuiMultiWindowUtils, "getFreeformSuggestionList", null, mContext
                    ) as List<*>
                    freeformSuggestionList.associateWith { pkg ->
                        constructorBubbleApp.newInstance(pkg, mCurrentUserId).apply {
                            ObjectUtils.invokeMethodBestMatch(this, "setChecked", null, true)
                        }
                    }.forEach { (pkg, bubbleApp) ->
                        arrayMap[pkg as String] = bubbleApp
                    }
                    param.result = arrayMap
                }
            }
    }
}