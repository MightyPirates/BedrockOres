package li.cil.bedrockores.common.item;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.bedrockores.common.block.Blocks;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class Items {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Constants.MOD_ID, Registries.ITEM);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<Item> BEDROCK_MINER = ITEMS.register("bedrock_miner", () -> new BlockItem(Blocks.BEDROCK_MINER.get(), new Item.Properties()));

    // --------------------------------------------------------------------- //

    public static void initialize() {
        ITEMS.register();

        CreativeTabRegistry.append(CreativeModeTabs.FUNCTIONAL_BLOCKS, BEDROCK_MINER);
    }
}
