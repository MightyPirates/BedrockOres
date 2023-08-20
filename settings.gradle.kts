pluginManagement {
    repositories {
        maven("https://maven.minecraftforge.net") {
            content { includeGroupByRegex("net\\.minecraftforge.*") }
        }
        maven("https://maven.parchmentmc.org") {
            content { includeGroupByRegex("org\\.parchmentmc.*") }
        }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

val modId: String by settings
rootProject.name = modId
