plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.csakitheone.ipariminimap"
        minSdk = 25
        targetSdk = 34
        versionCode = 20
        versionName = "3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    namespace = "com.csakitheone.ipariminimap"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.android.gms:play-services-ads:22.4.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.firebase:firebase-database:20.2.2")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.CsakiTheOne:CsakisHelper:1.3")
    implementation("com.github.skydoves:transformationlayout:1.1.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.20")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}