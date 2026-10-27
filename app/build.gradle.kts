import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk = 37

    namespace = "com.yifeplayte.maxfreeform"
    ndkVersion = "28.2.13676358"

    defaultConfig {
        applicationId = "com.yifeplayte.maxfreeform"
        minSdk = 33
        targetSdk = 37
        versionCode = 29
        versionName = "5.0.0"

        ndk {
            abiFilters.add("arm64-v8a")
        }

        externalNativeBuild {
            cmake {
                arguments += listOf("-DANDROID_STL=c++_static")
                targets += "maxfreeform_native"
            }
        }
    }

    buildTypes {
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }
        named("debug") {
            versionNameSuffix = "-debug-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .format(LocalDateTime.now())
        }
    }

    androidResources {
        additionalParameters += "--allow-reserved-package-id"
        additionalParameters += "--package-id"
        additionalParameters += "0x45"
        generateLocaleConfig = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    packaging {
        jniLibs.useLegacyPackaging = false
        resources {
            excludes += "/kotlin/**"
            excludes += "/*.txt"
            excludes += "/*.bin"
            merges += "META-INF/xposed/*"
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
        prefab = true
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set("${rootProject.name}-${output.versionName.get()}.apk")
        }
    }
}

dependencies {
    compileOnly("io.github.libxposed:api:102.0.0")
    implementation("io.github.libxposed:service:102.0.0")
    implementation("io.github.lingqiqi5211.ezhooktool:core:1.1.3")
    implementation("io.github.lingqiqi5211.ezhooktool:hook-xposed-102:1.1.3")
    implementation("com.github.topjohnwu.libsu:core:6.0.0")
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:6.1")
    implementation("io.github.ranlee1:jpinyin:1.0.1")

    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.foundation:foundation-android:1.12.0")
    implementation("androidx.compose.runtime:runtime-android:1.12.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.6")

    implementation("top.yukonga.miuix.kmp:miuix-ui-android:0.9.4")
    implementation("top.yukonga.miuix.kmp:miuix-preference-android:0.9.4")
    implementation("top.yukonga.miuix.kmp:miuix-blur-android:0.9.4")
    implementation("org.lsposed.lsplt:lsplt-standalone:2.1.1-7d609ac-compat")
    implementation("top.yukonga.miuix.kmp:miuix-icons-android:0.9.4")
}
