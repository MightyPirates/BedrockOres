package li.cil.bedrockores.client.render.fabric;

import li.cil.bedrockores.common.block.Blocks;
import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import li.cil.bedrockores.mixin.fabric.client.SingleQuadParticleAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public final class OreParticleSprites {
    public static void apply(final TerrainParticle particle, @Nullable final ClientLevel level, final BlockState state, @Nullable final BlockPos pos) {
        if (level == null || pos == null || !state.is(Blocks.BEDROCK_ORE.get())) {
            return;
        }

        final var bedrockOre = findBedrockOre(level, pos);
        if (bedrockOre == null) {
            return;
        }

        final var oreState = bedrockOre.getOreBlockState();
        if (oreState.isAir()) {
            return;
        }

        final var sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(oreState);
        ((SingleQuadParticleAccessor) particle).bedrockores$setSprite(sprite);
    }

    @Nullable
    private static BedrockOreBlockEntity findBedrockOre(final ClientLevel level, final BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof final BedrockOreBlockEntity bedrockOre) {
            return bedrockOre;
        }

        // Landing and running particles spawn slightly *above* the block that spawned them, so
        // the position derived from their spawn coordinates is one block off.
        if (level.getBlockEntity(pos.below()) instanceof final BedrockOreBlockEntity bedrockOre) {
            return bedrockOre;
        }

        return null;
    }

    private OreParticleSprites() {
    }
}
