package com.yifeplayte.maxfreeform.hook.hooks

import com.github.kyuubiran.ezxhelper.EzXHelper.hostPackageName
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.LogExtensions.logexIfThrow
import com.yifeplayte.maxfreeform.hook.utils.XSharedPreferences.getBoolean

abstract class BaseMultiHook {
    private var isInit: Boolean = false
    abstract val key: String
    abstract val hooks: Map<String, () -> Unit>
    open val isEnabled get() = getBoolean(key, false)
    fun init() {
        if (isInit) return
        if (!isEnabled) return
        hooks[hostPackageName]?.runCatching {
            invoke()
            isInit = true
            Log.ix("Inited hook: ${this@BaseMultiHook.javaClass.simpleName} in: $hostPackageName")
        }
            ?.logexIfThrow("Failed init hook: ${this@BaseMultiHook.javaClass.simpleName} in: $hostPackageName")
    }
}