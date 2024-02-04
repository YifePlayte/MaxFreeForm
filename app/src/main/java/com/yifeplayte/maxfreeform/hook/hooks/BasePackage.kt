package com.yifeplayte.maxfreeform.hook.hooks

import com.github.kyuubiran.ezxhelper.EzXHelper.hostPackageName
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.LogExtensions.logexIfThrow

abstract class BasePackage {
    private var isInit: Boolean = false
    abstract val packageName: String
    abstract val hooks: Set<BaseHook>
    fun init() {
        if (hostPackageName != packageName) return
        if (isInit) return
        runCatching {
            hooks.forEach { it.init() }
            isInit = true
            Log.ix("Inited package: ${this.javaClass.simpleName}")
        }.logexIfThrow("Failed init package: ${this.javaClass.simpleName}")
    }
}