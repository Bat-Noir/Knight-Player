plugins { // 👈 Fixed: strictly lowercase 'p'
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {

    namespace = "com.example.knightplayer"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.knightplayer"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_11
        targetCompatibility =
            JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

//////////////////////////////////////////////////////////////
// 📦 DEPENDENCIES
//////////////////////////////////////////////////////////////

dependencies {
    implementation("com.google.code.gson:gson:2.11.0") // 👈 This is all we need for the database now!

    //////////////////////////////////////////////////////////
    // CORE ANDROID
    //////////////////////////////////////////////////////////

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    //////////////////////////////////////////////////////////
    // COMPOSE BOM
    //////////////////////////////////////////////////////////

    implementation(
        platform(libs.androidx.compose.bom)
    )

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    //////////////////////////////////////////////////////////
    // MATERIAL UI
    //////////////////////////////////////////////////////////

    implementation(
        "androidx.compose.material3:material3"
    )

    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    //////////////////////////////////////////////////////////
    // COIL (THUMBNAILS + VIDEO PREVIEW)
    //////////////////////////////////////////////////////////

    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-video:2.6.0")

    //////////////////////////////////////////////////////////
    // 🎬 MEDIA3 EXOPLAYER (PLAYER CORE)
    //////////////////////////////////////////////////////////

    implementation(
        "androidx.media3:media3-exoplayer:1.10.1"
    )

    implementation(
        "androidx.media3:media3-ui:1.10.1"
    )

    implementation(
        "androidx.media3:media3-common:1.10.1"
    )

    // Session → subtitles + controller styling
    implementation(
        "androidx.media3:media3-session:1.10.1"
    )

    implementation("androidx.media3:media3-exoplayer-hls:1.10.1") // Use your project version

    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)

    //////////////////////////////////////////////////////////
    // 🧭 NAVIGATION (Knight OS Routing)
    //////////////////////////////////////////////////////////

    implementation("androidx.navigation:navigation-compose:2.9.8")

    //////////////////////////////////////////////////////////
    // 🎨 PALETTE (Cinematic Ambient Glows)
    //////////////////////////////////////////////////////////

    implementation("androidx.palette:palette-ktx:1.0.0")

    //////////////////////////////////////////////////////////
    // 🗄️ LIFECYCLE & VIEWMODEL (Clean Architecture)
    //////////////////////////////////////////////////////////

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")

    //////////////////////////////////////////////////////////
    // TESTING
    //////////////////////////////////////////////////////////

    testImplementation(libs.junit)

    androidTestImplementation(
        libs.androidx.junit
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )
}
