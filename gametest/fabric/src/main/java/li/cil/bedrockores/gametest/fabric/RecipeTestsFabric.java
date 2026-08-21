package li.cil.bedrockores.gametest.fabric;

import li.cil.bedrockores.gametest.RecipeTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static li.cil.bedrockores.gametest.fabric.FabricTestSupport.TEMPLATE;

public final class RecipeTestsFabric {
    @GameTest(template = TEMPLATE)
    public void everyModItemIsCraftable(final GameTestHelper helper) {
        RecipeTests.everyModItemIsCraftable(helper);
    }

    @GameTest(template = TEMPLATE)
    public void everyRecipeCraftsInCraftingTable(final GameTestHelper helper) {
        RecipeTests.everyRecipeCraftsInCraftingTable(helper);
    }
}
