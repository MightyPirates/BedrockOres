/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.gametest.neoforge;

import li.cil.bedrockores.gametest.VeinPlacementTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "worldgen")
public final class VeinPlacementTestsNeoForge {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "The vein tag resolves to every shipped placed feature.")
    public static void veinTagIsPopulated(final GameTestHelper helper) {
        VeinPlacementTests.veinTagIsPopulated(helper);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Every vein is added to overworld biomes.")
    public static void overworldBiomesGetVeins(final GameTestHelper helper) {
        VeinPlacementTests.overworldBiomesGetVeins(helper);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Every vein resolves into the bedrock band, superflat included.")
    public static void veinsResolveIntoBedrockBand(final GameTestHelper helper) {
        VeinPlacementTests.veinsResolveIntoBedrockBand(helper);
    }

    private VeinPlacementTestsNeoForge() {
    }
}
