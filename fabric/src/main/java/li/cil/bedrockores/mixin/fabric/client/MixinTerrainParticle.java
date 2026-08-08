package li.cil.bedrockores.mixin.fabric.client;

import li.cil.bedrockores.client.render.fabric.OreParticleSprites;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TerrainParticle.class)
public class MixinTerrainParticle {
    @Inject(
            method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
            at = @At("RETURN"))
    private void bedrockores$applyOreSprite(final ClientLevel level, final double x, final double y, final double z, final double vx, final double vy, final double vz, final BlockState state, final BlockPos pos, final CallbackInfo ci) {
        OreParticleSprites.apply((TerrainParticle) (Object) this, level, state, pos);
    }
}
