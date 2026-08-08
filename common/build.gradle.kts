val enabledPlatforms: String by project
val modId: String by project

architectury {
    common(enabledPlatforms.split(","))
}

loom {
    accessWidenerPath.set(file("src/main/resources/${modId}.accesswidener"))
}

repositories {
    exclusiveContent {
        forRepository { maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") }
        filter { includeGroup("fuzs.forgeconfigapiport") }
    }
}

dependencies {
    modImplementation(libs.fabric.loader)
    modApi(libs.architectury.api)

    // Only for the `net.neoforged.neoforge.common.ModConfigSpec` classes, so the config spec can
    // live in common. NeoForge provides them natively, Fabric gets them from Forge Config API Port.
    modCompileOnly(libs.fabric.forgeConfigPort)
}
