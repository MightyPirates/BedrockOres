package li.cil.bedrockores.common.block.entity;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class BedrockOreBlockEntityFactory {
    @ExpectPlatform
    public static BedrockOreBlockEntity create(final BlockPos pos, final BlockState state) {
        throw new AssertionError();
    }

    private BedrockOreBlockEntityFactory() {
    }
}
