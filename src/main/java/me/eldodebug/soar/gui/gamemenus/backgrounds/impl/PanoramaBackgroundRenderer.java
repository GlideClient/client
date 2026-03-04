package me.eldodebug.soar.gui.gamemenus.backgrounds.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

public class PanoramaBackgroundRenderer extends AbstractBackground {

    private final Tessellator tessellator = Tessellator.getInstance();
    private final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    
    private int panoramaTimer;
    private static final float FOV = 120.0F;
    float animationSpeed = 10f;
    private ResourceLocation backgroundTexture;
    private final PanoramaMode currentMode = PanoramaMode.REACTIVE;
    private float lastPitch = 0.0F, lastYaw = 0.0F, width = 0F, height = 0F;
    private long lastFrameTime = System.currentTimeMillis();
    Minecraft mc;

    float centerX, centerY;

    private enum PanoramaMode {
        UP_AND_DOWN,
        FLAT_SPIN,
        REACTIVE,
        STATIONARY
    }

    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[] {
            new ResourceLocation("textures/gui/title/background/panorama_0.png"),
            new ResourceLocation("textures/gui/title/background/panorama_1.png"),
            new ResourceLocation("textures/gui/title/background/panorama_2.png"),
            new ResourceLocation("textures/gui/title/background/panorama_3.png"),
            new ResourceLocation("textures/gui/title/background/panorama_4.png"),
            new ResourceLocation("textures/gui/title/background/panorama_5.png")
    };

    private static final float[][] SIDE_ROTATIONS = new float[][] {
            { 0.0F,   0.0F, 1.0F, 0.0F }, // Front
            { 90.0F,  0.0F, 1.0F, 0.0F }, // Right
            { 180.0F, 0.0F, 1.0F, 0.0F }, // Back
            { -90.0F, 0.0F, 1.0F, 0.0F }, // Left
            { 90.0F,  1.0F, 0.0F, 0.0F }, // Up
            { -90.0F, 1.0F, 0.0F, 0.0F }  // Down
    };

    public void init() {
        mc = Minecraft.getMinecraft();
        width = mc.displayWidth;
        height = mc.displayHeight;
        DynamicTexture viewportTexture = new DynamicTexture(256, 256);
        this.backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", viewportTexture);
        centerX = mc.displayWidth / 2.0F;
        centerY = mc.displayHeight / 2.0F;
        lastFrameTime = System.currentTimeMillis();
    }

