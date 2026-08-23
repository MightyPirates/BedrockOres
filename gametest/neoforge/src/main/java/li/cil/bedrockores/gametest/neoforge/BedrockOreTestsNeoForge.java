/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.gametest.neoforge;

import li.cil.bedrockores.gametest.BedrockOreTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static li.cil.bedrockores.gametest.TestSupport.MOD_ID;
import static li.cil.bedrockores.gametest.TestSupport.TEMPLATE;

@GameTestHolder(MOD_ID)
@PrefixGameTestTemplate(false)
public final class BedrockOreTestsNeoForge {
    @GameTest(template = TEMPLATE)
    public static void oreWithoutDataIsRemoved(final GameTestHelper helper) {
        BedrockOreTests.oreWithoutDataIsRemoved(helper);
    }

    @GameTest(template = TEMPLATE)
    public static void oreWithDataSurvives(final GameTestHelper helper) {
        BedrockOreTests.oreWithDataSurvives(helper);
    }

    @GameTest(template = TEMPLATE)
    public static void chunkSweepRemovesOreWithoutData(final GameTestHelper helper) {
        BedrockOreTests.worldGenRemovesOreWithoutData(helper);
    }

    @GameTest(template = TEMPLATE)
    public static void chunkSweepKeepsOreWithData(final GameTestHelper helper) {
        BedrockOreTests.worldGenKeepsOreWithData(helper);
    }

    private BedrockOreTestsNeoForge() {
    }
}
