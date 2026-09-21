plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "com.lifefit.os"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.lifefit.os"
        minSdk = 26
        targetSdk = 35
        versionCode = 12
        versionName = "1.2-dev"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.10.0")
    implementation("androidx.health.connect:connect-client:1.1.0")
}
