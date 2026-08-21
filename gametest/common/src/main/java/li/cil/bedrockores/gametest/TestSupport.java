package li.cil.bedrockores.gametest;

import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;

public final class TestSupport {
    public static final String MOD_ID = "bedrockores_gametest";
    public static final String TEMPLATE = "empty";

    public static final int WORK_Y = 2;

    // --------------------------------------------------------------------- //

    public static GameTestAssertException failure(final GameTestHelper helper, final String message) {
        return new GameTestAssertException(message);
    }

    public static void assertTrue(final GameTestHelper helper, final String what, final boolean condition) {
        if (!condition) {
            throw failure(helper, what);
        }
    }

    public static void assertEquals(final GameTestHelper helper, final String what, final long expected, final long actual) {
        if (expected != actual) {
            throw failure(helper, what + ": expected " + expected + ", got " + actual);
        }
    }

    // --------------------------------------------------------------------- //

    private TestSupport() {
    }
}
