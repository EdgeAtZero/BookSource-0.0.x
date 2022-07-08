pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        kotlin("multiplatform") version extra["kotlin.version"] as String
        kotlin("plugin.serialization") version extra["kotlin.version"] as String
    }
}

rootProject.name = "Book Source"