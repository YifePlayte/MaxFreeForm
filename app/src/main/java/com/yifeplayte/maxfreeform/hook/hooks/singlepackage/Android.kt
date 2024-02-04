package com.yifeplayte.maxfreeform.hook.hooks.singlepackage

import com.yifeplayte.maxfreeform.hook.hooks.BasePackage
import com.yifeplayte.maxfreeform.hook.hooks.singlepackage.android.UnlockForegroundPin
import com.yifeplayte.maxfreeform.hook.hooks.singlepackage.android.UnlockFreeformQuantityLimit
import com.yifeplayte.maxfreeform.hook.hooks.singlepackage.android.UnlockSideHideFreeform

object Android : BasePackage() {
    override val packageName = "android"
    override val hooks = setOf(
        UnlockFreeformQuantityLimit,
        UnlockSideHideFreeform,
        UnlockForegroundPin,
    )
}