plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}
android {
    namespace = "com.agrawalmedical.expiry"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.agrawalmedical.expiry"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    // Firebase SDKs
    dependencies {
        implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
        implementation("com.google.firebase:firebase-auth")
        implementation("com.google.firebase:firebase-firestore")
    }
}
