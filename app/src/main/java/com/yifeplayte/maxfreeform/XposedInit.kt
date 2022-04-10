package com.yifeplayte.maxfreeform

import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedInit : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        EzXHelperInit.initHandleLoadPackage(lpparam)
        when (lpparam.packageName) {
            "android" -> maxFreeFormCount(lpparam)
        }
    }

    private fun maxFreeFormCount(lpparam: XC_LoadPackage.LoadPackageParam) {
        findMethod("com.android.server.wm.MiuiFreeFormStackDisplayStrategy") {
            name == "getMaxMiuiFreeFormStackCount"
        }.hookMethod {
            after { param ->
                XposedBridge.log("MaxFreeForm: Hook getMaxMiuiFreeFormStackCount success!")
                param.result = 256
            }
        }
        XposedBridge.log("MaxFreeForm: Hook success!")
    }
}