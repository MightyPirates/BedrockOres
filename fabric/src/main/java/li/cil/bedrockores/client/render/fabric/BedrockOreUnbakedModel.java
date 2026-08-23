/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.client.render.fabric;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public final class BedrockOreUnbakedModel implements UnbakedModel {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block/bedrock_ore");

    public static final BedrockOreUnbakedModel INSTANCE = new BedrockOreUnbakedModel();

    private static final Material PARTICLE = new Material(InventoryMenu.BLOCK_ATLAS,
        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block/bedrock_ore_mask"));

    // --------------------------------------------------------------------- //

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return List.of();
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> resolver) {
    }

    @Override
    public BakedModel bake(final ModelBaker baker, final Function<Material, TextureAtlasSprite> spriteGetter, final ModelState state) {
        return new BedrockOreBakedModel(spriteGetter.apply(PARTICLE));
    }

    private BedrockOreUnbakedModel() {
    }
}
