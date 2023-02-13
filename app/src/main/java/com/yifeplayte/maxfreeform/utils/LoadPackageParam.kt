package com.yifeplayte.maxfreeform.utils

import android.os.Build
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

object LoadPackageParam {
    fun XC_LoadPackage.LoadPackageParam.getAppVersionCode(): Int {
        return try {
            if (this.packageName == "android") {
                return Build.VERSION.SDK_INT
            }
            val parser = loadClass("android.content.pm.PackageParser").newInstance()
            val apkPath = File(this.appInfo.sourceDir)
            val pkg = parser.invokeMethod(
                "parsePackage",
                args(apkPath, 0),
                argTypes(File::class.java, Int::class.java)
            )
            pkg?.javaClass?.field("mVersionCode")?.getInt(pkg) ?: 0
        } catch (e: Throwable) {
            0
        }
    }

    fun XC_LoadPackage.LoadPackageParam.getAppVersionName(): String {
        return try {
            if (this.packageName == "android") {
                return Build.VERSION.RELEASE_OR_CODENAME
            }
            val parser = loadClass("android.content.pm.PackageParser").newInstance()
            val apkPath = File(this.appInfo.sourceDir)
            val pkg = parser.invokeMethod(
                "parsePackage",
                args(apkPath, 0),
                argTypes(File::class.java, Int::class.java)
            )
            (pkg?.javaClass?.field("mVersionName")?.get(pkg) ?: "Error: Unknown") as String
        } catch (e: Throwable) {
            "Error: Unknown"
        }
    }

}
