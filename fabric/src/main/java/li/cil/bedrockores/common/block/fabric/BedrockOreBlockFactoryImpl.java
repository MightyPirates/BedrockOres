package li.cil.bedrockores.common.block.fabric;

import li.cil.bedrockores.common.block.BedrockOreBlock;

public final class BedrockOreBlockFactoryImpl {
    public static BedrockOreBlock create() {
        return new BedrockOreBlock();
    }

    private BedrockOreBlockFactoryImpl() {
    }
}
