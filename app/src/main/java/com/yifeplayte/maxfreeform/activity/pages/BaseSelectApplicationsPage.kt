package com.yifeplayte.maxfreeform.activity.pages

import android.content.pm.ApplicationInfo
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.activity.data.BasePage
import cn.fkj233.ui.activity.fragment.MIUIFragment
import cn.fkj233.ui.activity.view.SwitchV
import cn.fkj233.ui.activity.view.TextSummaryV
import cn.fkj233.ui.activity.view.TextSummaryWithArrowV
import cn.fkj233.ui.activity.view.TextSummaryWithSwitchV
import cn.fkj233.ui.dialog.MIUIDialog
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.utils.SharedPreferences.addStringToStringSet
import com.yifeplayte.maxfreeform.utils.SharedPreferences.generateStringSetFromTempBoolean
import com.yifeplayte.maxfreeform.utils.SharedPreferences.prepareTempBooleanFromStringSet
import com.yifeplayte.maxfreeform.utils.SharedPreferences.removeStringFromStringSet
import io.github.ranlee1.jpinyin.PinyinFormat
import io.github.ranlee1.jpinyin.PinyinHelper

abstract class BaseSelectApplicationsPage : BasePage() {
    abstract val key: String

    init {
        skipLoadItem = true
    }

    override fun asyncInit(fragment: MIUIFragment) {
        initApplicationList(fragment)
    }

    private fun initApplicationList(
        fragment: MIUIFragment,
        keyword: String = "",
        showSystemApplications: Boolean = MIUIActivity.safeSP.mSP?.getBoolean(
            "temp_show_system_applications", false
        ) == true
    ) {
        val sharedPreferences = MIUIActivity.safeSP.mSP
        fragment.showLoading()
        fragment.clearAll()
        fragment.addItem(
            TextSummaryWithArrowV(TextSummaryV(
                textId = R.string.search
            ) {
                MIUIDialog(activity) {
                    setTitle(R.string.search)
                    setEditText("", keyword)
                    setLButton(R.string.clear) {
                        initApplicationList(fragment, "", showSystemApplications)
                        dismiss()
                    }
                    setRButton(R.string.done) {
                        initApplicationList(fragment, getEditText(), showSystemApplications)
                        dismiss()
                    }
                }.show()
            })
        )
        fragment.addItem(
            TextSummaryWithSwitchV(TextSummaryV(
                textId = R.string.show_system_applications
            ), SwitchV("temp_show_system_applications") {
                initApplicationList(fragment, keyword, it)
            })
        )
        runCatching {
            sharedPreferences?.prepareTempBooleanFromStringSet(key)
            val applicationsInfo = activity.packageManager.getInstalledApplications(0)
                .filter { showSystemApplications || (it.flags and ApplicationInfo.FLAG_SYSTEM) != 1 }
                .filter {
                    val labelLowercase =
                        it.loadLabel(activity.packageManager).toString().lowercase()
                    val packageNameLowercase = it.packageName.lowercase()
                    val keywordLowercase = keyword.lowercase()
                    labelLowercase.contains(keywordLowercase) || packageNameLowercase.contains(
                        keywordLowercase
                    )
                }.associateWith {
                    val label = it.loadLabel(activity.packageManager).toString()
                    PinyinHelper.convertToPinyinString(label, "", PinyinFormat.WITHOUT_TONE)
                }.entries.sortedBy { it.value }.map { it.key }
            applicationsInfo.forEach { applicationInfo ->
                fragment.addItem(
                    TextSummaryWithSwitchV(TextSummaryV(
                        text = applicationInfo.loadLabel(activity.packageManager).toString(),
                        tips = applicationInfo.packageName
                    ), SwitchV("temp_${key}_" + applicationInfo.packageName) { switchValue ->
                        sharedPreferences?.generateStringSetFromTempBoolean(key)
                        if (switchValue) {
                            sharedPreferences?.addStringToStringSet(
                                key, applicationInfo.packageName
                            )
                        } else {
                            sharedPreferences?.removeStringFromStringSet(
                                key, applicationInfo.packageName
                            )
                        }
                    })
                )
            }
        }
        fragment.closeLoading()
        fragment.initData()
    }

    override fun onCreate() {}
}