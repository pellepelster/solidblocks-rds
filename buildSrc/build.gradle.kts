plugins {
    `kotlin-dsl`
    id("net.nemerosa.versioning") version "2.14.0"
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("gradle.plugin.net.nemerosa:versioning:2.14.0")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:10.2.0")
}
