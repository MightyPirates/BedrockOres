/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.world;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public final class BedrockOrePlacements {
    public static final List<ResourceKey<PlacedFeature>> OVERWORLD = List.of(
            key("overworld_coal"),
            key("overworld_copper"),
            key("overworld_diamond"),
            key("overworld_emerald"),
            key("overworld_gold"),
            key("overworld_iron"),
            key("overworld_lapis"),
            key("overworld_redstone")
    );

    public static final ResourceKey<PlacedFeature> UNCONFIGURED_ORE_CLEANUP = key("unconfigured_ore_cleanup");

    private static ResourceKey<PlacedFeature> key(final String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    private BedrockOrePlacements() {
    }
}
