package li.cil.bedrockores.common.block;

import com.mojang.serialization.MapCodec;
import dev.architectury.event.EventResult;
import dev.architectury.utils.value.IntValue;
import li.cil.bedrockores.common.RegistryKeys;
import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import li.cil.bedrockores.common.block.entity.BlockEntities;
import li.cil.bedrockores.common.config.Settings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;

/**
 * This block itself is configured like bedrock; however, it forwards pretty
 * much everything logic related to the underlying ore block, such as hardness
 * checks, harvesting (drops). It only does slightly custom rendering (a mask
 * over the underlying ore's model) and not actually breaking when harvested,
 * but instead reducing the remaining amount by one.
 * <p>
 * The forwarding methods that exist only as loader extensions live in the
 * NeoForge subclass; the ones below are vanilla and work on both platforms.
 */
public class BedrockOreBlock extends BaseEntityBlock {
    public static final MapCodec<BedrockOreBlock> CODEC = MapCodec.unit(BedrockOreBlockFactory::create);

    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

    // --------------------------------------------------------------------- //

    public BedrockOreBlock() {
        super(Properties.of()
                .setId(RegistryKeys.block(RegistryKeys.BEDROCK_ORE))
                .mapColor(MapColor.STONE)
                .strength(-1F, 3600000)
                .noLootTable()
                .lightLevel(state -> state.getValue(LIGHT))
                .isValidSpawn((state, reader, pos, entity) -> false));
        registerDefaultState(defaultBlockState().setValue(LIGHT, 0));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT);
    }

    // --------------------------------------------------------------------- //

    public static EventResult onBlockBreak(final Level level, final BlockPos pos, final BlockState state, final ServerPlayer player, @Nullable final IntValue xp) {
        if (!state.is(Blocks.BEDROCK_ORE.get())) {
            return EventResult.pass();
        }

        if (player.isCreative()) {
            return EventResult.pass();
        }

        if (!Settings.allowPlayerMining.get()) {
            resync(level, pos, player);
            return EventResult.interruptFalse();
        }

        if (level.getBlockEntity(pos) instanceof final BedrockOreBlockEntity bedrockOre) {
            final var oreBlockState = bedrockOre.getOreBlockState();

            final var tool = player.getMainHandItem().copy();
            final var canHarvest = player.hasCorrectToolForDrops(oreBlockState);

            // Ignore result, expect drops to be handled by underlying ore.
            bedrockOre.extract();

            oreBlockState.getBlock().playerWillDestroy(level, pos, oreBlockState, player);
            if (canHarvest) {
                oreBlockState.getBlock().playerDestroy(level, player, pos, oreBlockState, null, tool);
            }

            // The vanilla break is cancelled below, so its effects never fire; play them for the
            // wrapped ore instead. Using the ore's state also gets the right particle texture.
            if (!oreBlockState.isAir()) {
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(oreBlockState));
            }
        }

        // `extract` turns the block into plain bedrock once it runs out; otherwise the block
        // stays put, so the client has to be told its predicted break did not happen.
        resync(level, pos, player);

        return EventResult.interruptFalse();
    }

    private static void resync(final Level level, final BlockPos pos, final ServerPlayer player) {
        player.connection.send(new ClientboundBlockUpdatePacket(level, pos));

        final var blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            final var packet = blockEntity.getUpdatePacket();
            if (packet != null) {
                player.connection.send(packet);
            }
        }
    }

    // --------------------------------------------------------------------- //
    // BaseEntityBlock

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return BlockEntities.BEDROCK_ORE.get().create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    // --------------------------------------------------------------------- //
    // BlockBehaviour


    @Override
    public float getDestroyProgress(final BlockState state, final Player player, final BlockGetter level, final BlockPos pos) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.getDestroyProgress(player, level, pos);
        } else {
            return super.getDestroyProgress(state, player, level, pos);
        }
    }

    @Override
    public ItemStack getCloneItemStack(final LevelReader level, final BlockPos pos, final BlockState state, final boolean includeData) {
        final var ore = getOreBlockState(level.getBlockEntity(pos));
        if (ore != null) {
            return ore.getCloneItemStack(level, pos, includeData);
        } else {
            return super.getCloneItemStack(level, pos, state, includeData);
        }
    }

    // --------------------------------------------------------------------- //

    @Nullable
    protected static BlockState getOreBlockState(@Nullable final BlockEntity blockEntity) {
        if (blockEntity instanceof final BedrockOreBlockEntity bedrockOre) {
            return bedrockOre.getOreBlockState();
        }
        return null;
    }
}
