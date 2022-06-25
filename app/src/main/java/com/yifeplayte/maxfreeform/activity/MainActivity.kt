package com.yifeplayte.maxfreeform.activity

import android.annotation.SuppressLint
import android.os.Bundle
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.activity.view.SwitchV
import cn.fkj233.ui.activity.view.TextSummaryV
import cn.fkj233.ui.dialog.MIUIDialog
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.util.Utils
import kotlin.system.exitProcess

class MainActivity : MIUIActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        checkLSPosed()
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("WorldReadableFiles")
    private fun checkLSPosed() {
        try {
            setSP(getSharedPreferences("config", MODE_WORLD_READABLE))
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
        initView {
            registerMain(getString(R.string.app_name), false) {
                TextSummaryWithSwitch(
                    TextSummaryV(
                        textId = R.string.side_hide_freeform,
                        tipsId = R.string.side_hide_freeform_tips
                    ),
                    SwitchV("side_hide", true)
                )
                TextSummaryWithSwitch(
                    TextSummaryV(
                        textId = R.string.ignore_bubble_notification_settings,
                        tipsId = R.string.ignore_bubble_notification_settings_tips
                    ),
                    SwitchV("side_hide_notification", true)
                )
                TextSummaryWithSwitch(
                    TextSummaryV(
                        textId = R.string.can_notification_slide,
                        tipsId = R.string.can_notification_slide_tips
                    ),
                    SwitchV("can_notification_slide", true)
                )
                TextSummaryWithSwitch(
                    TextSummaryV(
                        textId = R.string.recents_to_small_freeform,
                        tipsId = R.string.recents_to_small_freeform_tips
                    ),
                    SwitchV("recents_to_small_freeform", false)
                )
                Line()
                TitleText(getString(R.string.reboot))
                @Suppress("DEPRECATION")
                TextSummaryArrow(
                    TextSummaryV(getString(R.string.reboot_system)) {
                        MIUIDialog(this@MainActivity) {
                            setTitle(R.string.warning)
                            setMessage(R.string.reboot_tips)
                            setLButton(R.string.cancal) {
                                dismiss()
                            }
                            setRButton(R.string.done) {
                                Utils.exec("/system/bin/sync;/system/bin/svc power reboot || reboot")
                            }
                        }.show()
                    }
                )
            }
        }
    }
}
