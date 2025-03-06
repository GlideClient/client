/*
 * Nanovg Blur
 * © Shoroa 2025, All Rights Reserved
 */

package eu.shoroa.contrib.render;

import eu.shoroa.contrib.shader.UIShader;
import eu.shoroa.contrib.shader.uniform.Uniform;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.io.IOException;
import java.nio.FloatBuffer;

public class ShBlur {
    private static final ShBlur instance = new ShBlur();

    public static ShBlur getInstance() {
        return instance;
    }

    private int nvgImage = -1;
    private final Minecraft mc = Minecraft.getMinecraft();
    private Framebuffer framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
    private Framebuffer framebuffer1 = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
    private Framebuffer framebuffer2 = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
    private Framebuffer framebuffer3 = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
    private FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(128);
    private UIShader shader = new UIShader("soar/shaders/vertex.vert", "soar/shaders/blur.frag");
    private long lastUpdate = System.currentTimeMillis();
    private float radius = 4f;

    public void init() {
        framebuffer.createFramebuffer(mc.displayWidth / 2, mc.displayHeight / 2);
        framebuffer1.createFramebuffer(mc.displayWidth / 2, mc.displayHeight / 2);
        framebuffer2.createFramebuffer(mc.displayWidth / 6, mc.displayHeight / 6);
        framebuffer3.createFramebuffer(mc.displayWidth / 6, mc.displayHeight / 6);

        framebuffer.setFramebufferFilter(GL11.GL_LINEAR);
        framebuffer1.setFramebufferFilter(GL11.GL_LINEAR);
        framebuffer2.setFramebufferFilter(GL11.GL_LINEAR);
        framebuffer3.setFramebufferFilter(GL11.GL_LINEAR);

        cacheRadius(radius);
        try {
            shader.init();
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void cacheRadius(float radius) {
        weightBuffer = BufferUtils.createFloatBuffer(128);
        for (int i = 0; i < radius; i++) {
            weightBuffer.put(gauss((float) i, radius / 2f));
        }
        weightBuffer.rewind();
    }

    public void resize() {
        framebuffer.deleteFramebuffer();
        framebuffer = new Framebuffer(mc.displayWidth / 2, mc.displayHeight / 2, false);
        framebuffer1.deleteFramebuffer();
        framebuffer1 = new Framebuffer(mc.displayWidth / 2, mc.displayHeight / 2, false);
        framebuffer2.deleteFramebuffer();
        framebuffer2 = new Framebuffer(mc.displayWidth / 6, mc.displayHeight / 6, false);
        framebuffer3.deleteFramebuffer();
        framebuffer3 = new Framebuffer(mc.displayWidth / 6, mc.displayHeight / 6, false);

        framebuffer.setFramebufferFilter(GL11.GL_LINEAR);
        framebuffer1.setFramebufferFilter(GL11.GL_LINEAR);
        framebuffer2.setFramebufferFilter(GL11.GL_LINEAR);
        framebuffer3.setFramebufferFilter(GL11.GL_LINEAR);
    }

    private int nvgImageFromHandle(int texture, int width, int height, int flags) {
        return NanoVGGL2.nvglCreateImageFromHandle(Glide.getInstance().getNanoVGManager().getContext(), texture, width, height, flags);
    }

    public void render() {
        if (nvgImage == -1) {
            nvgImage = nvgImageFromHandle(
                    framebuffer3.framebufferTexture, mc.displayWidth, mc.displayHeight, NanoVG.NVG_IMAGE_FLIPY
            );
        }

        ScaledResolution sr = new ScaledResolution(mc);
        if (System.currentTimeMillis() - lastUpdate > 15) {
            lastUpdate = System.currentTimeMillis();
            framebuffer.bindFramebuffer(true);
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

            shader.attach();
            bindTexture(mc.getFramebuffer().framebufferTexture, 10);
            shader.uniform(Uniform.makeInt("texture", 10));
            shader.uniform(Uniform.makeVec2("direction", 1f, 0f));
            shader.uniform(Uniform.makeVec2(
                    "texelSize", 1f / mc.getFramebuffer().framebufferWidth,
                    1f / mc.getFramebuffer().framebufferHeight
            ));
            shader.uniform(Uniform.makeFloatBuffer("kernels", weightBuffer));
            shader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());

            framebuffer1.bindFramebuffer(true);
            bindTexture(framebuffer.framebufferTexture, 10);
            shader.uniform(Uniform.makeInt("texture", 10));
            shader.uniform(Uniform.makeVec2("direction", 0f, 1f));
            shader.uniform(Uniform.makeVec2("texelSize", 1f / framebuffer1.framebufferWidth, 1f / framebuffer1.framebufferHeight));
            shader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());

            framebuffer2.bindFramebuffer(true);
            bindTexture(framebuffer1.framebufferTexture, 10);
            shader.uniform(Uniform.makeInt("texture", 10));
            shader.uniform(Uniform.makeVec2("direction", 1f, 0f));
            shader.uniform(Uniform.makeVec2("texelSize", 1f / framebuffer2.framebufferWidth, 1f / framebuffer2.framebufferHeight));
            shader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());

            framebuffer3.bindFramebuffer(true);
            bindTexture(framebuffer2.framebufferTexture, 10);
            shader.uniform(Uniform.makeInt("texture", 10));
            shader.uniform(Uniform.makeVec2("direction", 0f, 1f));
            shader.uniform(Uniform.makeVec2("texelSize", 1f / framebuffer3.framebufferWidth, 1f / framebuffer3.framebufferHeight));
            shader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());

            shader.detach();
            GL11.glPopAttrib();
        }
        mc.getFramebuffer().bindFramebuffer(true);
    }

    private void bindTexture(int texture, int id) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + id);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    }

    private float gauss(float x, float sigma) {
        double PI = 3.141592653;
        double output = 1.0 / Math.sqrt(2.0 * PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }

    public void drawBlur(float x, float y, float w, float h, float radius) {
        long ctx = Glide.getInstance().getNanoVGManager().getContext();
        ScaledResolution sr = new ScaledResolution(mc);

        ComboSetting setting = InternalSettingsMod.getInstance().getModThemeSetting();
        Option theme = setting.getOption();
        boolean rectShape = theme.getTranslate().equals(TranslateText.RECT) || theme.getTranslate().equals(TranslateText.GRADIENT_SIMPLE);

        NVGPaint paint = NVGPaint.calloc();

        NanoVG.nvgBeginPath(ctx);
        if (rectShape) {
            NanoVG.nvgRect(ctx, x, y, w, h);
        } else {
            NanoVG.nvgRoundedRect(ctx, x, y, w, h, radius);
        }
        NanoVG.nvgImagePattern(ctx, 0f, 0f, sr.getScaledWidth(), sr.getScaledHeight(), 0f, nvgImage, 1f, paint);
        NanoVG.nvgFillPaint(ctx, paint);
        NanoVG.nvgFill(ctx);
        NanoVG.nvgClosePath(ctx);

        paint.free();
    }

    public void drawBlur(Runnable r) {
        long ctx = Glide.getInstance().getNanoVGManager().getContext();
        ScaledResolution sr = new ScaledResolution(mc);
        NVGPaint paint = NVGPaint.calloc();
        NanoVG.nvgBeginPath(ctx);
        r.run();
        NanoVG.nvgImagePattern(ctx, 0f, 0f, sr.getScaledWidth(), sr.getScaledHeight(), 0f, nvgImage, 1f, paint);
        NanoVG.nvgFillPaint(ctx, paint);
        NanoVG.nvgFill(ctx);
        NanoVG.nvgClosePath(ctx);

        paint.free();
    }
}
