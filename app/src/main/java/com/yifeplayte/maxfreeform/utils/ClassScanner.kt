package com.yifeplayte.maxfreeform.utils

import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.ObjectUtils.getObjectOrNull
import com.github.kyuubiran.ezxhelper.ObjectUtils.getObjectOrNullAs
import com.github.kyuubiran.ezxhelper.ObjectUtils.getObjectOrNullUntilSuperclass
import com.github.kyuubiran.ezxhelper.ObjectUtils.invokeMethodBestMatch
import java.util.Enumeration

/**
 * 包扫描工具
 */
object ClassScanner {
    /**
     * 在某包下扫描特定类型的 Kotlin object
     *
     * @param T 需要扫描的目标类
     * @param packageName 包名
     * @param classLoader 类加载器
     * @return object 实例
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> scanObjectOf(
        packageName: String, classLoader: ClassLoader = ClassScanner::class.java.classLoader!!
    ): List<T> = runCatching {
        val dexPathList = getObjectOrNullUntilSuperclass(classLoader, "pathList")
        val dexElements = dexPathList?.let { getObjectOrNullAs<Array<*>>(it, "dexElements") }

        dexElements?.flatMap { element ->
            val dexFile = element?.let { getObjectOrNull(it, "dexFile") }
            val entries =
                dexFile?.let { invokeMethodBestMatch(it, "entries") } as? Enumeration<String>

            entries?.toList()?.filter { it.startsWith(packageName) }?.mapNotNull { entry ->
                runCatching {
                    val entryClass = Class.forName(entry, true, classLoader)
                    if (entryClass.name.contains("$") || !T::class.java.isAssignableFrom(entryClass)) null
                    else entryClass.fields.singleOrNull { it.name == "INSTANCE" }?.get(null) as T?
                }.getOrNull()
            } ?: emptyList()
        }?.distinct() ?: emptyList()
    }.getOrElse {
        Log.e("ClassScanner crashed!", it)
        emptyList()
    }
}