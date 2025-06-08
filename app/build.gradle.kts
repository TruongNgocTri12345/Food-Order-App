plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.food_order_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.food_order_app"
        minSdk = 24
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation(libs.firebase.auth)
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.google.firebase:firebase-storage:20.2.0")
    implementation(libs.firebase.database)
    implementation("com.sun.mail:android-mail:1.6.2")
    implementation ("com.sun.mail:android-activation:1.6.2")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.android.gms:play-services-auth:20.0.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.google.firebase:firebase-bom:31.5.0")
    implementation ("com.google.firebase:firebase-auth:21.0.1")
    implementation ("com.google.firebase:firebase-database:20.2.3")    // Firebase Realtime Database
    implementation ("com.facebook.android:facebook-login:12.1.0")


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}


