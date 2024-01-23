package com.yifeplayte.maxfreeform.activity.pages

import android.annotation.SuppressLint
import cn.fkj233.ui.activity.annotation.BMPage
import com.yifeplayte.maxfreeform.R

@SuppressLint("NonConstantResourceId")
@BMPage(
    key = "UnlockForegroundPinWhitelistPage", titleId = R.string.unlock_foreground_pin_whitelist
)
class UnlockForegroundPinWhitelistPage : BaseSelectApplicationsPage() {
    override val key: String = "unlock_foreground_pin_whitelist"
}