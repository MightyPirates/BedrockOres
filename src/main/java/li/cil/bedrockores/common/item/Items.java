package li.cil.bedrockores.common.item;

import li.cil.bedrockores.common.block.Blocks;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class Items {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<Item> BEDROCK_MINER = ITEMS.register(Blocks.BEDROCK_MINER.getId().getPath(), () -> new BlockItem(Blocks.BEDROCK_MINER.get(), new Item.Properties()));

    // --------------------------------------------------------------------- //

    public static void initialize() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
