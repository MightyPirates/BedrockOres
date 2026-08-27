/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.gametest.neoforge;

import li.cil.bedrockores.gametest.BedrockOreTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "ore")
public final class BedrockOreTestsNeoForge {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "A bedrock ore placed without ore data is removed once it is loaded.")
    public static void oreWithoutDataIsRemoved(final GameTestHelper helper) {
        BedrockOreTests.oreWithoutDataIsRemoved(helper);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "A bedrock ore with ore data survives loading.")
    public static void oreWithDataSurvives(final GameTestHelper helper) {
        BedrockOreTests.oreWithDataSurvives(helper);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "The chunk sweep removes bedrock ores without ore data.")
    public static void chunkSweepRemovesOreWithoutData(final GameTestHelper helper) {
        BedrockOreTests.worldGenRemovesOreWithoutData(helper);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "The chunk sweep keeps bedrock ores with ore data.")
    public static void chunkSweepKeepsOreWithData(final GameTestHelper helper) {
        BedrockOreTests.worldGenKeepsOreWithData(helper);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "State queries on a bedrock ore never load the chunk they are asked about.")
    public static void stateQueriesNeverLoadChunks(final GameTestHelper helper) {
        BedrockOreTests.stateQueriesNeverLoadChunks(helper);
    }

    private BedrockOreTestsNeoForge() {
    }
}
