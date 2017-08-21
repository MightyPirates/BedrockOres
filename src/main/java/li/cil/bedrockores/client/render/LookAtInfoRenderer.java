package li.cil.bedrockores.client.render;

import li.cil.bedrockores.common.config.Settings;
import li.cil.bedrockores.common.tileentity.LookAtInfoProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public enum LookAtInfoRenderer {
    INSTANCE;

    @SubscribeEvent
    public void onWorldRender(final RenderWorldLastEvent event) {
        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayer player = mc.player;

        if (Settings.uiOnlyWhenSneaking && !player.isSneaking()) {
            return;
        }

        if (mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }

        final BlockPos blockPos = mc.objectMouseOver.getBlockPos();
        final TileEntity tileEntity = mc.world.getTileEntity(blockPos);
        if (!(tileEntity instanceof LookAtInfoProvider)) {
            return;
        }

        final LookAtInfoProvider infoProvider = (LookAtInfoProvider) tileEntity;

        doPositionPrologue(event);
        doOverlayPrologue();

        float entityYaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * event.getPartialTicks();
        float entityPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * event.getPartialTicks();
        drawNameplateOnTop(mc.fontRendererObj,
                           infoProvider.getLookAtInfo(),
                           blockPos.getX() + 0.5f,
                           blockPos.getY() + 1.5f,
                           blockPos.getZ() + 0.5f,
                           0,
                           entityYaw,
                           entityPitch);

        doOverlayEpilogue();
        doPositionEpilogue();
    }

    private static void doPositionPrologue(final RenderWorldLastEvent event) {
        final EntityPlayer player = Minecraft.getMinecraft().player;
        final double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
        final double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
        final double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-px, -py, -pz);
    }

    private static void doPositionEpilogue() {
        GlStateManager.popMatrix();
    }

    private static void doOverlayPrologue() {
        setLightmapDisabled(true);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
    }

    private static void doOverlayEpilogue() {
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        setLightmapDisabled(false);

    }

    private static void setLightmapDisabled(boolean disabled) {
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);

        if (disabled) {
            GlStateManager.disableTexture2D();
        } else {
            GlStateManager.enableTexture2D();
        }

        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public static void drawNameplateOnTop(FontRenderer fontRenderer, String value, float x, float y, float z, int verticalShift, float viewerYaw, float viewerPitch) {
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0, 1, 0);
        GlStateManager.rotate(-viewerYaw, 0, 1, 0);
        GlStateManager.rotate(viewerPitch, 1, 0, 0);
        GlStateManager.scale(-0.025f, -0.025f, 0.025f);

        int halfWidth = fontRenderer.getStringWidth(value) / 2;

        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(-halfWidth - 1, -1 + verticalShift, 0).color(0, 0, 0, 0.25f).endVertex();
        buffer.pos(-halfWidth - 1, 8 + verticalShift, 0).color(0, 0, 0, 0.25f).endVertex();
        buffer.pos(halfWidth + 1, 8 + verticalShift, 0).color(0, 0, 0, 0.25f).endVertex();
        buffer.pos(halfWidth + 1, -1 + verticalShift, 0).color(0, 0, 0, 0.25f).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();

        fontRenderer.drawString(value, -halfWidth, verticalShift, 0x20FFFFFF);
        fontRenderer.drawString(value, -halfWidth, verticalShift, 0xFFFFFFFF);
        GlStateManager.color(1, 1, 1, 1);
    }
}
