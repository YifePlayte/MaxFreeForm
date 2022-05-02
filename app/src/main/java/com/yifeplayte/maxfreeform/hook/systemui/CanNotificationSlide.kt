package com.yifeplayte.maxfreeform.hook.systemui

import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.yifeplayte.maxfreeform.hook.BaseHook
import de.robv.android.xposed.XposedBridge

object CanNotificationSlide : BaseHook() {
    override fun init() {
        try {
            findMethod("com.android.systemui.statusbar.notification.NotificationSettingsManager") {
                name == "canSlide"
            }.hookMethod {
                after { param ->
                    param.result = true
                }
            }
            XposedBridge.log("MaxFreeForm: Hook canSlide success!")
        } catch (e: Throwable) {
            XposedBridge.log("MaxFreeForm: Hook canSlide failed!")
        }
    }
}
