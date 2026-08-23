/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.gametest;

import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import li.cil.bedrockores.common.block.entity.UnconfiguredOreCleanup;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import static li.cil.bedrockores.common.block.Blocks.BEDROCK_ORE;
import static li.cil.bedrockores.gametest.TestSupport.*;

public final class BedrockOreTests {
    private static final BlockPos ORE = new BlockPos(1, WORK_Y, 1);
    private static final int SETTLE_TICKS = 5;
    private static final int AMOUNT = 4;

    // --------------------------------------------------------------------- //

    public static void oreWithoutDataIsRemoved(final GameTestHelper helper) {
        helper.setBlock(ORE, BEDROCK_ORE.get());

        helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.AIR, ORE));
    }

    public static void oreWithDataSurvives(final GameTestHelper helper) {
        helper.setBlock(ORE, BEDROCK_ORE.get());

        final var bedrockOre = (BedrockOreBlockEntity) helper.getBlockEntity(ORE);
        bedrockOre.setOreBlockState(Blocks.IRON_ORE.defaultBlockState());
        bedrockOre.setAmount(AMOUNT);

        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertBlockPresent(BEDROCK_ORE.get(), ORE);

            final var stillThere = (BedrockOreBlockEntity) helper.getBlockEntity(ORE);
            assertTrue(helper, "expected the wrapped ore to be kept",
                stillThere.getOreBlockState().is(Blocks.IRON_ORE));
            assertEquals(helper, "expected the yield to be kept", AMOUNT, stillThere.getAmount().orElse(-1));

            helper.succeed();
        });
    }

    public static void worldGenRemovesOreWithoutData(final GameTestHelper helper) {
        helper.setBlock(ORE, BEDROCK_ORE.get());

        removeUnconfiguredFromChunk(helper);

        helper.assertBlockPresent(Blocks.AIR, ORE);
        helper.succeed();
    }

    public static void worldGenKeepsOreWithData(final GameTestHelper helper) {
        helper.setBlock(ORE, BEDROCK_ORE.get());

        final var bedrockOre = (BedrockOreBlockEntity) helper.getBlockEntity(ORE);
        bedrockOre.setOreBlockState(Blocks.IRON_ORE.defaultBlockState());
        bedrockOre.setAmount(AMOUNT);

        removeUnconfiguredFromChunk(helper);

        helper.assertBlockPresent(BEDROCK_ORE.get(), ORE);
        helper.succeed();
    }

    // --------------------------------------------------------------------- //

    private static void removeUnconfiguredFromChunk(final GameTestHelper helper) {
        final var level = helper.getLevel();
        UnconfiguredOreCleanup.removeUnconfiguredFromChunk(level, level.getChunkAt(helper.absolutePos(ORE)));
    }

    private BedrockOreTests() {
    }
}
