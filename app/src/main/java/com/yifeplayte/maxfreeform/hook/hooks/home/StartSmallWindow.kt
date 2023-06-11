package com.yifeplayte.maxfreeform.hook.hooks.home

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHooks
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import de.robv.android.xposed.XC_MethodHook


object StartSmallWindow : BaseHook() {
    override fun init() {
        var hook1: List<XC_MethodHook.Unhook>? = null
        var hook2: List<XC_MethodHook.Unhook>? = null
        loadClass("com.miui.home.recents.views.RecentsTopWindowCrop").methodFinder().filterByName("startSmallWindow")
            .toList().createHooks {
                before {
                    hook1 = loadClass("android.util.MiuiMultiWindowUtils").methodFinder()
                        .filterByName("startSmallFreeform").filterByParamCount(4).toList().createHooks {
                            before {
                                it.args[3] = false
                                hook2 = loadClass("miui.app.MiuiFreeFormManager").methodFinder()
                                    .filterByName("getAllFreeFormStackInfosOnDisplay").toList().createHooks {
                                        returnConstant(null)
                                    }
                            }
                            after {
                                hook2?.forEach { it.unhook() }
                            }
                        }
                }
                after {
                    hook1?.forEach { it.unhook() }
                }
            }
    }
}
