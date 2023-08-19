pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://maven.minecraftforge.net/") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val minecraftVersion = "1.20.1"
            val forgeVersion = "47.1.0"

            plugin("forgegradle", "net.minecraftforge.gradle").version("[6.0,6.2)")
            plugin("spotless", "com.diffplug.spotless").version("6.20.0")

            version("minecraft", minecraftVersion)
            library("minecraft", "com.mojang", "minecraft").versionRef("minecraft")

            version("forge", forgeVersion)
            library("forge", "net.minecraftforge", "forge").version("$minecraftVersion-$forgeVersion")

            library("jei", "curse.maven", "jei-238222").version("4690097")
        }
    }
}

val modId: String by settings
rootProject.name = modId
