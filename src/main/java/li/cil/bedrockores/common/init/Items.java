package li.cil.bedrockores.common.init;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@GameRegistry.ObjectHolder(Constants.MOD_ID)
public final class Items {
    @GameRegistry.ObjectHolder(Constants.NAME_BEDROCK_MINER)
    public static final Item bedrockMiner = null;

    public static void register(final IForgeRegistry<Item> registry) {
        registerItem(registry, new ItemBlock(Blocks.bedrockMiner), Constants.NAME_BEDROCK_MINER);
    }

    private static void registerItem(final IForgeRegistry<Item> registry, final Item item, final String name) {
        registry.register(item.
                setUnlocalizedName(Constants.MOD_ID + "." + name).
                setCreativeTab(CreativeTabs.MISC).
                setRegistryName(name));
    }
}
