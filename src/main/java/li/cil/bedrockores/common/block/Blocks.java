package li.cil.bedrockores.common.block;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class Blocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<BedrockOreBlock> BEDROCK_ORE = BLOCKS.register("bedrock_ore", BedrockOreBlock::new);
    public static final RegistryObject<BedrockMinerBlock> BEDROCK_MINER = BLOCKS.register("bedrock_miner", BedrockMinerBlock::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
