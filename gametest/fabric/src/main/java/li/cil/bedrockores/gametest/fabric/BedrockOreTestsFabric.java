/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.gametest.fabric;

import li.cil.bedrockores.gametest.BedrockOreTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class BedrockOreTestsFabric {
    @GameTest
    public void oreWithoutDataIsRemoved(final GameTestHelper helper) {
        BedrockOreTests.oreWithoutDataIsRemoved(helper);
    }

    @GameTest
    public void oreWithDataSurvives(final GameTestHelper helper) {
        BedrockOreTests.oreWithDataSurvives(helper);
    }

    @GameTest
    public void chunkSweepRemovesOreWithoutData(final GameTestHelper helper) {
        BedrockOreTests.worldGenRemovesOreWithoutData(helper);
    }

    @GameTest
    public void chunkSweepKeepsOreWithData(final GameTestHelper helper) {
        BedrockOreTests.worldGenKeepsOreWithData(helper);
    }

    @GameTest
    public void stateQueriesNeverLoadChunks(final GameTestHelper helper) {
        BedrockOreTests.stateQueriesNeverLoadChunks(helper);
    }
}
