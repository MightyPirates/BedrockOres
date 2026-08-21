/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.client.fabric;

import li.cil.bedrockores.client.ClientSetup;
import li.cil.bedrockores.client.render.InfoRenderer;
import li.cil.bedrockores.client.render.fabric.BedrockOreUnbakedModel;
import li.cil.bedrockores.common.block.Blocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.RenderType;

public final class BedrockOresFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSetup.initialize();

        ModelLoadingPlugin.register(context -> context.resolveModel().register(resolverContext ->
                BedrockOreUnbakedModel.ID.equals(resolverContext.id()) ? BedrockOreUnbakedModel.INSTANCE : null));

        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BEDROCK_ORE.get(), RenderType.cutout());

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context ->
                InfoRenderer.render(context.matrixStack(), context.camera()));
    }
}
