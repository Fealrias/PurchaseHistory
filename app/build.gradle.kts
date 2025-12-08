import com.android.sdklib.AndroidVersion

plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.angelp.purchasehistory"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.angelp.purchasehistory"
        minSdk = 29
        targetSdk = AndroidVersion.VersionCodes.TIRAMISU
        versionCode = 9
        versionName = "1.3.1"
        testInstrumentationRunner = "com.angelp.purchasehistory.setup.CustomTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    buildToolsVersion = "36.0.0"
}

dependencies {
    implementation(fileTree("libs"))
    // Fix Duplicate class
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.2.21"))

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.paging:paging-runtime:3.3.6")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.navigation:navigation-fragment:2.9.6")
    implementation("androidx.navigation:navigation-ui:2.9.6")
    implementation(files("libs/OutgoingViews_PurchaseHistory.jar-0.7.0-plain.jar"))
//    auto update
    implementation("com.github.supersu-man:apkupdater-library:v2.1.0")

    //captcha
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation("com.google.android.recaptcha:recaptcha:18.8.0")

//    QR reading
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.4")
    implementation("com.opencsv:opencsv:5.12.0")
//    Web
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("dev.gustavoavila:java-android-websocket-client:2.0.2")

//  Hilt Dependency injection
    implementation("com.google.dagger:hilt-android:2.57.2")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.57.2")
    implementation("androidx.activity:activity-compose:1.12.1")
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

//  Caching
//    implementation("com.google.guava:guava:33.4.0-android")
//  UI
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")
    implementation("com.github.Mohammad3125:KavehColorPicker:v1.0.0")
    implementation("uk.co.samuelwall:material-tap-target-prompt:3.3.2")
    implementation("com.squareup.okhttp3:okhttp-testing-support:3.14.9")

    // Chart and graph library
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.tradingview:lightweightcharts:4.0.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.paging:paging-guava:3.3.6")
//    implementation("com.google.android.gms:play-services-ads:23.4.0")

    androidTestImplementation(platform("androidx.compose:compose-bom:2025.12.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    annotationProcessor("com.google.dagger:hilt-compiler:2.57.2")

// Hilt Jetpack Integrations
//    implementation 'androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03'
    annotationProcessor("androidx.hilt:hilt-compiler:1.3.0")


//    Lombok
    //noinspection AnnotationProcessorOnCompilePath
    implementation("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.dagger:hilt-android-testing:2.57.2")
    testAnnotationProcessor("com.google.dagger:hilt-android-compiler:2.57.2")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.57.2")
    androidTestAnnotationProcessor("com.google.dagger:hilt-android-compiler:2.57.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}