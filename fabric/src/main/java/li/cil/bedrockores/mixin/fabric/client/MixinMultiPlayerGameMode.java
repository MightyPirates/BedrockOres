/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.mixin.fabric.client;

import li.cil.bedrockores.common.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode {
    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void bedrockores$dontPredictBedrockOreRemoval(final BlockPos pos, final CallbackInfoReturnable<Boolean> cir) {
        final var minecraft = Minecraft.getInstance();
        final var level = minecraft.level;
        final var player = minecraft.player;
        if (level == null || player == null || player.isCreative()) {
            return;
        }

        if (!level.getBlockState(pos).is(Blocks.BEDROCK_ORE.get())) {
            return;
        }

        cir.setReturnValue(true);
    }
}
