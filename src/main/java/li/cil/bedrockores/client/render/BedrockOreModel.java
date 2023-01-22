package li.cil.bedrockores.client.render;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.Set;
import java.util.function.Function;

public final class BedrockOreModel implements IUnbakedGeometry<BedrockOreModel> {
    @Override
    public BakedModel bake(final IGeometryBakingContext context, final ModelBaker baker, final Function<Material, TextureAtlasSprite> spriteGetter, final ModelState modelState, final ItemOverrides overrides, final ResourceLocation modelLocation) {
        return new BedrockOreBakedModel();
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> modelGetter, final IGeometryBakingContext context) {
    }

    @Override
    public Set<String> getConfigurableComponentNames() {
        return ImmutableSet.of();
    }
}
