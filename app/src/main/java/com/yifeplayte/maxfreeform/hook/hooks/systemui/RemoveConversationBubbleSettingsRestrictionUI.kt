package com.yifeplayte.maxfreeform.hook.hooks.systemui

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import android.util.ArrayMap
import com.github.kyuubiran.ezxhelper.ClassUtils.getStaticObjectOrNullAs
import com.github.kyuubiran.ezxhelper.ClassUtils.invokeStaticMethodBestMatch
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.ObjectUtils.getObjectOrNullAs
import com.github.kyuubiran.ezxhelper.ObjectUtils.invokeMethodBestMatch
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

object RemoveConversationBubbleSettingsRestrictionUI : BaseHook() {
    override fun init() {
        if (!IS_HYPER_OS) return
        val clazzBubbleApp = loadClass("miui.app.MiuiBubbleApp")
        val constructorBubbleApp =
            clazzBubbleApp.getConstructor(String::class.java, Int::class.java)
        val clazzMiuiMultiWindowUtils = loadClass("android.util.MiuiMultiWindowUtils")
        val clazzMiuiBubbleSettings =
            loadClass("com.android.wm.shell.miuifreeform.miuibubbles.settings.MiuiBubbleSettings")
        loadClass("com.android.wm.shell.miuifreeform.miuibubbles.MiuiBubbleController").declaredConstructors.first()
            .createHook {
                after { param ->
                    val arrayMap = ArrayMap<String, Any>()
                    val mContext = getObjectOrNullAs<Context>(param.thisObject, "mContext")
                    val mCurrentUserId =
                        invokeStaticMethodBestMatch(UserHandle::class.java, "myUserId")
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
                    val mBubbleAppMaps = getStaticObjectOrNullAs<ArrayMap<String, Any>>(
                        clazzMiuiBubbleSettings, "mBubbleAppMaps"
                    )
                    mBubbleAppMaps?.putAll(arrayMap)
                    invokeStaticMethodBestMatch(
                        clazzMiuiBubbleSettings, "updateBubbleAppStates", null, mContext
                    )
                    val stringMiuiBubbleAppSettings = buildString {
                        mBubbleAppMaps?.values?.forEach {
                            if (isNotEmpty()) append(',')
                            append(invokeMethodBestMatch(it, "getSpString"))
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
}