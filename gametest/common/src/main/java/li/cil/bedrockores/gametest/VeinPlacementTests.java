/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.gametest;

import li.cil.bedrockores.common.world.AboveWorldBottomPlacement;
import li.cil.bedrockores.common.world.BedrockOrePlacements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacementContext;

import java.util.Optional;
import java.util.stream.Collectors;

import static li.cil.bedrockores.gametest.TestSupport.assertEquals;
import static li.cil.bedrockores.gametest.TestSupport.assertTrue;

public final class VeinPlacementTests {
    private static final int VEIN_COUNT = 8;
    private static final int VEIN_MAX_OFFSET = 5;
    private static final int SAMPLE_COUNT = 32;

    // --------------------------------------------------------------------- //

    public static void veinTagIsPopulated(final GameTestHelper helper) {
        final var placedFeatures = helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        final var veins = placedFeatures.getTag(BedrockOrePlacements.OVERWORLD_VEINS).orElseThrow();

        assertEquals(helper, "expected the vein tag to resolve", VEIN_COUNT, veins.size());

        helper.succeed();
    }

    public static void overworldBiomesGetVeins(final GameTestHelper helper) {
        final var registries = helper.getLevel().registryAccess();
        final var placedFeatures = registries.registryOrThrow(Registries.PLACED_FEATURE);
        final var veins = placedFeatures.getTag(BedrockOrePlacements.OVERWORLD_VEINS).orElseThrow();

        final var plains = registries.registryOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        final var ores = plains.getGenerationSettings().features()
            .get(GenerationStep.Decoration.UNDERGROUND_ORES.ordinal())
            .stream()
            .map(Holder::unwrapKey)
            .flatMap(Optional::stream)
            .collect(Collectors.toSet());

        for (final var vein : veins) {
            final var key = vein.unwrapKey().orElseThrow();
            assertTrue(helper, "expected " + key.location() + " to be added to plains", ores.contains(key));
        }

        helper.succeed();
    }

    public static void veinsResolveIntoBedrockBand(final GameTestHelper helper) {
        final var level = helper.getLevel();
        final var context = new PlacementContext(level, level.getChunkSource().getGenerator(), Optional.empty());
        final var random = RandomSource.create(0);
        final var origin = helper.absolutePos(BlockPos.ZERO);
        final var bottom = level.getMinBuildHeight();

        final var placedFeatures = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        for (final var vein : placedFeatures.getTag(BedrockOrePlacements.OVERWORLD_VEINS).orElseThrow()) {
            final var key = vein.unwrapKey().orElseThrow();
            final var heights = vein.value().placement().stream()
                .filter(AboveWorldBottomPlacement.class::isInstance)
                .toList();

            assertEquals(helper, "expected " + key.location() + " to place relative to the world bottom", 1, heights.size());

            for (int i = 0; i < SAMPLE_COUNT; i++) {
                final var y = heights.getFirst().getPositions(context, random, origin).findFirst().orElseThrow().getY();
                assertTrue(helper, key.location() + " resolved to y " + y + ", outside the bedrock band", y >= bottom && y <= bottom + VEIN_MAX_OFFSET);
            }
        }

        helper.succeed();
    }

    // --------------------------------------------------------------------- //

    private VeinPlacementTests() {
    }
}
