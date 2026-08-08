package li.cil.bedrockores.client.render.neoforge;

import com.mojang.serialization.MapCodec;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

public record BedrockOreUnbakedModel() implements CustomUnbakedBlockStateModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "bedrock_ore");

    public static final MapCodec<BedrockOreUnbakedModel> CODEC = MapCodec.unit(BedrockOreUnbakedModel::new);

    private static final Material PARTICLE = new Material(TextureAtlas.LOCATION_BLOCKS,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/bedrock_ore_mask"));

    private static final ModelDebugName DEBUG_NAME = ID::toString;

    // --------------------------------------------------------------------- //

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    @Override
    public BlockStateModel bake(final ModelBaker baker) {
        return new BedrockOreModel(baker.sprites().get(PARTICLE, DEBUG_NAME));
    }

    @Override
    public void resolveDependencies(final ResolvableModel.Resolver resolver) {
    }
}
