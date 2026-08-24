/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.gametest.neoforge;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.summary.JUnitSummaryDumper;

import java.nio.file.Path;

import static li.cil.bedrockores.gametest.TestSupport.MOD_ID;

@Mod(MOD_ID)
public final class GameTests {
    private static final String JUNIT_OUTPUT_DIR_PROPERTY = "bedrockores.gameTest.junitDir";

    public GameTests(final IEventBus modEventBus, final ModContainer modContainer) {
        FrameworkConfiguration.builder(Identifier.fromNamespaceAndPath(MOD_ID, "tests"))
            .dumpers(new JUnitSummaryDumper(Path.of(
                System.getProperty(JUNIT_OUTPUT_DIR_PROPERTY, "../../build/test-results/gameTest"))))
            .build()
            .create()
            .init(modEventBus, modContainer);
    }
}
