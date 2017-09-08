package li.cil.bedrockores.common;

import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.Settings;
import li.cil.bedrockores.common.config.OreConfigManager;
import li.cil.bedrockores.common.init.Blocks;
import li.cil.bedrockores.common.init.Items;
import li.cil.bedrockores.common.network.Network;
import li.cil.bedrockores.common.sound.Sounds;
import li.cil.bedrockores.common.world.Retrogen;
import li.cil.bedrockores.common.world.WorldGeneratorBedrockOre;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
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
        Sounds.INSTANCE.init();
        Network.INSTANCE.init();

        GameRegistry.registerWorldGenerator(WorldGeneratorBedrockOre.INSTANCE, Settings.worldGeneratorWeight);

        MinecraftForge.EVENT_BUS.register(Retrogen.INSTANCE);
    }

    public void onPostInit(final FMLPostInitializationEvent event) {
        OreConfigManager.INSTANCE.load();
    }

    // --------------------------------------------------------------------- //

    public Block registerBlock(final String name, final Supplier<Block> constructor, final Class<? extends TileEntity> tileEntity) {
        final Block block = constructor.get().
                setUnlocalizedName(Constants.MOD_ID + '.' + name).
                setCreativeTab(CreativeTabs.MISC).
                setRegistryName(name);
        GameRegistry.register(block);
        GameRegistry.registerTileEntityWithAlternatives(tileEntity, Constants.MOD_ID + ':' + name,
                                                        Constants.MOD_ID + ": " + name); // Herp derp, typo in early version -.-

        return block;
    }

    public Item registerItem(final String name, final Supplier<Item> constructor) {
        final Item item = constructor.get().
                setUnlocalizedName(Constants.MOD_ID + '.' + name).
                setCreativeTab(CreativeTabs.MISC).
                setRegistryName(name);
        GameRegistry.register(item);
        return item;
    }
}
