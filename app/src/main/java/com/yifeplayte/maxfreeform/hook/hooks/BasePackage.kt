package com.yifeplayte.maxfreeform.hook.hooks

import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.LogExtensions.logexIfThrow
import com.yifeplayte.maxfreeform.utils.ClassScanner.scanObjectOf

abstract class BasePackage(val packageName: String) {
    private var isInit: Boolean = false
    open val hooks: List<BaseHook> by lazy {
        scanObjectOf<BaseHook>(javaClass.packageName + "." + javaClass.simpleName.lowercase())
    }

    fun init() {
        if (EzXHelper.hostPackageName != packageName) return
        if (isInit) return
        runCatching {
            hooks.forEach { it.init() }
            isInit = true
            Log.ix("Inited package: ${this.javaClass.simpleName}")
        }.logexIfThrow("Failed init package: ${this.javaClass.simpleName}")
    }
}