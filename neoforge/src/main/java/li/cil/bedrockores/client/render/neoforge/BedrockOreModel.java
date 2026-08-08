package li.cil.bedrockores.client.render.neoforge;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.Set;
import java.util.function.Function;

public final class BedrockOreModel implements IUnbakedGeometry<BedrockOreModel> {
    @Override
    public BakedModel bake(final IGeometryBakingContext context, final ModelBaker baker, final Function<Material, TextureAtlasSprite> spriteGetter, final ModelState modelState, final ItemOverrides overrides) {
        return new BedrockOreBakedModel();
    }

    @Override
    public void resolveParents(final Function<Identifier, UnbakedModel> modelGetter, final IGeometryBakingContext context) {
    }

    @Override
    public Set<String> getConfigurableComponentNames() {
        return ImmutableSet.of();
    }
}
