import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("kapt")
}

val properties = Properties()
file("secrets.properties").takeIf { it.exists() }?.apply {
    FileInputStream(this).use { properties.load(it) }
}

android {
    namespace = "com.originb.inkwisenote2"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
        compose = true
    }

    configurations.all {
        resolutionStrategy {
            force("org.projectlombok:lombok:1.18.30")
        }
    }

    defaultConfig {
        applicationId = "com.originb.inkwisenote2"
        minSdk = 26
        targetSdk = 34
        versionCode = 6
        versionName = "Release 0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["lombok.config.stopBubbling"] = "true"
            }
        }

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions {
        animationsDisabled = false
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-base:18.5.0") // Ensure this is added
    implementation("com.google.mlkit:digital-ink-recognition:18.0.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("org.projectlombok:lombok:1.18.30")
    implementation("com.rmtheis:tess-two:9.1.0")
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    implementation("io.noties.markwon:core:4.6.2")
    implementation("org.greenrobot:eventbus:3.3.1")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.9.22"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    implementation("io.insert-koin:koin-android:3.1.6")
    implementation("io.insert-koin:koin-android-compat:3.1.6")
    implementation("io.insert-koin:koin-androidx-workmanager:3.1.6")

    implementation("androidx.lifecycle:lifecycle-livedata-core:2.6.1") // or use the latest version available
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime:2.6.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.core:core-ktx:1.17.0")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    // Compose testing
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    kapt("org.projectlombok:lombok:1.18.30")
    compileOnly("org.projectlombok:lombok:1.18.30")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    kapt("androidx.room:room-compiler:2.8.4")

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