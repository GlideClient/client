package eu.shoroa.contrib.cosmetic;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.injection.interfaces.IMixinMinecraft;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Cosmetic {
    private RenderPlayer renderPlayer;
    private final Framebuffer framebuffer = new Framebuffer(90 * 4, 140 * 4, true);

    private final String name;
    private final PositionType positionType;
    private boolean state;
    private Minecraft mc =  Minecraft.getMinecraft();

    public Cosmetic(String name, PositionType positionType) {
        this.name = name;
        this.positionType = positionType;
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

        framebuffer.setFramebufferColor(0f, 0f, 0f, 1f);
        framebuffer.setFramebufferFilter(GL11.GL_LINEAR);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);

        float scale = 340f;

//        GlStateManager.enableColorMaterial();
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(framebuffer.framebufferWidth / 2f + 80, 20f, 50f);
//        GlStateManager.rotate((System.currentTimeMillis() % 2000) / 2000f * 360, 0f, 1f, 0f);
        GlStateManager.pushMatrix();
//        GlStateManager.scale(scale, scale / 2f, scale);
        GlStateManager.scale(1f, 0.4f, 1f);
        GlStateManager.rotate(180f, 1f, 0f, 0f);
        GlStateManager.rotate(180f, 0f, 1f, 0f);
        GlStateManager.translate(-(framebuffer.framebufferWidth / 2f + 80), -60, 0f);
        GuiInventory.drawEntityOnScreen(0,0, (int) scale, 0f, 0f, mc.thePlayer);
        GlStateManager.popMatrix();
//        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
//        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
//        RenderHelper.enableStandardItemLighting();
//        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
//        rm.setPlayerViewY(180.0F);
//        rm.setRenderShadow(false);
//        GlStateManager.enableDepth();
//        GlStateManager.enableCull();
//        rm.renderEntityWithPosYaw(mc.thePlayer, 0,0,0,0f, 0f);
//        GlStateManager.disableDepth();
//        rm.setRenderShadow(true);
//        GlStateManager.popMatrix();
//        RenderHelper.disableStandardItemLighting();
//        GlStateManager.disableRescaleNormal();
//        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
//        GlStateManager.disableTexture2D();
//        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
        nvg.setupAndDraw(() -> {
//            nvg.drawRoundedRect(0f, 0f, 100f, 100f, 8f, Color.RED);
        });
//        GuiInventory.drawEntityOnScreen(45, 70, 30, 0f, 0f, mc.thePlayer);
        mc.getFramebuffer().bindFramebuffer(true);
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
