package com.yifeplayte.maxfreeform.activity.pages

import android.annotation.SuppressLint
import android.view.View
import android.widget.Toast
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.activity.annotation.BMMainPage
import cn.fkj233.ui.activity.data.BasePage
import cn.fkj233.ui.activity.view.SwitchV
import cn.fkj233.ui.activity.view.TextSummaryV
import cn.fkj233.ui.dialog.MIUIDialog
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.hook.PACKAGE_NAME_HOOKED
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import com.yifeplayte.maxfreeform.utils.Terminal

@SuppressLint("NonConstantResourceId")
@BMMainPage(titleId = R.string.app_name)
class MainPage : BasePage() {
    override fun onCreate() {
        TitleText(textId = R.string.maxfreeform_tips)
        if (!IS_HYPER_OS) TextSummaryWithSwitch(
            TextSummaryV(
                textId = R.string.side_hide_freeform,
                tipsId = R.string.side_hide_freeform_tips
            ), SwitchV("unlock_side_hide_freeform")
        )
        TextSummaryWithSwitch(
            TextSummaryV(
                textId = R.string.remove_conversation_bubble_settings_restriction,
                tipsId = R.string.remove_conversation_bubble_settings_restriction_tips
            ), SwitchV("remove_conversation_bubble_settings_restriction")
        )
        TextSummaryWithSwitch(
            TextSummaryV(
                textId = R.string.can_notification_slide,
                tipsId = R.string.can_notification_slide_tips
            ), SwitchV("can_notification_slide")
        )
        TextSummaryWithSwitch(
            TextSummaryV(
                textId = R.string.remove_small_window_restrictions,
                tipsId = R.string.remove_small_window_restrictions_tips
            ), SwitchV("remove_small_window_restrictions")
        )
        TextSummaryWithSwitch(
            TextSummaryV(
                textId = R.string.add_freeform_shortcut,
                tipsId = R.string.add_freeform_shortcut_tips
            ), SwitchV("add_freeform_shortcut")
        )
        if (IS_HYPER_OS) {
            TextSummaryWithSwitch(
                TextSummaryV(
                    textId = R.string.unlock_multiple_task,
                    tipsId = R.string.unlock_multiple_task_tips
                ), SwitchV("unlock_multiple_task")
            )
            val bindingUnlockForegroundPin = GetDataBinding({
                MIUIActivity.safeSP.getBoolean("unlock_foreground_pin", false)
            }) { view, flags, data ->
                when (flags) {
                    1 -> view.visibility = if (data as Boolean) View.VISIBLE else View.GONE
                }
            }
            TextSummaryWithSwitch(
                TextSummaryV(
                    textId = R.string.unlock_foreground_pin
                ), SwitchV(
                    "unlock_foreground_pin",
                    dataBindingSend = bindingUnlockForegroundPin.bindingSend
                )
            )
            TextSummaryWithArrow(
                TextSummaryV(
                    textId = R.string.unlock_foreground_pin_whitelist
                ) {
                    showFragment("UnlockForegroundPinWhitelistPage")
                },
                dataBindingRecv = bindingUnlockForegroundPin.getRecv(1)
            )
            TextSummaryWithSwitch(
                TextSummaryV(
                    textId = R.string.hide_freeform_top_bar
                ), SwitchV("hide_freeform_top_bar")
            )
            TextSummaryWithSwitch(
                TextSummaryV(
                    textId = R.string.remove_freeform_top_bar
                ), SwitchV("remove_freeform_top_bar")
            )
            TextSummaryWithSwitch(
                TextSummaryV(
                    textId = R.string.remove_freeform_bottom_bar
                ), SwitchV("remove_freeform_bottom_bar")
            )
        }
        Line()
        TitleText(textId = R.string.more)
        TextSummaryWithArrow(TextSummaryV(
            textId = R.string.try_to_fix_conversation_bubbles,
            tipsId = R.string.try_to_fix_conversation_bubbles_tips
        ) {
            Terminal.exec("pm enable com.miui.securitycenter/com.miui.bubbles.services.BubblesNotificationListenerService")
            Toast.makeText(
                activity, getString(R.string.finished), Toast.LENGTH_SHORT
            ).show()
        })
        Line()
        TitleText(textId = R.string.reboot)
        TextSummaryWithArrow(TextSummaryV(
            textId = R.string.restart_all_scope
        ) {
            MIUIDialog(activity) {
                setTitle(R.string.warning)
                setMessage(R.string.restart_all_scope_tips)
                setLButton(R.string.cancel) {
                    dismiss()
                }
                setRButton(R.string.done) {
                    PACKAGE_NAME_HOOKED.forEach {
                        if (it != "android") Terminal.exec("killall $it")
                    }
                    Toast.makeText(
                        activity, getString(R.string.finished), Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
            }.show()
        })
        TextSummaryWithArrow(TextSummaryV(
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
        })
    }
}