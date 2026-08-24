/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.world;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BedrockOrePlacements {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final TagKey<PlacedFeature> OVERWORLD_VEINS = TagKey.create(Registries.PLACED_FEATURE, id("overworld_veins"));

    public static final ResourceKey<PlacedFeature> UNCONFIGURED_ORE_CLEANUP = key("unconfigured_ore_cleanup");

    // --------------------------------------------------------------------- //

    public static void checkOverworldVeins(final MinecraftServer server) {
        final var veins = server.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).get(OVERWORLD_VEINS);
        if (veins.isPresent() && veins.get().size() > 0) {
            return;
        }

        LOGGER.error("""
            No bedrock ore veins will generate: the tag {} is empty or was not loaded.
            A data pack listing a placed feature that does not exist makes the game discard the entire tag; \
            look for a preceding "Couldn't load tag" error naming the offending entry and data pack. \
            Data packs extending this tag should mark entries that may be absent as {}.""",
            OVERWORLD_VEINS.location(), "{\"id\": \"...\", \"required\": false}");
    }

    // --------------------------------------------------------------------- //

    private static ResourceKey<PlacedFeature> key(final String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, id(name));
    }

    private static Identifier id(final String name) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, name);
    }

    private BedrockOrePlacements() {
    }
}
