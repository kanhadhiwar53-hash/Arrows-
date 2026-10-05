plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace="com.kanha.arrowflow"
    compileSdk=35
    defaultConfig {
        applicationId="com.kanha.arrowflow"
        minSdk=24
        targetSdk=35
        versionCode=2
        versionName="0.2.0"
    }
    signingConfigs {
        getByName("debug")
    }
    buildTypes {
        getByName("release") {
            // Test/distribution build: use Android's debug signing key so the APK
            // is directly installable. A Play Store release should use a private
            // production keystore instead.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility=JavaVersion.VERSION_17
        targetCompatibility=JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget="17" }
}
