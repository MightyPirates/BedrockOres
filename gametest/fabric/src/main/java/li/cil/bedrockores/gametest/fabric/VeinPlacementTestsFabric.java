/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.gametest.fabric;

import li.cil.bedrockores.gametest.VeinPlacementTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class VeinPlacementTestsFabric {
    @GameTest
    public void veinTagIsPopulated(final GameTestHelper helper) {
        VeinPlacementTests.veinTagIsPopulated(helper);
    }

    @GameTest
    public void overworldBiomesGetVeins(final GameTestHelper helper) {
        VeinPlacementTests.overworldBiomesGetVeins(helper);
    }

    @GameTest
    public void veinsResolveIntoBedrockBand(final GameTestHelper helper) {
        VeinPlacementTests.veinsResolveIntoBedrockBand(helper);
    }
}
