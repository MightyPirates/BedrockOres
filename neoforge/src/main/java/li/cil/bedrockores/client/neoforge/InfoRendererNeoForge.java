package li.cil.bedrockores.client.neoforge;

import li.cil.bedrockores.client.render.InfoRenderer;
import li.cil.bedrockores.common.config.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class InfoRendererNeoForge {
    @SubscribeEvent
    public static void handleRenderLevelStage(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        InfoRenderer.render(event.getPoseStack(), event.getCamera());
    }

    private InfoRendererNeoForge() {
    }
}
