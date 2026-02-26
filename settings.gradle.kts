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
