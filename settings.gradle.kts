pluginManagement {
    repositories {
        exclusiveContent {
            forRepository { maven("https://maven.minecraftforge.net") }
            filter { includeGroupByRegex("net\\.minecraftforge.*") }
        }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

val modId: String by settings
rootProject.name = modId
