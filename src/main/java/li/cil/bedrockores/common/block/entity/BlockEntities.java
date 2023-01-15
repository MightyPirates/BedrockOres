package li.cil.bedrockores.common.block.entity;

import li.cil.bedrockores.common.block.Blocks;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<BlockEntityType<BedrockOreBlockEntity>> BEDROCK_ORE = register(Blocks.BEDROCK_ORE, BedrockOreBlockEntity::new);
    public static final RegistryObject<BlockEntityType<BedrockOreMinerBlockEntity>> MINER = register(Blocks.BEDROCK_MINER, BedrockOreMinerBlockEntity::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        BLOCK_ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    // --------------------------------------------------------------------- //

    @SuppressWarnings("ConstantConditions") // .build(null) is fine
    private static <B extends Block, T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(final RegistryObject<B> block, final BlockEntityType.BlockEntitySupplier<T> factory) {
        return BLOCK_ENTITY_TYPES.register(block.getId().getPath(), () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }
}
