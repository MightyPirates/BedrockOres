package li.cil.bedrockores.mixin.fabric.client;

import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * As of 1.21.11 the sprite is a protected field on {@link SingleQuadParticle} rather than
 * something set through a method, and {@code TextureSheetParticle} is gone entirely.
 */
@Mixin(SingleQuadParticle.class)
public interface SingleQuadParticleAccessor {
    @Accessor("sprite")
    void bedrockores$setSprite(TextureAtlasSprite sprite);
}