    public void draw(ScaledResolution sr, Glide instance, NanoVGManager nvg, float partialTicks) {
        drawGradientRect(0, 0, mc.displayWidth, mc.displayHeight, -1, -1); // for some reason the menu goes white without this
        GlStateManager.disableAlpha();
        this.renderSkybox(partialTicks);
        GlStateManager.enableAlpha();
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void update(float width, float height) {
        this.width = width;
        this.height = height;
    }

    private void drawPanorama(float partialTicks) {
        ++this.panoramaTimer;
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(FOV, 1.0F, 0.05F, 10.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        int renderPasses = 8;

        for (int currentPass = 0; currentPass < renderPasses * renderPasses; ++currentPass) {
            GlStateManager.pushMatrix();

            float offX = ((float)(currentPass % renderPasses) / (float)renderPasses - 0.5F) / 64.0F;
            float offY = ((float)(currentPass / renderPasses) / (float)renderPasses - 0.5F) / 64.0F;
            GlStateManager.translate(offX, offY, 0.0F);

            this.applyPanoramaRotation(partialTicks, animationSpeed);

            for (int side = 0; side < 6; ++side) {
                GlStateManager.pushMatrix();

                float[] rot = SIDE_ROTATIONS[side];
                if (rot[0] != 0.0F) {
                    GlStateManager.rotate(rot[0], rot[1], rot[2], rot[3]);
                }
                this.mc.getTextureManager().bindTexture(titlePanoramaPaths[side]);
                worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                int alpha = 255 / (currentPass + 1);

                worldrenderer.pos(-1.0D, -1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, alpha).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, alpha).endVertex();
                worldrenderer.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, alpha).endVertex();
                worldrenderer.pos(-1.0D, 1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, alpha).endVertex();
                tessellator.draw();

                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }

        worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
    }

    /**
     * Calculates and applies the rotation based on the current mode.
     */
    private void applyPanoramaRotation(float partialTicks, float rotationSpeed) {
        float time = ((float)this.panoramaTimer + partialTicks) * rotationSpeed;

        float pitch, yaw;

        switch (currentMode) {
            case UP_AND_DOWN:
                pitch = MathHelper.sin(time / 400.0F) * 25.0F + 20.0F;
                yaw = -(time * 0.1F);
                break;
            case FLAT_SPIN:
                pitch = 20.0F;
                yaw = -(time * 0.1F);
                break;
            case STATIONARY:
                pitch = 20.0F;
                yaw = 0.0F;
                break;
            case REACTIVE:
                long currentTime = System.currentTimeMillis();
                float deltaTime = (currentTime - lastFrameTime) / 1000.0F;
                this.lastFrameTime = currentTime;
                this.lastYaw = lerp(this.lastYaw, -(((Mouse.getX() - centerX) / centerX) * 45.0F), 12F * deltaTime);
                this.lastPitch = lerp(this.lastPitch, -(((Mouse.getY() - centerY) / centerY) * 25.0F),12F * deltaTime);
                yaw = this.lastYaw;
                pitch = this.lastPitch;
                break;
            default:
                pitch = 0.0F;
                yaw = 0.0F;
        }

        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
    }

    /**
     * Rotate and blurs the skybox view in the main menu
     */
    private void rotateAndBlurSkybox() {
        this.mc.getTextureManager().bindTexture(this.backgroundTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, 256, 256);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.colorMask(true, true, true, false);
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        GlStateManager.disableAlpha();
        int blurPasses = 3;

        for (int currentPass = 0; currentPass < blurPasses; ++currentPass) {
            float alpha = 1.0F / (float)(currentPass + 1);
            float blurOff = (float)(currentPass - blurPasses / 2) / 256.0F;
            worldrenderer.pos(width, height, 0).tex((0.0F + blurOff), 1.0D).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            worldrenderer.pos(width, 0.0D, 0).tex((1.0F + blurOff), 1.0D).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            worldrenderer.pos(0.0D, 0.0D, 0).tex((1.0F + blurOff), 0.0D).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            worldrenderer.pos(0.0D, height, 0).tex((0.0F + blurOff), 0.0D).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.colorMask(true, true, true, true);
    }

    /**
     * Renders the skybox in the main menu
     */
    private void renderSkybox(float partialTicks) {
        this.mc.getFramebuffer().unbindFramebuffer();
        GlStateManager.viewport(0, 0, 256, 256);
        this.drawPanorama(partialTicks);
        for (int i = 0; i < 7; i++) {
            this.rotateAndBlurSkybox();
        }
        this.mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);

        float scale = width > height
                ? FOV / width
                : FOV / height;

        float uScale = height * scale / 256.0F;
        float vScale = width * scale / 256.0F;

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0D, height, 0).tex((0.5F - uScale), (0.5F + vScale)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(width, height, 0).tex((0.5F - uScale), (0.5F - vScale)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(width, 0.0D, 0).tex((0.5F + uScale), (0.5F - vScale)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(0.0D, 0.0D, 0).tex((0.5F + uScale), (0.5F + vScale)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        tessellator.draw();
    }

    private float lerp(float start, float end, float amount) {
        return start + amount * (end - start);
    }

    protected void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor)
    {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double)right, (double)top, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double)left, (double)top, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double)left, (double)bottom, 0).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos((double)right, (double)bottom, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}

