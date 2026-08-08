package li.cil.bedrockores.client.render.neoforge;

import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;

import javax.annotation.Nullable;
import java.util.List;

public final class BedrockOreModel implements DynamicBlockStateModel {
    private final TextureAtlasSprite particle;

    public BedrockOreModel(final TextureAtlasSprite particle) {
        this.particle = particle;
    }

    // --------------------------------------------------------------------- //

    @Override
    public void collectParts(final BlockAndTintGetter level, final BlockPos pos, final BlockState state, final RandomSource random, final List<BlockModelPart> parts) {
        final var oreState = getOreBlockState(level, pos);
        if (oreState == null) {
            return;
        }

        final var oreModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(oreState);
        if (oreModel instanceof final DynamicBlockStateModel dynamic) {
            dynamic.collectParts(level, pos, oreState, random, parts);
        } else {
            oreModel.collectParts(random, parts);
        }
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return particle;
    }

    @Override
    public TextureAtlasSprite particleIcon(final BlockAndTintGetter level, final BlockPos pos, final BlockState state) {
        final var oreState = getOreBlockState(level, pos);
        if (oreState == null) {
            return particle;
        }
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(oreState).particleIcon();
    }

    // --------------------------------------------------------------------- //

    @Nullable
    private static BlockState getOreBlockState(final BlockAndTintGetter level, final BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof final BedrockOreBlockEntity bedrockOre)) {
            return null;
        }

        final var oreState = bedrockOre.getOreBlockState();
        return oreState.isAir() ? null : oreState;
    }
}
