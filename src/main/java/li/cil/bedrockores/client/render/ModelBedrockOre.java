package li.cil.bedrockores.client.render;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import li.cil.bedrockores.client.model.ModelLoaderBedrockOre;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;

@SideOnly(Side.CLIENT)
public enum ModelBedrockOre implements IModel {
    INSTANCE;

    // --------------------------------------------------------------------- //
    // IModel

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableList.of();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return ImmutableList.of();
    }

    @Override
    public IBakedModel bake(final IModelState state, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        final IBakedModel mask = ModelLoaderBedrockOre.INSTANCE.getMaskModel().bake(state, format, bakedTextureGetter);
        final TextureAtlasSprite particleTexture = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(Blocks.BEDROCK.getDefaultState()).getParticleTexture();
        return new BakedModelBedrockOre(mask, particleTexture);
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
}
