package li.cil.bedrockores.mixin.fabric;

import li.cil.bedrockores.common.block.Blocks;
import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLightEngine.class)
public abstract class MixinBlockLightEngine {
    @Inject(method = "getEmission", at = @At("HEAD"), cancellable = true)
    private void bedrockores$getWrappedOreEmission(final long packedPos, final BlockState state, final CallbackInfoReturnable<Integer> cir) {
        if (!state.is(Blocks.BEDROCK_ORE.get())) {
            return;
        }

        final var pos = BlockPos.of(packedPos);
        final var level = ((LightEngineAccessor) this).bedrockores$getChunkSource().getLevel();
        final var blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof final BedrockOreBlockEntity bedrockOre) {
            cir.setReturnValue(bedrockOre.getOreBlockState().getLightEmission());
        }
    }
}
