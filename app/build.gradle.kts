import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 34

    namespace = "com.yifeplayte.maxfreeform"

    defaultConfig {
        applicationId = "com.yifeplayte.maxfreeform"
        minSdk = 31
        targetSdk = 34
        versionCode = 20
        versionName = "3.2.2"
    }

    buildTypes {
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }
        named("debug") {
            versionNameSuffix = "-debug-" +
                    DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
        }
    }

    androidResources {
        additionalParameters += "--allow-reserved-package-id"
        additionalParameters += "--package-id"
        additionalParameters += "0x45"
        generateLocaleConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/**"
            excludes += "/kotlin/**"
            excludes += "/*.txt"
            excludes += "/*.bin"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":blockmiui"))
    implementation("com.github.kyuubiran:EzXHelper:2.0.8")
    implementation("io.github.ranlee1:jpinyin:1.0.1")
    compileOnly("de.robv.android.xposed:api:82")
}
