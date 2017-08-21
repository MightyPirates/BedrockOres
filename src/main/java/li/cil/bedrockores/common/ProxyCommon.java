package li.cil.bedrockores.common;

import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.VeinConfig;
import li.cil.bedrockores.common.init.Blocks;
import li.cil.bedrockores.common.init.Items;
import li.cil.bedrockores.common.network.Network;
import li.cil.bedrockores.common.world.WorldGeneratorBedrockOre;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.function.Supplier;

/**
 * Takes care of common setup.
 */
public class ProxyCommon {
    public void onPreInit(final FMLPreInitializationEvent event) {
        Blocks.registerBlocks(this);
        Items.register(this);
    }

    public void onInit(final FMLInitializationEvent event) {
        Items.addRecipes();

        Network.INSTANCE.init();

        GameRegistry.registerWorldGenerator(WorldGeneratorBedrockOre.INSTANCE, 10);
    }

    public void onPostInit(final FMLPostInitializationEvent event) {
        VeinConfig.load();
    }

    // --------------------------------------------------------------------- //

    public Block registerBlock(final String name, final Supplier<Block> constructor, final Class<? extends TileEntity> tileEntity) {
        final Block block = constructor.get().
                setUnlocalizedName(Constants.MOD_ID + "." + name).
                setCreativeTab(CreativeTabs.MISC).
                setRegistryName(name);
        GameRegistry.register(block);
        GameRegistry.registerTileEntity(tileEntity, Constants.MOD_ID + ": " + name);

        return block;
    }

    public Item registerItem(final String name, final Supplier<Item> constructor) {
        final Item item = constructor.get().
                setUnlocalizedName(Constants.MOD_ID + "." + name).
                setCreativeTab(CreativeTabs.MISC).
                setRegistryName(name);
        GameRegistry.register(item);
        return item;
    }
}
