package li.cil.bedrockores.common.block.entity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.bedrockores.common.block.Blocks;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Constants.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<BlockEntityType<BedrockOreBlockEntity>> BEDROCK_ORE = register("bedrock_ore", Blocks.BEDROCK_ORE, BedrockOreBlockEntityFactory::create);
    public static final RegistrySupplier<BlockEntityType<BedrockOreMinerBlockEntity>> MINER = register("bedrock_miner", Blocks.BEDROCK_MINER, BedrockOreMinerBlockEntity::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        BLOCK_ENTITY_TYPES.register();
    }

    // --------------------------------------------------------------------- //

    @SuppressWarnings("DataFlowIssue") // .build(null) is fine
    private static <B extends Block, T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(final String name, final RegistrySupplier<B> block, final BlockEntityType.BlockEntitySupplier<T> factory) {
        return BLOCK_ENTITY_TYPES.register(name, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }
}
