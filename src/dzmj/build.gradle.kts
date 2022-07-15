import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(project(":BookSource-api"))
    implementation("io.ktor:ktor-client-core:2.0.3")
    implementation("io.ktor:ktor-client-logging:2.0.3")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("it.skrape:skrapeit-html-parser:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.3.3")
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-okhttp:2.0.3")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    test {
        useJUnitPlatform()
    }
}

apply {
    from(rootDir.resolve("src").resolve("common.gradle.kts").absolutePath)
}
repositories {
    mavenCentral()
}
