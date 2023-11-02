package com.yifeplayte.maxfreeform.hook.hooks.home

import android.util.ArraySet
import com.github.kyuubiran.ezxhelper.ClassUtils.invokeStaticMethodBestMatch
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHooks
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.ObjectUtils.getObjectOrNullAs
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

object UnlockEnterSmallWindow : BaseHook() {
    override fun init() {
        if (IS_HYPER_OS) return
        val clazzRecentsAndFSGestureUtils =
            loadClass("com.miui.home.launcher.RecentsAndFSGestureUtils")
        clazzRecentsAndFSGestureUtils.methodFinder().filterByName("canTaskEnterSmallWindow")
            .toList().createHooks {
            returnConstant(true)
        }
        clazzRecentsAndFSGestureUtils.methodFinder().filterByName("canTaskEnterMiniSmallWindow")
            .toList().createHooks {
            before {
                it.result = invokeStaticMethodBestMatch(
                    loadClass("com.miui.home.smallwindow.SmallWindowStateHelper"), "getInstance"
                )!!.objectHelper().invokeMethodBestMatch("canEnterMiniSmallWindow") as Boolean
            }
        }
        loadClass("com.miui.home.smallwindow.SmallWindowStateHelperUseManager").methodFinder()
            .filterByName("canEnterMiniSmallWindow").first().createHook {
                before {
                    it.result = getObjectOrNullAs<ArraySet<*>>(
                        it.thisObject,
                        "mMiniSmallWindowInfoSet"
                    )!!.isEmpty()
                }
            }
        loadClass("miui.app.MiuiFreeFormManager").methodFinder()
            .filterByName("getAllFreeFormStackInfosOnDisplay")
            .toList().createHooks {
                before { param ->
                    if (Throwable().stackTrace.any {
                            it.className == "android.util.MiuiMultiWindowUtils" && it.methodName == "startSmallFreeform"
                        }) {
                        param.result = null
                    }
                }
            }
        loadClass("android.util.MiuiMultiWindowUtils").methodFinder()
            .filterByName("hasSmallFreeform").toList().createHooks {
                before { param ->
                    if (Throwable().stackTrace.any {
                            it.className == "android.util.MiuiMultiWindowUtils" && it.methodName == "startSmallFreeform"
                        }) {
                        param.result = false
                    }
                }
            }
    }
}