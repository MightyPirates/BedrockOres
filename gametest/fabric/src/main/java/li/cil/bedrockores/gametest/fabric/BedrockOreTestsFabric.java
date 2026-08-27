/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.gametest.fabric;

import li.cil.bedrockores.gametest.BedrockOreTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static li.cil.bedrockores.gametest.fabric.FabricTestSupport.TEMPLATE;

public final class BedrockOreTestsFabric {
    @GameTest(template = TEMPLATE)
    public void oreWithoutDataIsRemoved(final GameTestHelper helper) {
        BedrockOreTests.oreWithoutDataIsRemoved(helper);
    }

    @GameTest(template = TEMPLATE)
    public void oreWithDataSurvives(final GameTestHelper helper) {
        BedrockOreTests.oreWithDataSurvives(helper);
    }

    @GameTest(template = TEMPLATE)
    public void chunkSweepRemovesOreWithoutData(final GameTestHelper helper) {
        BedrockOreTests.worldGenRemovesOreWithoutData(helper);
    }

    @GameTest(template = TEMPLATE)
    public void chunkSweepKeepsOreWithData(final GameTestHelper helper) {
        BedrockOreTests.worldGenKeepsOreWithData(helper);
    }

    @GameTest(template = TEMPLATE)
    public void stateQueriesNeverLoadChunks(final GameTestHelper helper) {
        BedrockOreTests.stateQueriesNeverLoadChunks(helper);
    }
}
