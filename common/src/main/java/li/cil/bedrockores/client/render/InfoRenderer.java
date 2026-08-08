package li.cil.bedrockores.client.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.bedrockores.common.block.entity.BlockEntityWithInfo;
import li.cil.bedrockores.common.config.Settings;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.BlockHitResult;

public final class InfoRenderer {
    private static final MultiBufferSource.BufferSource BUFFER =
            MultiBufferSource.immediate(new ByteBufferBuilder(1536));

    public static void render(final PoseStack stack, final Camera camera) {
        final var mc = Minecraft.getInstance();
        final var player = mc.player;
        if (player == null) {
            return;
        }

        final var level = player.level();

        if (Settings.uiOnlyWhenSneaking.get() && !player.isCrouching()) {
            return;
        }

        if (!(mc.hitResult instanceof final BlockHitResult hit)) {
            return;
        }

        final var blockPos = hit.getBlockPos();
        if (!(level.getBlockEntity(blockPos) instanceof final BlockEntityWithInfo info)) {
            return;
        }

        final var text = info.getLookAtInfo();
        if (text == null) {
            return;
        }

        stack.pushPose();

        stack.translate(0.5, 1.5, 0.5);

        stack.translate(
                blockPos.getX() - camera.position().x,
                blockPos.getY() - camera.position().y,
                blockPos.getZ() - camera.position().z);

        stack.mulPose(camera.rotation());

        stack.scale(0.025f, -0.025f, 0.025f);

        final var matrix = stack.last().pose();

        final Font font = mc.font;

        final float horizontalTextOffset = -font.width(text) * 0.5f;
        final float backgroundOpacity = mc.options.getBackgroundOpacity(0.25F);
        final int backgroundColor = (int) (backgroundOpacity * 255.0F) << 24;
        final int packedLight = LightTexture.pack(15, 15);

        font.drawInBatch(text, horizontalTextOffset, 0, 0xffffffff,
                false, matrix, BUFFER, Font.DisplayMode.SEE_THROUGH, backgroundColor, packedLight);
        font.drawInBatch(text, horizontalTextOffset, 0, 0xffffffff,
                false, matrix, BUFFER, Font.DisplayMode.NORMAL, 0, packedLight);

        BUFFER.endBatch();

        stack.popPose();
    }

    private InfoRenderer() {
    }
}
