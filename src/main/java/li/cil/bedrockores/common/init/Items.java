package li.cil.bedrockores.common.init;

import li.cil.bedrockores.common.ProxyCommon;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

public final class Items {
    public static Item bedrockMiner;

    public static void register(final ProxyCommon proxy) {
        bedrockMiner = proxy.registerItem(Constants.NAME_BEDROCK_MINER, () -> new ItemBlock(Blocks.bedrockMiner));
    }

    public static void addRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(bedrockMiner),
                "ITI",
                "RPR",
                "ODO",
                'I', "ingotIron",
                'T', net.minecraft.init.Blocks.DROPPER,
                'R', "blockRedstone",
                'P', net.minecraft.init.Blocks.PISTON,
                'D', "gemDiamond",
                'O', "obsidian"));
    }

    // --------------------------------------------------------------------- //

    private Items() {
    }
}
