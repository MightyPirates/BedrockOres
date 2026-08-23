# Bedrock Ores

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

To change an ore, override that path from a data pack in `<world>/datapacks/`. Adding a new ore also needs a matching
`worldgen/placed_feature` entry and, on NeoForge, listing it in [`data/bedrockores/neoforge/biome_modifier/overworld_minecraft.json`][neoforge-biome-modifiers].
On Fabric the set of veins added to biomes is currently hardcoded.

## License / Use in Modpacks

This mod is [licensed under the **MIT license**](LICENSE). All **assets are public domain**, unless otherwise stated;
all are free to be distributed as long as the license / source credits are kept. This means you can use this mod in any
mod pack **as you please**. I'd be happy to hear about you using it, though, just out of curiosity.

[worldgen-feature]: common/src/main/resources/data/bedrockores/worldgen/configured_feature/overworld_iron.json
[neoforge-biome-modifiers]: neoforge/src/main/resources/data/bedrockores/neoforge/biome_modifier/overworld_minecraft.json
