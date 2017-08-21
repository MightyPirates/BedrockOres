package li.cil.bedrockores.common.init;

import li.cil.bedrockores.common.ProxyCommon;
import li.cil.bedrockores.common.block.BlockBedrockMiner;
import li.cil.bedrockores.common.block.BlockBedrockOre;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.tileentity.TileEntityBedrockMiner;
import li.cil.bedrockores.common.tileentity.TileEntityBedrockOre;
import net.minecraft.block.Block;

public final class Blocks {
    public static Block bedrockOre;
    public static Block bedrockMiner;

    public static void registerBlocks(final ProxyCommon proxy) {
        bedrockOre = proxy.registerBlock(Constants.NAME_BEDROCK_ORE, BlockBedrockOre::new, TileEntityBedrockOre.class);
        bedrockMiner = proxy.registerBlock(Constants.NAME_BEDROCK_MINER, BlockBedrockMiner::new, TileEntityBedrockMiner.class);
    }
}
