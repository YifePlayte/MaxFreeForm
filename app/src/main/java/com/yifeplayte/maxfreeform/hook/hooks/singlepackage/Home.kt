package com.yifeplayte.maxfreeform.hook.hooks.singlepackage

import com.yifeplayte.maxfreeform.hook.hooks.BasePackage
import com.yifeplayte.maxfreeform.hook.hooks.singlepackage.home.AddFreeformShortcut
import com.yifeplayte.maxfreeform.hook.hooks.singlepackage.home.UnlockEnterSmallWindow

object Home : BasePackage() {
    override val packageName = "com.miui.home"
    override val hooks = setOf(
        UnlockEnterSmallWindow,
        AddFreeformShortcut,
    )
}