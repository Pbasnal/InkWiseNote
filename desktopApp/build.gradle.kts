plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

group = "com.originb.inkwisenote2"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
}

compose.desktop {
    application {
        mainClass = "com.originb.inkwisenote2.desktop.MainKt"
        nativeDistributions {
            packageName = "InkWiseNote"
            packageVersion = version.toString()
            description = "InkWiseNote desktop application"
            vendor = "Origin B"
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Rpm
            )
            windows {
                menuGroup = "InkWiseNote"
                shortcut = true
            }
            linux {
                shortcut = true
            }
        }
    }
}
