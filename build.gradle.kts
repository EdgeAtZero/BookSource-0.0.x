allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
}