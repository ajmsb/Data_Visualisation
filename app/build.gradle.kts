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
        targetSdk = 35
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
    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:4.3.3")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Vico Charts - using latest version only
    val vicoVersion = "2.1.2"
    implementation("com.patrykandpatrick.vico:core:$vicoVersion")
    implementation("com.patrykandpatrick.vico:compose:$vicoVersion")
    // For Material 2 theming in Jetpack Compose.
    implementation("com.patrykandpatrick.vico:compose-m2:$vicoVersion")
    // For Material 3 theming in Jetpack Compose.
    implementation("com.patrykandpatrick.vico:compose-m3:$vicoVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    // Material Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.6")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.2.0")

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