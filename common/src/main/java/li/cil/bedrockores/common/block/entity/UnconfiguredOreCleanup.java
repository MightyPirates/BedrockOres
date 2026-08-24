/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.block.entity;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static li.cil.bedrockores.common.block.Blocks.BEDROCK_ORE;

public final class UnconfiguredOreCleanup {
    private static final int MAX_REMOVALS_PER_TICK = 64;

    private static final Map<ServerLevel, List<BlockPos>> PENDING = new WeakHashMap<>();

    // --------------------------------------------------------------------- //

    public static void initialize() {
        TickEvent.SERVER_LEVEL_POST.register(UnconfiguredOreCleanup::processPending);
        LifecycleEvent.SERVER_LEVEL_UNLOAD.register(PENDING::remove);
    }

    public static int removeUnconfiguredFromChunk(final LevelAccessor level, final ChunkAccess chunk) {
        final var sections = chunk.getSections();
        final var origin = chunk.getPos().getWorldPosition();
        final var pos = new BlockPos.MutableBlockPos();

        var removed = 0;
        for (var sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            final var section = sections[sectionIndex];
            if (!section.maybeHas(UnconfiguredOreCleanup::isBedrockOre)) {
                continue;
            }

            final var sectionY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(sectionIndex));
            for (var y = 0; y < SectionPos.SECTION_SIZE; y++) {
                for (var z = 0; z < SectionPos.SECTION_SIZE; z++) {
                    for (var x = 0; x < SectionPos.SECTION_SIZE; x++) {
                        if (!isBedrockOre(section.getBlockState(x, y, z))) {
                            continue;
                        }

                        pos.set(origin.getX() + x, sectionY + y, origin.getZ() + z);
                        if (isUnconfigured(chunk, pos)) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_NONE);
                            removed++;
                        }
                    }
                }
            }
        }

        return removed;
    }

    public static void schedule(final ServerLevel level, final BlockPos pos) {
        PENDING.computeIfAbsent(level, unused -> new ArrayList<>()).add(pos.immutable());
    }

    // --------------------------------------------------------------------- //

    private static void processPending(final ServerLevel level) {
        final var positions = PENDING.get(level);
        if (positions == null) {
            return;
        }

        var removed = 0;
        while (removed < MAX_REMOVALS_PER_TICK && !positions.isEmpty()) {
            final var pos = positions.removeLast();
            if (!isLoaded(level, pos)) {
                continue;
            }
            if (level.getBlockEntity(pos) instanceof final BedrockOreBlockEntity bedrockOre && bedrockOre.isUnconfigured()) {
                level.removeBlock(pos, false);
                level.getChunkSource().getLightEngine().checkBlock(pos);
                removed++;
            }
        }

        if (positions.isEmpty()) {
            PENDING.remove(level);
        }
    }

    private static boolean isBedrockOre(final BlockState state) {
        return state.is(BEDROCK_ORE.get());
    }

    private static boolean isUnconfigured(final ChunkAccess chunk, final BlockPos pos) {
        return !(chunk.getBlockEntity(pos) instanceof final BedrockOreBlockEntity bedrockOre) || bedrockOre.isUnconfigured();
    }

    private static boolean isLoaded(final ServerLevel level, final BlockPos pos) {
        return level.getChunkSource().hasChunk(
            SectionPos.blockToSectionCoord(pos.getX()),
            SectionPos.blockToSectionCoord(pos.getZ()));
    }

    // --------------------------------------------------------------------- //

    private UnconfiguredOreCleanup() {
    }
}
