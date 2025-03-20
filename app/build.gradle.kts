import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
}

val properties = Properties()
file("secrets.properties").takeIf { it.exists() }?.apply {
    FileInputStream(this).use { properties.load(it) }
}

android {
    namespace = "com.originb.inkwisenote2"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.originb.inkwisenote2"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "app_bundle_release"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val visionApiKey: String = System.getenv("VISION_API_KEY")
            ?: properties.getProperty("VISION_API_KEY")
        val visionApiEndpoint: String = System.getenv("VISION_API_ENDPOINT")
            ?: properties.getProperty("VISION_API_ENDPOINT")

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
    testOptions {
        animationsDisabled = false
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
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
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("io.noties.markwon:core:4.6.2")
    implementation("org.greenrobot:eventbus:3.3.1")

    implementation("androidx.lifecycle:lifecycle-livedata-core:2.6.1") // or use the latest version available
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.1")

    annotationProcessor("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    testImplementation("org.robolectric:robolectric:4.9")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestUtil("androidx.test:orchestrator:1.4.2")
}