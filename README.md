# Bedrock Ores

[![build](https://img.shields.io/github/actions/workflow/status/MightyPirates/BedrockOres/build.yml?label=build)](https://github.com/MightyPirates/BedrockOres/actions/workflows/build.yml)
[![game tests](https://img.shields.io/github/actions/workflow/status/MightyPirates/BedrockOres/test-report.yml?label=game%20tests)](https://github.com/MightyPirates/BedrockOres/actions/workflows/test-report.yml)
[![curseforge](https://img.shields.io/curseforge/dt/275083?label=curseforge&color=f16436&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/bedrockores)
[![modrinth](https://img.shields.io/modrinth/dt/bedrockores?label=modrinth&color=1bd96a&logo=modrinth&logoColor=white)](https://modrinth.com/mod/bedrockores)
![loaders](https://img.shields.io/badge/loaders-Fabric%20%7C%20NeoForge-blueviolet)

Bedrock Ores is a Minecraft mod that adds larger stationary ore deposits that can be harvested over a longer amount of
time, encouraging mining outposts. It is somewhat inspired by the way resources are gathered in Factorio.

## Configuration

The miner and general settings can be configured in `config/bedrockores-common.toml`.

Ore veins are defined by data pack files. Each ore has a configured feature naming the block and the yield range, and a
placed feature controlling rarity and height. For example, [`data/bedrockores/worldgen/configured_feature/overworld_iron.json`][worldgen-feature]:

```json
{
    "type": "bedrockores:bedrock_ore",
    "config": {
        "ore": {
            "Name": "minecraft:iron_ore"
        },
        "amount": {
            "type": "minecraft:uniform",
            "min_inclusive": 200,
            "max_inclusive": 300
        }
    }
}
```

To change an ore, override that path from a data pack in `<world>/datapacks/`. To add one, ship a configured feature
plus a matching `worldgen/placed_feature` entry, and list that placed feature in the `bedrockores:overworld_veins`
tag ([`data/bedrockores/tags/worldgen/placed_feature/overworld_veins.json`][vein-tag]).

## License / Use in Modpacks

This mod is [licensed under the **MIT license**](LICENSE). All **assets are public domain**, unless otherwise stated;
all are free to be distributed as long as the license / source credits are kept. This means you can use this mod in any
mod pack **as you please**. I'd be happy to hear about you using it, though, just out of curiosity.

[worldgen-feature]: common/src/main/resources/data/bedrockores/worldgen/configured_feature/overworld_iron.json
[vein-tag]: common/src/main/resources/data/bedrockores/tags/worldgen/placed_feature/overworld_veins.json
