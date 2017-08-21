package li.cil.bedrockores.client.render;

import com.google.common.collect.ImmutableList;
import li.cil.bedrockores.common.block.BlockBedrockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

@SideOnly(Side.CLIENT)
public final class BakedModelBedrockOre implements IBakedModel {
    private final IBlockState bedrockState = Blocks.BEDROCK.getDefaultState();
    private final IBakedModel mask;
    private final TextureAtlasSprite particleTexture;

    BakedModelBedrockOre(final IBakedModel mask, final TextureAtlasSprite particleTexture) {
        this.mask = mask;
        this.particleTexture = particleTexture;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final IBlockState state, @Nullable final EnumFacing side, final long rand) {
        if (state == null) {
            return ImmutableList.of();
        }

        try {
            final IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
            IBlockState oreState = extendedBlockState.getValue(BlockBedrockOre.ORE_BLOCK_STATE);
            if (oreState == null) {
                oreState = bedrockState;
            }

            final BlockModelShapes shapes = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
            final IBakedModel model = shapes.getModelForState(oreState);
            final List<BakedQuad> oreQuads = model.getQuads(state, side, rand);
            final List<BakedQuad> maskQuads = mask.getQuads(state, side, rand);

            final ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
            builder.addAll(oreQuads);
            builder.addAll(maskQuads);
            return builder.build();
        } catch (final Throwable ignored) {
            return ImmutableList.of();
        }
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return particleTexture;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
