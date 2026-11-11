plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}

android {
    namespace = "com.asgaya.husk"
    compileSdk = 34  // SIMPLE VERSION

    defaultConfig {
        applicationId = "com.asgaya.husk"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // FIXED SYNTAX
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
        jvmToolchain(17)
    }

    buildFeatures {
        viewBinding = true
    }

    // Allow unit tests to use Android classes (Log, etc.) without mocking
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

ksp {
    // Export the Room schema (versioned JSON) so migrations can be written, diffed and tested —
    // and we stop wiping the DB on every bump. See collaborative_workspace/room-migrations/.
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Existing dependencies (keep these - already there)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ADD THESE - Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ADD THESE - UI & Lifecycle
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // ADD THIS - Coroutines (explicit, TightDS recommended)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ADD THIS - JSON parsing (for bank_patterns.json)
    implementation("org.json:json:20230227")

    // ADD THIS - BouncyCastle for Bitcoin hashing (RIPEMD160, SHA256)
    implementation("org.bouncycastle:bcprov-jdk18on:1.78")

    // ADD THIS - OkHttp for Nostr WebSocket connections (Task 1 - Nostr Transport)
    implementation("com.squareup.okhttp3:okhttp:4.5.0")

    // Bitcoin Cash Wallet SDK (provides CashAddressConverter, P2SH32 support)
    // Using master-SNAPSHOT to avoid dependency resolution issues with v1.2.0
    // Exclude BouncyCastle to avoid conflicts with our bcprov-jdk18on version
    implementation("com.github.horizontalsystems.bitcoin-kit-android:bitcoincore:master-SNAPSHOT") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
    }
    implementation("com.github.horizontalsystems.bitcoin-kit-android:bitcoincashkit:master-SNAPSHOT") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
    }

    // QR Code generation and scanning (for merchant cashout)
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.5.0")
}