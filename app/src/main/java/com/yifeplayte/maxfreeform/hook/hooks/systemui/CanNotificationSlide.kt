package com.yifeplayte.maxfreeform.hook.hooks.systemui

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook

object CanNotificationSlide : BaseHook() {
    override fun init() {
        loadClass("com.android.systemui.statusbar.notification.NotificationSettingsManager").methodFinder()
            .filterByName("canSlide").first().createHook {
            returnConstant(true)
        }
    }
}
