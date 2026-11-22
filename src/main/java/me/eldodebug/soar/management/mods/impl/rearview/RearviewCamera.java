package me.eldodebug.soar.management.mods.impl.rearview;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL11;

import me.eldodebug.soar.injection.interfaces.IMixinMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;

public class RearviewCamera {

    private Minecraft mc = Minecraft.getMinecraft();

    private int mirrorFBO;
    private int mirrorTex;
    private int mirrorDepth;
    private long renderEndNanoTime;
    private RenderGlobalHelper mirrorRenderGlobal;
    private float fov;
    private boolean firstUpdate, recording, lockCamera;

    public RearviewCamera() {
        mirrorFBO = ARBFramebufferObject.glGenFramebuffers();
        mirrorTex = GL11.glGenTextures();
        mirrorDepth = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorTex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB8, 800, 600, 0, GL11.GL_RGB,
                GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorDepth);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, 800, 600, 0,
                GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT, (ByteBuffer) null);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        mirrorRenderGlobal = new RenderGlobalHelper();
        fov = 70;
        lockCamera = true;
    }

    public void updateMirror() {

        int w, h;
        float y, py, p, pp;
        boolean hide;
        int view, limit;
        long endTime = 0;

        GuiScreen currentScreen;

        boolean texture2DWasEnabled = GL11.glGetBoolean(GL11.GL_TEXTURE_2D);
        boolean blendWasEnabled = GL11.glGetBoolean(GL11.GL_BLEND);
        boolean depthWasEnabled = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
        boolean lightingWasEnabled = GL11.glGetBoolean(GL11.GL_LIGHTING);
        int prevFBO = GL11.glGetInteger(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER_BINDING);
        IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewportBuffer);

        if (!this.firstUpdate) {
            mc.renderGlobal.loadRenderers();
            this.firstUpdate = true;
        }

        w = mc.displayWidth;
        h = mc.displayHeight;
        y = ((IMixinMinecraft)mc).getRenderViewEntity().rotationYaw;
        py = ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationYaw;
        p = ((IMixinMinecraft)mc).getRenderViewEntity().rotationPitch;
        pp = ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationPitch;
        hide = mc.gameSettings.hideGUI;
        view = mc.gameSettings.thirdPersonView;
        limit = mc.gameSettings.limitFramerate;
        fov = mc.gameSettings.fovSetting;
        currentScreen = mc.currentScreen;

        try {
            switchToFB();

            if (limit != 0) {
                endTime = renderEndNanoTime;
            }

            mc.currentScreen = null;
            mc.displayHeight = 600;
            mc.displayWidth = 800;
            mc.gameSettings.hideGUI = true;
            mc.gameSettings.thirdPersonView = 0;
            mc.gameSettings.limitFramerate = 0;
            mc.gameSettings.fovSetting = fov;

            ((IMixinMinecraft)mc).getRenderViewEntity().rotationYaw += 180;
            ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationYaw += 180;

            if(lockCamera) {
                ((IMixinMinecraft)mc).getRenderViewEntity().rotationPitch = 0;
                ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationPitch = 0;
            }else {
                ((IMixinMinecraft)mc).getRenderViewEntity().rotationPitch = -p + 18;
                ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationPitch = -pp + 18;
            }

            recording = true;
            mirrorRenderGlobal.switchTo();

            mc.entityRenderer.renderWorld(((IMixinMinecraft)mc).getTimer().renderPartialTicks, System.nanoTime());
            mc.entityRenderer.setupOverlayRendering();


            if (limit != 0) {
                renderEndNanoTime = endTime;
            }

            mirrorRenderGlobal.switchFrom();
            recording = false;

            mc.currentScreen = currentScreen;
            ((IMixinMinecraft)mc).getRenderViewEntity().rotationYaw = y;
            ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationYaw = py;
            ((IMixinMinecraft)mc).getRenderViewEntity().rotationPitch = p;
            ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationPitch = pp;
            mc.gameSettings.limitFramerate = limit;
            mc.gameSettings.thirdPersonView = view;
            mc.gameSettings.hideGUI = hide;
            mc.displayWidth = w;
            mc.displayHeight = h;
            mc.gameSettings.fovSetting = fov;
        } finally {
            if (texture2DWasEnabled) GlStateManager.enableTexture2D(); else GlStateManager.disableTexture2D();
            if (blendWasEnabled) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
            if (depthWasEnabled) GlStateManager.enableDepth(); else GlStateManager.disableDepth();
            if (lightingWasEnabled) GlStateManager.enableLighting(); else GlStateManager.disableLighting();
            GL11.glViewport(viewportBuffer.get(0), viewportBuffer.get(1), viewportBuffer.get(2), viewportBuffer.get(3));

            recording = false;
            switchFromFB(prevFBO);
        }
    }

    private void switchToFB() {

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();

        OpenGlHelper.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, mirrorFBO);
        OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER,
                OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D,
                mirrorTex, 0);
        OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER,
                OpenGlHelper.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D,
                mirrorDepth, 0);

        int status = OpenGlHelper.glCheckFramebufferStatus(OpenGlHelper.GL_FRAMEBUFFER);
        if (status != OpenGlHelper.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("[Rearview] Framebuffer incomplete, status: 0x" + Integer.toHexString(status));
        }
    }

    private void switchFromFB(int prevFBO) {
        OpenGlHelper.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, prevFBO);
    }

    public int getTexture() {
        return mirrorTex;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

    public void setLockCamera(boolean lockCamera) {
        this.lockCamera = lockCamera;
    }
}