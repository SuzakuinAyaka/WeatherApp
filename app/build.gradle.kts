import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val amapProperties = Properties().apply {
    val propertiesFile = rootProject.file("amap.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}

fun readAmapProperty(key: String, fallback: String): String {
    return amapProperties.getProperty(key)?.takeIf { it.isNotBlank() } ?: fallback
}

android {
    namespace = "com.dengyy.weatherapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dengyy.weatherapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "AMAP_WEATHER_BASE_URL",
            "\"${readAmapProperty("AMAP_WEATHER_BASE_URL", "https://restapi.amap.com/v3/")}\""
        )
        buildConfigField(
            "String",
            "AMAP_WEB_SERVICE_KEY",
            "\"${readAmapProperty("AMAP_WEB_SERVICE_KEY", "")}\""
        )
        buildConfigField("String", "AMAP_WEATHER_PATH", "\"weather/weatherInfo\"")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.swiperefreshlayout)
    implementation(libs.viewpager2)
    implementation(libs.okhttp)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
