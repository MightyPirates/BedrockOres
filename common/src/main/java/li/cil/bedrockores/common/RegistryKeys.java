/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class RegistryKeys {
    public static final String BEDROCK_ORE = "bedrock_ore";
    public static final String BEDROCK_MINER = "bedrock_miner";

    // --------------------------------------------------------------------- //

    public static ResourceKey<Block> block(final String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    public static ResourceKey<Item> item(final String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    private RegistryKeys() {
    }
}
