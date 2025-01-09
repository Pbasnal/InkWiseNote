import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
}

val properties = Properties()
file("secrets.properties").takeIf { it.exists() }?.apply {
    FileInputStream(this).use { properties.load(it) }
}

// Print all properties
println("Loaded properties:")
properties.forEach { key, value ->
    println("$key: $value")
}

android {
    namespace = "com.originb.inkwisenote"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.originb.inkwisenote"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val visionApiKey: String = System.getenv("VISION_API_KEY") ?: properties.getProperty("VISION_API_KEY")
        val visionApiEndpoint: String = System.getenv("VISION_API_ENDPOINT") ?: properties.getProperty("VISION_API_ENDPOINT")

        buildConfigField("String", "VISION_API_KEY", "\"$visionApiKey\"")
        buildConfigField("String", "VISION_API_ENDPOINT", "\"$visionApiEndpoint\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-base:18.5.0") // Ensure this is added
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.ui:ui-graphics-android:1.7.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("org.projectlombok:lombok:1.18.30")
    implementation("com.rmtheis:tess-two:9.1.0")
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    annotationProcessor("org.projectlombok:lombok:1.18.30")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    testImplementation("org.robolectric:robolectric:4.9")
}