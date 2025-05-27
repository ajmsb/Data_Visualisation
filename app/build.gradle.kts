plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.practicaltrainingproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.practicaltrainingproject"
        minSdk = 29
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}



dependencies {
    val vicoVersion = "2.1.2"
    implementation("com.patrykandpatrick.vico:core:$vicoVersion")
    implementation("com.patrykandpatrick.vico:compose:$vicoVersion")
    // For Material 2 theming in Jetpack Compose.
    implementation("com.patrykandpatrick.vico:compose-m2:$vicoVersion")
    // For Material 3 theming in Jetpack Compose.
    implementation("com.patrykandpatrick.vico:compose-m3:$vicoVersion")
    val vicoVersion1 = "1.16.1"
    implementation("com.patrykandpatrick.vico:core:$vicoVersion1")
    implementation("com.patrykandpatrick.vico:compose:$vicoVersion1")
    // For Material 2 theming in Jetpack Compose.
    implementation("com.patrykandpatrick.vico:compose-m2:$vicoVersion1")
    // For Material 3 theming in Jetpack Compose.
    implementation("com.patrykandpatrick.vico:compose-m3:$vicoVersion1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}