package com.yifeplayte.maxfreeform.activity.pages

import android.widget.Toast
import cn.fkj233.ui.activity.annotation.BMMainPage
import cn.fkj233.ui.activity.data.BasePage
import cn.fkj233.ui.activity.view.SwitchV
import cn.fkj233.ui.activity.view.TextSummaryV
import cn.fkj233.ui.dialog.MIUIDialog
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.utils.Terminal

@BMMainPage(titleId = R.string.app_name)
class MainPage : BasePage() {
    override fun onCreate() {
        TitleText(textId = R.string.maxfreeform_tips)
        TextSummaryWithSwitch(
            TextSummaryV(
                textId = R.string.side_hide_freeform,
                tipsId = R.string.side_hide_freeform_tips
            ),
            SwitchV("side_hide", true)
        )
        TextSummaryWithSwitch(
            TextSummaryV(
                textId = R.string.remove_conversation_bubble_settings_restriction,
                tipsId = R.string.remove_conversation_bubble_settings_restriction_tips
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
        TitleText(textId = R.string.more)
        TextSummaryArrow(
            TextSummaryV(
                textId = R.string.try_to_fix_conversation_bubbles,
                tipsId = R.string.try_to_fix_conversation_bubbles_tips
            ) {
                Terminal.exec("pm enable com.miui.securitycenter/com.miui.bubbles.services.BubblesNotificationListenerService")
                Toast.makeText(
                    activity,
                    getString(R.string.finished),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        Line()
        TitleText(textId = R.string.reboot)
        TextSummaryArrow(
            TextSummaryV(
                textId = R.string.restart_all_scope
            ) {
                MIUIDialog(activity) {
                    setTitle(R.string.warning)
                    setMessage(R.string.restart_all_scope_tips)
                    setLButton(R.string.cancel) {
                        dismiss()
                    }
                    setRButton(R.string.done) {
                        val command = arrayOf(
                            "killall com.android.systemui",
                            "killall com.miui.home",
                            "killall com.miui.securitycenter",
                        )
                        Terminal.exec(command)
                        Toast.makeText(
                            activity,
                            getString(R.string.finished),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.show()
            }
        )
        TextSummaryArrow(
            TextSummaryV(
                textId = R.string.reboot_system
            ) {
                MIUIDialog(activity) {
                    setTitle(R.string.warning)
                    setMessage(R.string.reboot_tips)
                    setLButton(R.string.cancel) {
                        dismiss()
                    }
                    setRButton(R.string.done) {
                        Terminal.exec("/system/bin/sync;/system/bin/svc power reboot || reboot")
                    }
                }.show()
            }
        )
    }
}