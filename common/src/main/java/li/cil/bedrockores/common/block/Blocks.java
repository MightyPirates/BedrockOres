/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public final class Blocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Constants.MOD_ID, Registries.BLOCK);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<BedrockOreBlock> BEDROCK_ORE = BLOCKS.register("bedrock_ore", BedrockOreBlockFactory::create);
    public static final RegistrySupplier<BedrockMinerBlock> BEDROCK_MINER = BLOCKS.register("bedrock_miner", BedrockMinerBlock::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        BLOCKS.register();
    }
}
