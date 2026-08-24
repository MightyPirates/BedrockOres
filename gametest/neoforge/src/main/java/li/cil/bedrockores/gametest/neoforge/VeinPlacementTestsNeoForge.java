/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.gametest.neoforge;

import li.cil.bedrockores.gametest.VeinPlacementTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static li.cil.bedrockores.gametest.TestSupport.MOD_ID;
import static li.cil.bedrockores.gametest.TestSupport.TEMPLATE;

@GameTestHolder(MOD_ID)
@PrefixGameTestTemplate(false)
public final class VeinPlacementTestsNeoForge {
    @GameTest(template = TEMPLATE)
    public static void veinTagIsPopulated(final GameTestHelper helper) {
        VeinPlacementTests.veinTagIsPopulated(helper);
    }

    @GameTest(template = TEMPLATE)
    public static void overworldBiomesGetVeins(final GameTestHelper helper) {
        VeinPlacementTests.overworldBiomesGetVeins(helper);
    }

    @GameTest(template = TEMPLATE)
    public static void veinsResolveIntoBedrockBand(final GameTestHelper helper) {
        VeinPlacementTests.veinsResolveIntoBedrockBand(helper);
    }

    private VeinPlacementTestsNeoForge() {
    }
}
