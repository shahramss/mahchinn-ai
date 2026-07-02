// حذف خودکار آیکون‌های قدیمی که ممکن است داخل ریپوی GitHub مانده باشند و باعث Duplicate resources شوند.
listOf(
    "src/main/res/drawable/ic_launcher.png",
    "src/main/res/drawable/ic_launcher.webp",
    "src/main/res/drawable/ic_launcher.jpg",
    "src/main/res/drawable/ic_launcher.jpeg"
).forEach { legacyLauncherPath ->
    val legacyLauncherFile = file(legacyLauncherPath)
    if (legacyLauncherFile.exists()) legacyLauncherFile.delete()
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.mahchin.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.mahchin.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 66
        versionName = "1.22.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keyStorePath = System.getenv("MAHCHIN_KEYSTORE")
            if (!keyStorePath.isNullOrBlank()) {
                storeFile = file(keyStorePath)
                storePassword = System.getenv("MAHCHIN_KEYSTORE_PASSWORD") ?: "mahchin1234"
                keyAlias = System.getenv("MAHCHIN_KEY_ALIAS") ?: "mahchin"
                keyPassword = System.getenv("MAHCHIN_KEY_PASSWORD") ?: "mahchin1234"
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isDebuggable = false
            isMinifyEnabled = false
            val keyStorePath = System.getenv("MAHCHIN_KEYSTORE")
            if (!keyStorePath.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.04.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.navigation:navigation-compose:2.9.8")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    implementation("androidx.work:work-runtime-ktx:2.11.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
}