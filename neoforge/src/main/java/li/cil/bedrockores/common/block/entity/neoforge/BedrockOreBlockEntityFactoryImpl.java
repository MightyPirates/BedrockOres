package li.cil.bedrockores.common.block.entity.neoforge;

import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class BedrockOreBlockEntityFactoryImpl {
    public static BedrockOreBlockEntity create(final BlockPos pos, final BlockState state) {
        return new BedrockOreBlockEntityNeoForge(pos, state);
    }

    private BedrockOreBlockEntityFactoryImpl() {
    }
}
