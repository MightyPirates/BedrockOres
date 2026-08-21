/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.client.render.fabric;

import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public final class BedrockOreBakedModel implements BakedModel, FabricBakedModel {
    private final TextureAtlasSprite particle;

    public BedrockOreBakedModel(final TextureAtlasSprite particle) {
        this.particle = particle;
    }

    // --------------------------------------------------------------------- //
    // FabricBakedModel

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(final BlockAndTintGetter blockView, final BlockState state, final BlockPos pos, final Supplier<RandomSource> randomSupplier, final RenderContext context) {
        if (!(blockView instanceof final FabricBlockView fabricBlockView)) {
            return;
        }

        if (!(fabricBlockView.getBlockEntityRenderData(pos) instanceof final BlockState oreState)) {
            return;
        }

        final var oreModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(oreState);

        final var material = materialFor(oreState);
        if (material != null) {
            context.pushTransform(quad -> {
                quad.material(material);
                return true;
            });
        }

        try {
            oreModel.emitBlockQuads(blockView, oreState, pos, randomSupplier, context);
        } finally {
            if (material != null) {
                context.popTransform();
            }
        }
    }

    @Nullable
    private static RenderMaterial materialFor(final BlockState oreState) {
        final var renderer = RendererAccess.INSTANCE.getRenderer();
        if (renderer == null) {
            return null;
        }

        return renderer.materialFinder().clear()
                .blendMode(BlendMode.fromRenderLayer(ItemBlockRenderTypes.getChunkRenderType(oreState)))
                .find();
    }

    @Override
    public void emitItemQuads(final ItemStack stack, final Supplier<RandomSource> randomSupplier, final RenderContext context) {
    }

    // --------------------------------------------------------------------- //
    // BakedModel

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, final RandomSource random) {
        return List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
