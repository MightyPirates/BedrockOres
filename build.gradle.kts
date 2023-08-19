plugins {
    java
    idea
    eclipse
    alias(libs.plugins.forgegradle)
}

fun getGitRef(): String {
    return providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim()
}

val modId: String by project
val mavenGroup: String by project
val modVersion: String by project
val minecraftVersion: String = libs.versions.minecraft.get()
val forgeVersion: String = libs.versions.forge.get()

version = "${modVersion}+${getGitRef()}"
group = mavenGroup
base {
    archivesName.set("${modId}-MC${minecraftVersion}")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

repositories {
    maven {
        url = uri("https://cursemaven.com")
        content { includeGroup("curse.maven") }
    }
}

dependencies {
    minecraft(libs.forge)

    // Just for in-dev convenience. Mod doesn't use any JEI APIs.
    runtimeOnly(fg.deobf(libs.jei.get()))
}

minecraft {
    mappings("official", minecraftVersion)

    runs {
        create("client") {
            workingDirectory(file("run"))

            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "info")

            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("server") {
            workingDirectory(file("run"))

            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "info")

            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "utf-8"
    }

    processResources {
        val properties = mapOf(
                "version" to modVersion,
                "minecraftVersion" to minecraftVersion,
                "loaderVersion" to forgeVersion.split(".").first(),
                "forgeVersion" to forgeVersion
        )
        inputs.properties(properties)
        filesMatching("META-INF/mods.toml") {
            expand(properties)
        }
    }

    jar {
        finalizedBy("reobfJar")

        manifest {
            attributes(mapOf(
                    "Specification-Title" to modId,
                    "Specification-Version" to "1",
                    "Specification-Vendor" to "Sangar",
                    "Implementation-Title" to modId,
                    "Implementation-Version" to version,
                    "Implementation-Vendor" to "Sangar",
            ))
        }
    }
}

idea {
    module {
        for (exclude in arrayOf("out", "logs")) {
            excludeDirs.add(file(exclude))
        }
    }
}
