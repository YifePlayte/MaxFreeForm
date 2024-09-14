package com.yifeplayte.maxfreeform.activity

import android.annotation.SuppressLint
import android.os.Bundle
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.dialog.MIUIDialog
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.activity.pages.MainPage
import com.yifeplayte.maxfreeform.activity.pages.RemoveFreeformTopBarListPage
import com.yifeplayte.maxfreeform.activity.pages.UnlockForegroundPinWhitelistPage
import com.yifeplayte.maxfreeform.utils.SharedPreferences.clearTemp
import kotlin.system.exitProcess

class MainActivity : MIUIActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        checkLSPosed()
        super.onCreate(savedInstanceState)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("WorldReadableFiles")
    private fun checkLSPosed() {
        try {
            val sharedPreferences = getSharedPreferences("config", MODE_WORLD_READABLE)
            sharedPreferences.clearTemp()
            setSP(sharedPreferences)
        } catch (exception: SecurityException) {
            isLoad = false
            MIUIDialog(this) {
                setTitle(R.string.warning)
                setMessage(R.string.not_support)
                setCancelable(false)
                setRButton(R.string.done) {
                    exitProcess(0)
                }
            }.show()
        }
    }

    init {
        activity = this
        registerPage(MainPage::class.java)
        registerPage(UnlockForegroundPinWhitelistPage::class.java)
        registerPage(RemoveFreeformTopBarListPage::class.java)
    }
}
