package com.yifeplayte.maxfreeform.hook.hooks.systemui

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.ObjectUtils.getObjectOrNull
import com.github.kyuubiran.ezxhelper.ObjectUtils.getObjectOrNullAs
import com.github.kyuubiran.ezxhelper.ObjectUtils.invokeMethodBestMatch
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS

object CanNotificationSlide : BaseHook() {
    override fun init() {
        loadClass("com.android.systemui.statusbar.notification.NotificationSettingsManager").methodFinder()
            .filterByName("canSlide").firstOrNull()?.createHook {
                returnConstant(true)
            }
        if (!IS_HYPER_OS) return
        loadClass("com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow").methodFinder()
            .filterByName("updateMiniWindowBar").first().createHook {
                before {
                    val miniWindowTargetPkg =
                        invokeMethodBestMatch(it.thisObject, "getMiniWindowTargetPkg") as String
                    val mAppMiniWindowManager =
                        invokeMethodBestMatch(it.thisObject, "getMAppMiniWindowManager")!!
                    val notificationSettingsManager = getObjectOrNull(
                        mAppMiniWindowManager, "notificationSettingsManager"
                    )!!
                    val mAllowNotificationSlide = getObjectOrNullAs<MutableList<String>>(
                        notificationSettingsManager, "mAllowNotificationSlide"
                    )!!
                    if (miniWindowTargetPkg !in mAllowNotificationSlide) {
                        mAllowNotificationSlide.add(miniWindowTargetPkg)
                    }
                }
            }
    }
}
