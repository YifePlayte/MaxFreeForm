package com.yifeplayte.maxfreeform.utils

object SharedPreferences {
    fun android.content.SharedPreferences.putStringSet(
        key: String, value: Set<String>
    ) {
        val editor = edit()
        editor.putStringSet(key, value)
        editor.apply()
    }

    fun android.content.SharedPreferences.clearTemp(
        key: String = ""
    ) {
        val editor = edit()
        all.filter { it.key.startsWith("temp_$key") }.forEach {
            editor.remove(it.key)
        }
        editor.apply()
    }

    fun android.content.SharedPreferences.prepareTempBooleanFromStringSet(
        key: String, value: Boolean = true
    ) {
        clearTemp(key)
        val editor = edit()
        val stringSet = getStringSet(key, null)
        stringSet?.forEach {
            editor.putBoolean("temp_${key}_$it", value)
        }
        editor.apply()
    }

    fun android.content.SharedPreferences.generateStringSetFromTempBoolean(
        key: String, value: Boolean = true
    ) {
        val stringSet = mutableSetOf<String>()
        val prefix = "temp_${key}_"
        all.forEach {
            if (!it.key.startsWith(prefix)) return@forEach
            if (it.value?.equals(value) == true) {
                stringSet.add(it.key.removePrefix(prefix))
            }
        }
        putStringSet(key, stringSet)
    }

    fun android.content.SharedPreferences.addStringToStringSet(
        key: String, value: String
    ) {
        val stringSet = getStringSet(key, mutableSetOf())!!.toMutableSet()
        stringSet.add(value)
        putStringSet(key, stringSet)
    }

    fun android.content.SharedPreferences.removeStringFromStringSet(
        key: String, value: String
    ) {
        val stringSet = getStringSet(key, mutableSetOf())!!.toMutableSet()
        stringSet.remove(value)
        putStringSet(key, stringSet)
    }
}
