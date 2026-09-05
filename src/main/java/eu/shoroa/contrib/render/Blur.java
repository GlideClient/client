/*
 * Nanovg Blur
 * © Shoroa 2026, All Rights Reserved
 */

package eu.shoroa.contrib.render;

import eu.shoroa.contrib.shader.UIShader;
import eu.shoroa.contrib.shader.uniform.Uniform;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.types.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import java.io.IOException;

public class Blur {
    private static final UIShader shader = new UIShader("soar/shaders/vertex.vert", "soar/shaders/blur.frag");

    private static Framebuffer fboHalf = new Framebuffer(Minecraft.getMinecraft().displayWidth / 2, Minecraft.getMinecraft().displayHeight / 2, false);
    private static Framebuffer fboQuart = new Framebuffer(Minecraft.getMinecraft().displayWidth / 4, Minecraft.getMinecraft().displayHeight / 4, false);
    private static Framebuffer fboEighth = new Framebuffer(Minecraft.getMinecraft().displayWidth / 8, Minecraft.getMinecraft().displayHeight / 8, false);

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static int nvgImage = -1;

    public static void init() {
        try {
            shader.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        createFbos();
    }

    private static void createFbos() {
        int w = mc.displayWidth;
        int h = mc.displayHeight;

        fboHalf.deleteFramebuffer();
        fboHalf = new Framebuffer(w / 2, h / 2, false);
        fboQuart.deleteFramebuffer();
        fboQuart = new Framebuffer(w / 4, h / 4, false);
        fboEighth.deleteFramebuffer();
        fboEighth = new Framebuffer(Math.max(w / 8, 1), Math.max(h / 8, 1), false);

        fboHalf.setFramebufferFilter(GL11.GL_LINEAR);
        fboQuart.setFramebufferFilter(GL11.GL_LINEAR);
        fboEighth.setFramebufferFilter(GL11.GL_LINEAR);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fboHalf.framebufferTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fboQuart.framebufferTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fboEighth.framebufferTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    private static int nvgImageFromHandle(int texture, int width, int height) {
        return NanoVGGL2.nvglCreateImageFromHandle(Glide.getInstance().getNanoVGManager().getContext(), texture, width, height, NanoVG.NVG_IMAGE_FLIPY);
    }

    public static void resize() {
        createFbos();
    }

    public static void render() {
        render(InternalSettingsMod.getInstance().getBlurStrengthSetting().getValueFloat());
    }

    public static void render(float strength) {
        if (nvgImage == -1) {
            nvgImage = nvgImageFromHandle(fboHalf.framebufferTexture, mc.displayWidth, mc.displayHeight);
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        ScaledResolution sr = new ScaledResolution(mc);

        fboHalf.framebufferClear();
        fboHalf.bindFramebuffer(true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        mc.getFramebuffer().bindFramebufferTexture();
        shader.attach();
        shader.uniform(Uniform.makeInt("uTex", 0));
        shader.uniform(Uniform.makeVec2("uResolution", mc.displayWidth, mc.displayHeight));
        shader.uniform(Uniform.makeFloat("uRadius", 0.5f * strength / 4f));
        shader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());
        shader.detach();

        fboQuart.framebufferClear();
        fboQuart.bindFramebuffer(true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        fboHalf.bindFramebufferTexture();
        shader.attach();
        shader.uniform(Uniform.makeInt("uTex", 0));
        shader.uniform(Uniform.makeVec2("uResolution", mc.displayWidth / 2f, mc.displayHeight / 2f));
        shader.uniform(Uniform.makeFloat("uRadius", 0.5f * strength / 2f));
        shader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());
        shader.detach();

        fboEighth.framebufferClear();
        fboEighth.bindFramebuffer(true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        fboQuart.bindFramebufferTexture();
        shader.attach();
        shader.uniform(Uniform.makeInt("uTex", 0));
        shader.uniform(Uniform.makeVec2("uResolution", mc.displayWidth / 4f, mc.displayHeight / 4f));
        shader.uniform(Uniform.makeFloat("uRadius", 0.5f * strength));
        shader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());
        shader.detach();

        fboQuart.framebufferClear();
        fboQuart.bindFramebuffer(true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        fboEighth.bindFramebufferTexture();
        shader.attach();
        shader.uniform(Uniform.makeInt("uTex", 0));
        shader.uniform(Uniform.makeVec2("uResolution", mc.displayWidth / 8f, mc.displayHeight / 8f));
        shader.uniform(Uniform.makeFloat("uRadius", 0.5f * strength));
        shader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());
        shader.detach();

        fboHalf.framebufferClear();
        fboHalf.bindFramebuffer(true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        fboQuart.bindFramebufferTexture();
        shader.attach();
        shader.uniform(Uniform.makeInt("uTex", 0));
        shader.uniform(Uniform.makeVec2("uResolution", mc.displayWidth / 4f, mc.displayHeight / 4f));
        shader.uniform(Uniform.makeFloat("uRadius", 0.5f * strength / 2));
        shader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());
        shader.detach();

        mc.getFramebuffer().bindFramebuffer(true);
        GL11.glPopAttrib();
    }

    public static void drawBlurMod(float x, float y, float w, float h, float radius) {
        if (!InternalSettingsMod.getInstance().getBlurSetting().isToggled()) return;
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

    public static void drawBlurMod(Rect rect, float radius) {
        drawBlurRounded(rect.x, rect.y, rect.width, rect.height, radius);
    }

    public static void drawBlur(Runnable r) {
        if (!InternalSettingsMod.getInstance().getBlurSetting().isToggled()) return;
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

    public static void drawBlurRounded(float x, float y, float w, float h, float radius) {
        if (!InternalSettingsMod.getInstance().getBlurSetting().isToggled()) return;
        long ctx = Glide.getInstance().getNanoVGManager().getContext();
        ScaledResolution sr = new ScaledResolution(mc);
        NVGPaint paint = NVGPaint.calloc();

        NanoVG.nvgBeginPath(ctx);
        NanoVG.nvgRoundedRect(ctx, x, y, w, h, radius);
        NanoVG.nvgImagePattern(ctx, 0f, 0f, sr.getScaledWidth(), sr.getScaledHeight(), 0f, nvgImage, 1f, paint);
        NanoVG.nvgFillPaint(ctx, paint);
        NanoVG.nvgFill(ctx);
        NanoVG.nvgClosePath(ctx);

        paint.free();
    }

    public static void drawBlurRect(float x, float y, float w, float h) {
        if (!InternalSettingsMod.getInstance().getBlurSetting().isToggled()) return;
        long ctx = Glide.getInstance().getNanoVGManager().getContext();
        ScaledResolution sr = new ScaledResolution(mc);
        NVGPaint paint = NVGPaint.calloc();

        NanoVG.nvgBeginPath(ctx);
        NanoVG.nvgRect(ctx, x, y, w, h);
        NanoVG.nvgImagePattern(ctx, 0f, 0f, sr.getScaledWidth(), sr.getScaledHeight(), 0f, nvgImage, 1f, paint);
        NanoVG.nvgFillPaint(ctx, paint);
        NanoVG.nvgFill(ctx);
        NanoVG.nvgClosePath(ctx);

        paint.free();
    }

    public static void drawScreen(NanoVGManager nvg, ScaledResolution sr, float blurRadius) {
        if (!InternalSettingsMod.getInstance().getBlurSetting().isToggled()) return;

        render(blurRadius);

        nvg.setupAndDraw(() -> {
            drawBlurRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight());
        });
    }
}
