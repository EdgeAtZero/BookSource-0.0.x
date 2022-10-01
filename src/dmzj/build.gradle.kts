import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(project(":BookSource-api"))
    implementation("io.ktor:ktor-client-core:2.1.2")
    implementation("io.ktor:ktor-client-logging:2.1.2")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("it.skrape:skrapeit-html-parser:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-properties:1.4.0")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-okhttp:2.1.2")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    test {
        useJUnitPlatform()
    }
    jar {
        manifest {
            attributes(
                mapOf(
                    "Plugin-Class" to "io.github.edgeatzero.booksource.dmzj.DmzjBookSource"
                )
            )
        }
    }
}