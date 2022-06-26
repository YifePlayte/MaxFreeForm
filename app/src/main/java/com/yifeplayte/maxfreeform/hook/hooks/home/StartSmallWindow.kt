package com.yifeplayte.maxfreeform.hook.hooks.home

import android.R.attr.classLoader
import com.github.kyuubiran.ezxhelper.utils.*
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers


object StartSmallWindow : BaseHook() {
    override fun init() {
        try {
            var hook1: List<XC_MethodHook.Unhook>? = null
            var hook2: List<XC_MethodHook.Unhook>? = null
            findAllMethods("com.miui.home.recents.views.RecentsTopWindowCrop") {
                name == "startSmallWindow"
            }.hookBefore {
                XposedBridge.log("MaxFreeFormTest: startSmallWindow called!")
                try {
                    hook1 = findAllMethods("android.util.MiuiMultiWindowUtils") {
                        name == "startSmallFreeform" && paramCount == 4
                    }.hookBefore {
                        XposedBridge.log("MaxFreeFormTest: startSmallFreeform called!")
                        it.args[3] = false
                        XposedBridge.log("MaxFreeFormTest: startSmallFreeform args changed!")
                        try {
                            hook2 = findAllMethods("miui.app.MiuiFreeFormManager") {
                                name == "getAllFreeFormStackInfosOnDisplay"
                            }.hookBefore {
                                XposedBridge.log("MaxFreeFormTest: getAllFreeFormStackInfosOnDisplay called!")
                                it.result = null
                            }
                            XposedBridge.log("MaxFreeForm: Hook getAllFreeFormStackInfosOnDisplay success!")
                        } catch (e: Throwable) {
                            XposedBridge.log("MaxFreeForm: Hook getAllFreeFormStackInfosOnDisplay failed!")
                        }
                    }
                    findAllMethods("android.util.MiuiMultiWindowUtils") {
                        name == "startSmallFreeform"
                    }.hookAfter {
                        hook2?.unhookAll()
                    }
                    XposedBridge.log("MaxFreeForm: Hook startSmallFreeform success!")
                } catch (e: Throwable) {
                    XposedBridge.log("MaxFreeForm: Hook startSmallFreeform failed!")
                }
            }
            findAllMethods("com.miui.home.recents.views.RecentsTopWindowCrop") {
                name == "startSmallWindow"
            }.hookAfter {
                hook1?.unhookAll()
            }
            XposedBridge.log("MaxFreeForm: Hook startSmallWindow success!")
        } catch (e: Throwable) {
            XposedBridge.log("MaxFreeForm: Hook startSmallWindow failed!")
        }
    }

}
