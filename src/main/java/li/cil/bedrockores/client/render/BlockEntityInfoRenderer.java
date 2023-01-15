package li.cil.bedrockores.client.render;

import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import li.cil.bedrockores.common.block.entity.BlockEntityWithInfo;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Constants.MOD_ID)
public final class BlockEntityInfoRenderer {
    @SubscribeEvent
    public static void onWorldRender(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        final var mc = Minecraft.getInstance();
        final var player = mc.player;
        if (player == null) {
            return;
        }

        final var level = player.level;

        if (Settings.uiOnlyWhenSneaking.get() && !player.isCrouching()) {
            return;
        }

        if (!(mc.hitResult instanceof final BlockHitResult hit)) {
            return;
        }

        final var blockPos = hit.getBlockPos();
        if (!(level.getBlockEntity(blockPos) instanceof BlockEntityWithInfo info)) {
            return;
        }

        final var text = info.getLookAtInfo();
        if (text == null) {
            return;
        }

        final var stack = event.getPoseStack();
        stack.pushPose();

        // Center and move on top.
        stack.translate(0.5, 1.5, 0.5);

        final var camera = mc.gameRenderer.getMainCamera();
        stack.translate(
                blockPos.getX() - camera.getPosition().x,
                blockPos.getY() - camera.getPosition().y,
                blockPos.getZ() - camera.getPosition().z);

        final EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        stack.mulPose(renderManager.cameraOrientation());

        stack.scale(-0.025f, -0.025f, 0.025f);

        final Matrix4f matrix = stack.last().pose();

        final Font font = Minecraft.getInstance().font;
        final MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        final float horizontalTextOffset = -font.width(text) * 0.5f;
        final float backgroundOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        final int backgroundColor = (int) (backgroundOpacity * 255.0F) << 24;
        final int packedLight = LightTexture.pack(15, 15);

        font.drawInBatch(text, horizontalTextOffset, 0, 0xffffffff,
                false, matrix, buffer, true, backgroundColor, packedLight);
        font.drawInBatch(text, horizontalTextOffset, 0, 0xffffffff,
                false, matrix, buffer, false, 0, packedLight);

        buffer.endBatch();

        stack.popPose();
    }
}
