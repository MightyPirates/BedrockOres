package li.cil.bedrockores.client.fabric;

import li.cil.bedrockores.client.ClientSetup;
import li.cil.bedrockores.client.render.InfoRenderer;
import li.cil.bedrockores.client.render.fabric.BedrockOreUnbakedModel;
import li.cil.bedrockores.common.block.Blocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public final class BedrockOresFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSetup.initialize();

        CustomUnbakedBlockStateModel.register(BedrockOreUnbakedModel.ID, BedrockOreUnbakedModel.CODEC);

        BlockRenderLayerMap.putBlock(Blocks.BEDROCK_ORE.get(), ChunkSectionLayer.CUTOUT);

        WorldRenderEvents.END_MAIN.register(context ->
                InfoRenderer.render(context.matrices(), Minecraft.getInstance().gameRenderer.getMainCamera()));
    }
}
