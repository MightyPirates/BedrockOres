package li.cil.bedrockores.client.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.EmptyModel;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public final class BedrockOreBakedModel implements IDynamicBakedModel {
    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, final RandomSource random, final ModelData data, @Nullable final RenderType renderType) {
        final var ore = data.get(Ore.ORE_PROPERTY);
        if (ore != null) {
            return ore.model().getQuads(ore.state(), side, random, ore.data(), renderType);
        } else {
            return ImmutableList.of();
        }
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

    @SuppressWarnings("deprecation")
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return EmptyModel.BAKED.getParticleIcon();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull final ModelData data) {
        final var ore = data.get(Ore.ORE_PROPERTY);
        if (ore != null) {
            return ore.model().getParticleIcon(ore.data());
        } else {
            return IDynamicBakedModel.super.getParticleIcon(data);
        }
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull final BlockState state, @NotNull final RandomSource random, @NotNull final ModelData data) {
        final var ore = data.get(Ore.ORE_PROPERTY);
        if (ore != null) {
            return ore.model().getRenderTypes(ore.state(), random, ore.data());
        } else {
            return ChunkRenderTypeSet.of();
        }
    }
}
