import org.gradle.kotlin.dsl.support.listFilesOrdered

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("multiplatform") version extra["kotlin.version"] as String
        kotlin("plugin.serialization") version extra["kotlin.version"] as String
    }
}

rootProject.name = "BookSource"

include(":BookSource-api")

rootDir.resolve("src").listFilesOrdered { it.isDirectory }.forEach {
    val project = ":${it.name}"
    include(project)
    project(project).projectDir = it
}