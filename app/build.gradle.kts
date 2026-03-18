plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.chat_app_frontend"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.chat_app_frontend"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        jniLibs {
            pickFirsts.add("**/*.so")
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.viewpager2)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("io.agora.rtc:full-sdk:4.3.0")
    // Khai báo Firebase BoM (Bill of Materials) để tự động quản lý phiên bản
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    implementation("com.google.firebase:firebase-database") // Để làm Realtime Chat
    implementation("com.google.firebase:firebase-auth") // Để quản lý người dùng và xác thực
    implementation("com.google.firebase:firebase-messaging") // Để nhận push notification chat realtime

    // Tải ảnh từ URL (avatar)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // GIF picker (GIPHY SDK)
    implementation("com.giphy.sdk:ui:2.4.1")

    // Emoji picker (Jetpack Emoji2)
    implementation("androidx.emoji2:emoji2-emojipicker:1.5.0")
    implementation("com.google.firebase:firebase-storage")
}