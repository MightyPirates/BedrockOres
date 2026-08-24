/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.client.render.fabric;

import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class BedrockOreModel implements BlockStateModel, FabricBlockStateModel {
    private static final Direction[] FACES = new Direction[Direction.values().length + 1];

    static {
        System.arraycopy(Direction.values(), 0, FACES, 0, Direction.values().length);
    }

    private final TextureAtlasSprite particle;

    public BedrockOreModel(final TextureAtlasSprite particle) {
        this.particle = particle;
    }

    // --------------------------------------------------------------------- //
    // FabricBlockStateModel

    @Override
    public void emitQuads(final QuadEmitter emitter, final BlockAndTintGetter blockView, final BlockPos pos, final BlockState state, final RandomSource random, final Predicate<Direction> cullTest) {
        final var oreState = getOreBlockState(blockView, pos);
        if (oreState == null) {
            return;
        }

        final var oreLayer = ItemBlockRenderTypes.getChunkRenderType(oreState);

        final var parts = new ArrayList<BlockModelPart>();
        Minecraft.getInstance().getBlockRenderer().getBlockModel(oreState).collectParts(random, parts);

        for (final var part : parts) {
            for (final var face : FACES) {
                for (final var quad : part.getQuads(face)) {
                    emitter.fromBakedQuad(quad);
                    emitter.cullFace(face);
                    emitter.renderLayer(oreLayer);
                    emitter.emit();
                }
            }
        }
    }

    @Override
    public TextureAtlasSprite particleSprite(final BlockAndTintGetter blockView, final BlockPos pos, final BlockState state) {
        final var oreState = getOreBlockState(blockView, pos);
        if (oreState == null) {
            return particle;
        }
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(oreState).particleIcon();
    }

    @Nullable
    @Override
    public Object createGeometryKey(final BlockAndTintGetter blockView, final BlockPos pos, final BlockState state, final RandomSource random) {
        return getOreBlockState(blockView, pos);
    }

    // --------------------------------------------------------------------- //
    // BlockStateModel

    @Override
    public void collectParts(final RandomSource random, final List<BlockModelPart> parts) {
        // Position-blind callers get nothing; everything interesting needs the block entity.
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return particle;
    }

    // --------------------------------------------------------------------- //

    @Nullable
    private static BlockState getOreBlockState(final BlockAndTintGetter blockView, final BlockPos pos) {
        if (!(blockView instanceof final FabricBlockView fabricBlockView)) {
            return null;
        }

        if (!(fabricBlockView.getBlockEntityRenderData(pos) instanceof final BlockState oreState)) {
            return null;
        }

        return oreState.isAir() ? null : oreState;
    }
}
