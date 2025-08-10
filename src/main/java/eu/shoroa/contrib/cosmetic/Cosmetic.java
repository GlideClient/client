package eu.shoroa.contrib.cosmetic;

import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.injection.interfaces.IMixinMinecraft;
import me.eldodebug.soar.injection.interfaces.IMixinRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;

public class Cosmetic {
    private RenderPlayer renderPlayer;
    private final Framebuffer framebuffer = new Framebuffer(90 * 4, 140 * 4, true);

    private final String name;
    private final PositionType positionType;
    private boolean state;
    private final Minecraft mc =  Minecraft.getMinecraft();
    private final CosmeticPreviewEntity previewEntity;

    public Cosmetic(String name, PositionType positionType) {
        this.name = name;
        this.positionType = positionType;
        this.previewEntity = new CosmeticPreviewEntity(this);
        mc.getRenderManager().cacheActiveRenderInfo(previewEntity.fakeWorld, mc.fontRendererObj, previewEntity, previewEntity, mc.gameSettings, 0.0F);
    }

    public RenderPlayer getRenderPlayer() {
        return renderPlayer;
    }

    public void setRenderPlayer(RenderPlayer renderPlayer) {
        this.renderPlayer = renderPlayer;
    }

    public void render(AbstractClientPlayer entityPlayer, float handSwing, float handSwingAmount, float ticks, float age, float headYaw, float headPitch, float scale) {}
    public void render(AbstractClientPlayer entityPlayer) {}

    public final void renderPreview() {
        if (!(mc.currentScreen instanceof GuiModMenu)) return;

        IMixinMinecraft imm = (IMixinMinecraft) mc;
        RenderManager rm = mc.getRenderManager();

//        EntityPlayerSP lastPlayer = mc.thePlayer;
//        mc.thePlayer = previewEntity;
//        previewEntity.fakeWorld.updateEntity(previewEntity);
//        if (mc.getRenderManager().worldObj == null || ((IMixinRenderManager) mc.getRenderManager()).getPlayerRenderer() == null) {
//            mc.getRenderManager().cacheActiveRenderInfo(previewEntity.fakeWorld, mc.fontRendererObj, previewEntity, previewEntity, mc.gameSettings, 0.0F);
//        }

        framebuffer.setFramebufferColor(0f, 0f, 0f, 0f);
        framebuffer.setFramebufferFilter(GL11.GL_LINEAR);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);

        float scale = 340f;

        GlStateManager.pushMatrix();
        GlStateManager.scale(1f, .4f, 1f);
        GlStateManager.translate(-(framebuffer.framebufferWidth / 2f - 415), 50, 0f);
        GlStateManager.rotate(((System.currentTimeMillis() % 4000) / 4000f * 360), 0f, 1f, 0f);
        drawEntityOnScreen(0f, 0f, scale, 0f, 0f, mc.thePlayer);
        GlStateManager.popMatrix();
        mc.getFramebuffer().bindFramebuffer(true);

//        mc.thePlayer = lastPlayer;
    }

    private void drawEntityOnScreen(float posX, float posY, float scale, float yawRotate, float pitchRotate, EntityLivingBase ent) {
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(posX, posY, 50.0F);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(pitchRotate, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(yawRotate, 0.0F, 1.0F, 0.0F);
        float f2 = ent.renderYawOffset;
        float f3 = ent.rotationYaw;
        float f4 = ent.rotationPitch;
        float f5 = ent.prevRotationYawHead;
        float f6 = ent.rotationYawHead;
        RenderHelper.enableStandardItemLighting();
        ent.renderYawOffset = (float) Math.atan(yawRotate / 40.0F);
        ent.rotationYaw = (float) Math.atan(yawRotate / 40.0F);
        ent.rotationPitch = -((float) Math.atan(0 / 40.0F)) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        try {
            RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
            rendermanager.setPlayerViewY(180.0F);
            rendermanager.setRenderShadow(false);
            rendermanager.doRenderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, true);
            rendermanager.setRenderShadow(true);
        }
        finally {
            ent.renderYawOffset = f2;
            ent.rotationYaw = f3;
            ent.rotationPitch = f4;
            ent.prevRotationYawHead = f5;
            ent.rotationYawHead = f6;
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.translate(0.0F, 0.0F, 20.0F);
        }
    }

    public boolean isEnabled() {
        return state;
    }

    public void setEnabled(boolean enabled) {
        this.state = enabled;
    }

    public void toggle() {
        this.state = !this.state;
    }

    public String getName() {
        return name;
    }

    public PositionType getPositionType() {
        return positionType;
    }

    public int getPreviewImage() {
        return framebuffer.framebufferTexture;
    }
}
