package li.cil.bedrockores.common.block.neoforge;

import li.cil.bedrockores.common.block.BedrockOreBlock;

public final class BedrockOreBlockFactoryImpl {
    public static BedrockOreBlock create() {
        return new BedrockOreBlockNeoForge();
    }

    private BedrockOreBlockFactoryImpl() {
    }
}
