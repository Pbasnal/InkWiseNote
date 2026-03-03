// Kotlin compiler's JavaVersion parser does not support JDK 25's version string ("25.0.2"); require JDK 21.
val javaVersion = System.getProperty("java.version") ?: ""
if (javaVersion.startsWith("25")) {
    throw GradleException(
        "This build requires JDK 21. Current JVM is JDK 25 (reported as: $javaVersion). Install JDK 21, set JAVA_HOME to it, and run the build again."
    )
}

// Use JAVA_HOME for JDK path so AGP's JdkImageTransform finds jlink (avoids /app/jbr from CI/IDE)
System.getenv("JAVA_HOME")?.let { javaHome ->
    val jlink = java.io.File(javaHome, "bin/jlink")
    if (jlink.exists()) {
        System.setProperty("org.gradle.java.home", javaHome)
    }
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "inkwisenote2"
include(":androidApp")
include(":shared")
include(":desktopApp")
