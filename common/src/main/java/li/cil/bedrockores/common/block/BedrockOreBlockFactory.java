package li.cil.bedrockores.common.block;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class BedrockOreBlockFactory {
    @ExpectPlatform
    public static BedrockOreBlock create() {
        throw new AssertionError();
    }

    private BedrockOreBlockFactory() {
    }
}
