plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.hamsterbase.burrowui"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.hamsterbase.burrowui"
        minSdk = 24
        targetSdk = 36
        versionCode = 12
        versionName = "1.5.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val project = "burrowed-launcher"
                val s = "-"
                val buildType = variant.buildType.name
                val version = variant.versionName

                val newApkName = "${project}${s}${buildType}${s}${version}.apk"
                output.outputFileName = newApkName
            }
    }
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
