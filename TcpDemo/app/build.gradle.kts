plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "edu.cs4730.tcpdemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.cs4730.tcpdemo"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation ("androidx.navigation:navigation-fragment:2.8.2")
    implementation ("androidx.navigation:navigation-ui:2.8.2")
}