package com.yifeplayte.maxfreeform.hook.hooks.singlepackage

import com.yifeplayte.maxfreeform.hook.hooks.BasePackage
import com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui.CanNotificationSlide
import com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui.HideFreeformTopBar
import com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui.RemoveFreeformBottomBar
import com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui.RemoveFreeformTopBar
import com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui.UnlockMultipleTask

object SystemUI : BasePackage() {
    override val packageName = "com.android.systemui"
    override val hooks = setOf(
        CanNotificationSlide,
        HideFreeformTopBar,
        RemoveFreeformBottomBar,
        RemoveFreeformTopBar,
        UnlockMultipleTask,
    )
}