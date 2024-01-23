package com.yifeplayte.maxfreeform.hook.utils

import com.yifeplayte.maxfreeform.BuildConfig
import de.robv.android.xposed.XSharedPreferences

object XSharedPreferences {
    fun getBoolean(key: String, defValue: Boolean): Boolean {
        val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID, "config")
        if (prefs.hasFileChanged()) {
            prefs.reload()
        }
        return prefs.getBoolean(key, defValue)
    }

    fun getString(key: String, defValue: String): String {
        val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID, "config")
        if (prefs.hasFileChanged()) {
            prefs.reload()
        }
        return prefs.getString(key, defValue) ?: defValue
    }

    fun getStringSet(key: String, defValue: Set<String>): Set<String> {
        val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID, "config")
        if (prefs.hasFileChanged()) {
            prefs.reload()
        }
        return prefs.getStringSet(key, defValue) ?: defValue
    }

}