/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.gametest;

import li.cil.bedrockores.common.world.BedrockOrePlacements;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;

import java.util.Optional;
import java.util.stream.Collectors;

import static li.cil.bedrockores.gametest.TestSupport.assertEquals;
import static li.cil.bedrockores.gametest.TestSupport.assertTrue;

public final class VeinPlacementTests {
    private static final int VEIN_COUNT = 8;

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

    private VeinPlacementTests() {
    }
}
