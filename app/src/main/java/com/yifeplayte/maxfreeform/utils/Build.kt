package com.yifeplayte.maxfreeform.utils

import com.github.kyuubiran.ezxhelper.ClassUtils.getStaticObjectOrNullAs
import com.github.kyuubiran.ezxhelper.ClassUtils.invokeStaticMethodBestMatch
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass

/**
 * 获取系统信息
 */
object Build {
    private val clazzMiuiBuild by lazy {
        loadClass("miui.os.Build")
    }

    private val clazzSystemProperties by lazy {
        loadClass("android.os.SystemProperties")
    }

    /**
     * 设备是否为平板
     */
    val IS_TABLET by lazy {
        getStaticObjectOrNullAs<Boolean>(clazzMiuiBuild, "IS_TABLET") ?: false
    }

    /**
     * 是否为国际版系统
     */
    val IS_INTERNATIONAL_BUILD by lazy {
        getStaticObjectOrNullAs<Boolean>(clazzMiuiBuild, "IS_INTERNATIONAL_BUILD") ?: false
    }

    /**
     * 是否为HyperOS
     */
    val IS_HYPER_OS by lazy {
        invokeStaticMethodBestMatch(
            clazzSystemProperties, "getInt", null, "ro.mi.os.version.code", -1
        ) != -1
    }

    /**
     * HyperOS 主版本号
     */
    val HYPER_OS_VERSION_CODE by lazy {
        invokeStaticMethodBestMatch(
            clazzSystemProperties, "getInt", null, "ro.mi.os.version.code", -1
        ) as Int
    }
}