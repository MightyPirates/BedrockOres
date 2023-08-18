plugins {
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("idea")
    id("eclipse")
}

fun getGitRef(): String {
    return providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim()
}

val modId: String by extra
val minecraftVersion: String by extra
val forgeVersion: String by extra
val modVersion: String by extra
val mavenGroup: String by extra

version = "${modVersion}+${getGitRef()}"
group = mavenGroup
base {
    archivesName.set("${modId}-MC${minecraftVersion}")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "utf-8"
}

repositories {
    maven {
        url = uri("https://cursemaven.com")
        content { includeGroup("curse.maven") }
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:${minecraftVersion}-${forgeVersion}")

    // Just for in-dev convenience. Mod doesn't use any JEI APIs.
    runtimeOnly(fg.deobf("curse.maven:jei-238222:4690097"))
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

        create("data") {
            workingDirectory(file("run"))

            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "info")

            args("--mod", "bedrockores", "--all", "--output", file("src/generated/resources/"), "--existing", file("src/main/resources"))

            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }
    }
}

tasks.register<Copy>("copyGeneratedResources") {
    from("src/generated")
    into("src/main")
    exclude("resources/.cache")
}

tasks.withType<ProcessResources> {
    inputs.property("version", modVersion)

    filesMatching("META-INF/mods.toml") {
        expand(mapOf("version" to modVersion))
    }
}

tasks.withType<Jar> {
    finalizedBy("reobfJar")

    manifest {
        attributes(mapOf(
                "Specification-Title" to "bedrockores",
                "Specification-Vendor" to "Sangar",
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to version,
                "Implementation-Vendor" to "Sangar",
        ))
    }
}

idea {
    module {
        for (exclude in arrayOf("out", "logs")) {
            excludeDirs.add(file(exclude))
        }
    }
}
