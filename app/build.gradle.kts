import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aistudio.cinefilm.kxmpzq"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "2.2"

        val envFile = rootProject.file(".env")
        val envExampleFile = rootProject.file(".env.example")
        val envProps = Properties()
        if (envExampleFile.exists()) {
            envExampleFile.inputStream().use { envProps.load(it) }
        }
        if (envFile.exists()) {
            envFile.inputStream().use { envProps.load(it) }
        }
        val paystackPublicKey = System.getenv("PAYSTACK_PUBLIC_KEY")
            ?: envProps.getProperty("PAYSTACK_PUBLIC_KEY", "pk_test_mopolee_cinema_public_key")
        val paystackBackendUrl = System.getenv("PAYSTACK_BACKEND_URL")
            ?: envProps.getProperty("PAYSTACK_BACKEND_URL", "https://mopolee-paystack-backend.onrender.com")

        buildConfigField("String", "PAYSTACK_PUBLIC_KEY", "\"$paystackPublicKey\"")
        buildConfigField("String", "PAYSTACK_BACKEND_URL", "\"$paystackBackendUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("debugConfig") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            signingConfig = signingConfigs.getByName("debugConfig")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
