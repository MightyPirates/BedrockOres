package li.cil.bedrockores.common.init;

import li.cil.bedrockores.common.block.BlockBedrockMiner;
import li.cil.bedrockores.common.block.BlockBedrockOre;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.tileentity.TileEntityBedrockMiner;
import li.cil.bedrockores.common.tileentity.TileEntityBedrockOre;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@GameRegistry.ObjectHolder(Constants.MOD_ID)
public final class Blocks {
    @GameRegistry.ObjectHolder(Constants.NAME_BEDROCK_ORE)
    public static final Block bedrockOre = null;
    @GameRegistry.ObjectHolder(Constants.NAME_BEDROCK_MINER)
    public static final Block bedrockMiner = null;

    public static void register(final IForgeRegistry<Block> registry) {
        registerBlock(registry, new BlockBedrockOre(), Constants.NAME_BEDROCK_ORE, TileEntityBedrockOre.class);
        registerBlock(registry, new BlockBedrockMiner(), Constants.NAME_BEDROCK_MINER, TileEntityBedrockMiner.class);
    }

    // --------------------------------------------------------------------- //

    private static void registerBlock(final IForgeRegistry<Block> registry, final Block block, final String name, final Class<? extends TileEntity> tileEntity) {
        registry.register(block.
                setHardness(5).
                setResistance(10).
                setUnlocalizedName(Constants.MOD_ID + "." + name).
                setCreativeTab(CreativeTabs.MISC).
                setRegistryName(name));

        GameRegistry.registerTileEntity(tileEntity, Constants.MOD_ID + ": " + name);
    }
}
