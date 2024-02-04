package com.yifeplayte.maxfreeform.hook.hooks

import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.LogExtensions.logexIfThrow
import com.yifeplayte.maxfreeform.hook.utils.XSharedPreferences.getBoolean

abstract class BaseHook {
    private var isInit: Boolean = false
    abstract val key: String
    abstract fun hook()
    open val isEnabled get() = getBoolean(key, false)
    fun init() {
        if (isInit) return
        if (isEnabled) runCatching {
            hook()
            isInit = true
            Log.ix("Inited hook: ${this.javaClass.simpleName}")
        }.logexIfThrow("Failed init hook: ${this.javaClass.simpleName}")
    }
}