/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.gametest.fabric;

import li.cil.bedrockores.gametest.VeinPlacementTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static li.cil.bedrockores.gametest.fabric.FabricTestSupport.TEMPLATE;

public final class VeinPlacementTestsFabric {
    @GameTest(template = TEMPLATE)
    public void veinTagIsPopulated(final GameTestHelper helper) {
        VeinPlacementTests.veinTagIsPopulated(helper);
    }

    @GameTest(template = TEMPLATE)
    public void overworldBiomesGetVeins(final GameTestHelper helper) {
        VeinPlacementTests.overworldBiomesGetVeins(helper);
    }

    @GameTest(template = TEMPLATE)
    public void veinsResolveIntoBedrockBand(final GameTestHelper helper) {
        VeinPlacementTests.veinsResolveIntoBedrockBand(helper);
    }
}
