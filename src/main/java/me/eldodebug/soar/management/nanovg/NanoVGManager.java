package me.eldodebug.soar.management.nanovg;

/*
 * Copyright (C) 2021-2024 Polyfrost Inc. and contributors.
 * <https://polyfrost.org> <https://github.com/Polyfrost>
 *
 *  OneConfig is licensed under the terms of version 3 of the GNU Lesser
 * General Public License as published by the Free Software Foundation, AND
 * under the Additional Terms Applicable to OneConfig, as published by Polyfrost Inc.,
 * either version 1.1 of the Additional Terms, or (at your option) any later
 * version.
 *
 * A copy of version 3 of the GNU Lesser General Public License is
 * found below, along with the Additional Terms Applicable to OneConfig.
 * A copy of version 3 of the GNU General Public License, which supplements
 * version 3 of the GNU Lesser General Public License, is also found below.
 *
 * https://github.com/Polyfrost/OneConfig/blob/develop-v0/LICENSE
 */

import java.awt.Color;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import me.eldodebug.soar.types.CircQueue;
import me.eldodebug.soar.types.Rect;
import me.eldodebug.soar.types.Size;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.opengl.GL11;

import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.management.nanovg.asset.AssetManager;
import me.eldodebug.soar.management.nanovg.font.Font;
import me.eldodebug.soar.management.nanovg.font.FontManager;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.system.MemoryUtil;

public class NanoVGManager {

    private final Minecraft mc = Minecraft.getMinecraft();

    private final FloatBuffer f4Buff = MemoryUtil.memAllocFloat(4);
    private final IntBuffer i1buff1 = MemoryUtil.memAllocInt(1);
    private final IntBuffer i1buff2 = MemoryUtil.memAllocInt(1);
    private final FloatBuffer f1Buff1 = MemoryUtil.memAllocFloat(1);
    private final FloatBuffer f1Buff2 = MemoryUtil.memAllocFloat(1);

    private final CircQueue<NVGColor> colorQueue = new CircQueue<>(NVGColor.calloc(), NVGColor.calloc(), NVGColor.calloc(), NVGColor.calloc());
    private final CircQueue<NVGPaint> paintQueue = new CircQueue<>(NVGPaint.calloc(), NVGPaint.calloc(), NVGPaint.calloc(), NVGPaint.calloc());

    private final long nvg;

    private final FontManager fontManager;
    private final AssetManager assetManager;

    public NanoVGManager() {

        nvg = NanoVGGL2.nvgCreate(NanoVGGL2.NVG_ANTIALIAS | NanoVGGL2.NVG_STENCIL_STROKES);

        if (nvg == 0) {
            GlideLogger.error("Failed to create NanoVG context");
            mc.shutdown();
        }

        fontManager = new FontManager();
        fontManager.init(nvg);

        assetManager = new AssetManager();
    }

    public void destroy() {
        NanoVGGL2.nvgDelete(nvg);

        for (int i = 0; i < 4; i++) {
            colorQueue.poll().free();
            paintQueue.poll().free();
        }

        MemoryUtil.memFree(f4Buff);
        MemoryUtil.memFree(i1buff1);
        MemoryUtil.memFree(i1buff2);
        MemoryUtil.memFree(f1Buff1);
        MemoryUtil.memFree(f1Buff2);
    }

    public NVGColor getColor(Color color) {
        return getColor(color.getRGB());
    }

    public NVGColor getColor(float r, float g, float b, float a) {
        NVGColor nvgColor = colorQueue.poll();
        nvgColor.r(r);
        nvgColor.g(g);
        nvgColor.b(b);
        nvgColor.a(a);

        return nvgColor;
    }

    public NVGColor getColor(int color) {
        return getColor((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, ((color >> 24 & 0xFF) / 255f));
    }

    public NVGPaint getAvailablePaint() {
        return paintQueue.poll();
    }

    public void imageSize(int imageId, Size src) {
        NanoVG.nvgImageSize(nvg, imageId, i1buff1, i1buff2);
        src.set(i1buff1.get(0), i1buff2.get(0));
    }

    public void setupAndDraw(Runnable task, boolean scale) {

        ScaledResolution sr = new ScaledResolution(mc);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        NanoVG.nvgBeginFrame(nvg, mc.displayWidth, mc.displayHeight, 1);

        if (scale) {
            NanoVG.nvgScale(nvg, sr.getScaleFactor(), sr.getScaleFactor());
        }

        task.run();

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        NanoVG.nvgEndFrame(nvg);
        GL11.glPopAttrib();
    }

    public void setupAndDraw(Runnable task) {
        setupAndDraw(task, true);
    }

    public void drawAlphaBar(float x, float y, float width, float height, float radius, Color color) {
        drawAlphaBar(x, y, width, height, radius, color.getRGB());
    }

    public void drawAlphaBar(float x, float y, float width, float height, float radius, int color) {
        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
        NVGColor nvgColor = getColor(color);
        NVGColor nvgColor2 = getColor(0);
        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x, y, x + width, y, nvgColor2, nvgColor, getAvailablePaint()));
        NanoVG.nvgFill(nvg);
    }

    public void drawHSBBox(float x, float y, float width, float height, float radius, Color color) {
        drawHSBBox(x, y, width, height, radius, color.getRGB());
    }

    public void drawHSBBox(float x, float y, float width, float height, float radius, int color) {
        drawRoundedRect(x, y, width, height, radius, color);

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
        NVGColor nvgColor = getColor(-1);
        NVGColor nvgColor2 = getColor(0);
        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x + 8, y + 8, x + width, y, nvgColor, nvgColor2, getAvailablePaint()));
        NanoVG.nvgFill(nvg);

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
        NVGColor nvgColor3 = getColor(0);
        NVGColor nvgColor4 = getColor(0xFF000000);

        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x + 8, y + 8, x, y + height, nvgColor3, nvgColor4, getAvailablePaint()));
        NanoVG.nvgFill(nvg);
    }

    public void drawRect(float x, float y, float width, float height, Color color) {
        drawRect(x, y, width, height, color.getRGB());
    }

    public void drawRect(float x, float y, float width, float height, int color) {

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRect(nvg, x, y, width, height);

        NVGColor nvgColor = getColor(color);

        NanoVG.nvgFillColor(nvg, nvgColor);
        NanoVG.nvgFill(nvg);
    }

    public void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        drawRoundedRect(x, y, width, height, radius, color.getRGB());
    }

    public void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);

        NVGColor nvgColor = getColor(color);

        NanoVG.nvgFillColor(nvg, nvgColor);
        NanoVG.nvgFill(nvg);
    }

    public void drawRoundedRect(Rect rect, float radius, int color) {
        drawRoundedRect(rect.x, rect.y, rect.width, rect.height, radius, color);
    }

    public void drawRoundedRect(Rect rect, float radius, Color color) {
        drawRoundedRect(rect.x, rect.y, rect.width, rect.height, radius, color);
    }

    public void drawRoundedRectVarying(float x, float y, float width, float height, float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius, Color color) {
        drawRoundedRectVarying(x, y, width, height, topLeftRadius, topRightRadius, bottomLeftRadius, bottomRightRadius, color.getRGB());
    }

    public void drawRoundedRectVarying(float x, float y, float width, float height, float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius, int color) {

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRoundedRectVarying(nvg, x, y, width, height, topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);

        NVGColor nvgColor = getColor(color);

        NanoVG.nvgFillColor(nvg, nvgColor);
        NanoVG.nvgFill(nvg);
    }

    public void drawVerticalGradientRect(float x, float y, float width, float height, Color color1, Color color2) {
        drawVerticalGradientRect(x, y, width, height, color1.getRGB(), color2.getRGB());
    }

    public void drawVerticalGradientRect(float x, float y, float width, float height, int color1, int color2) {
        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRect(nvg, x, y, width, height);

        NVGColor nvgColor1 = getColor(color1);
        NVGColor nvgColor2 = getColor(color2);

        NanoVG.nvgFillColor(nvg, nvgColor1);
        NanoVG.nvgFillColor(nvg, nvgColor2);

        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x, y, x, y + height, nvgColor1, nvgColor2, getAvailablePaint()));
        NanoVG.nvgFill(nvg);
    }

    public void drawHorizontalGradientRect(float x, float y, float width, float height, Color color1, Color color2) {
        drawHorizontalGradientRect(x, y, width, height, color1.getRGB(), color2.getRGB());
    }

    public void drawHorizontalGradientRect(float x, float y, float width, float height, int color1, int color2) {

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRect(nvg, x, y, width, height);

        NVGColor nvgColor1 = getColor(color1);
        NVGColor nvgColor2 = getColor(color2);

        NanoVG.nvgFillColor(nvg, nvgColor1);
        NanoVG.nvgFillColor(nvg, nvgColor2);

        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x, y, x + width, y, nvgColor1, nvgColor2, getAvailablePaint()));
        NanoVG.nvgFill(nvg);
    }

    public void drawGradientRect(float x, float y, float width, float height, Color color1, Color color2) {
        drawGradientRect(x, y, width, height, color1.getRGB(), color2.getRGB());
    }

    public void drawGradientRect(float x, float y, float width, float height, int color1, int color2) {
        float tick = ((System.currentTimeMillis() % 3600) / 570F);
        float max = Math.max(width, height);

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRect(nvg, x, y, width, height);

        NVGColor nvgColor1 = getColor(color1);
        NVGColor nvgColor2 = getColor(color2);

        NanoVG.nvgFillColor(nvg, nvgColor1);
        NanoVG.nvgFillColor(nvg, nvgColor2);

        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x + width / 2 - (max / 2) * MathUtils.cos(tick), y + height / 2 - (max / 2) * MathUtils.sin(tick), x + width / 2 + (max / 2) * MathUtils.cos(tick), y + height / 2 + (max + 2f) * MathUtils.sin(tick), nvgColor1, nvgColor2, getAvailablePaint()));
        NanoVG.nvgFill(nvg);
    }

    public void drawGradientRoundedRect(float x, float y, float width, float height, float radius, Color color1, Color color2) {
        drawGradientRoundedRect(x, y, width, height, radius, color1.getRGB(), color2.getRGB());
    }

    public void drawGradientRoundedRect(float x, float y, float width, float height, float radius, int color1, int color2) {
        float tick = ((System.currentTimeMillis() % 3600) / 570F);
        float max = Math.max(width, height);

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);

        NVGColor nvgColor1 = getColor(color1);
        NVGColor nvgColor2 = getColor(color2);

        NanoVG.nvgFillColor(nvg, nvgColor1);
        NanoVG.nvgFillColor(nvg, nvgColor2);

        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x + width / 2 - (max / 2) * MathUtils.cos(tick), y + height / 2 - (max / 2) * MathUtils.sin(tick), x + width / 2 + (max / 2) * MathUtils.cos(tick), y + height / 2 + (max + 2f) * MathUtils.sin(tick), nvgColor1, nvgColor2, getAvailablePaint()));
        NanoVG.nvgFill(nvg);
    }

    public void drawOutlineRoundedRect(float x, float y, float width, float height, float radius, float strokeWidth, Color color) {
        drawOutlineRoundedRect(x, y, width, height, radius, strokeWidth, color.getRGB());
    }

    public void drawOutlineRoundedRect(float x, float y, float width, float height, float radius, float strokeWidth, int color) {

        if (radius < 0.5f) {
            radius = 0.5f;
        }

        NVGColor nvgColor = getColor(color);

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRoundedRect(nvg, x - strokeWidth / 2f, y - strokeWidth / 2f, width + strokeWidth, height + strokeWidth, radius + strokeWidth / 2f);
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
        NanoVG.nvgPathWinding(nvg, NanoVG.NVG_HOLE);
        NanoVG.nvgFillColor(nvg, nvgColor);
        NanoVG.nvgFill(nvg);
    }

    public void drawGradientOutlineRoundedRect(float x, float y, float width, float height, float radius, float strokeWidth, Color color1, Color color2) {
        drawGradientOutlineRoundedRect(x, y, width, height, radius, strokeWidth, color1.getRGB(), color2.getRGB());
    }

    public void drawGradientOutlineRoundedRect(float x, float y, float width, float height, float radius, float strokeWidth, int color1, int color2) {
        float tick = ((System.currentTimeMillis() % 3600) / 570F);
        float max = Math.max(width, height);

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);

        NVGColor nvgColor1 = getColor(color1);
        NVGColor nvgColor2 = getColor(color2);

        NanoVG.nvgFillColor(nvg, nvgColor1);
        NanoVG.nvgFillColor(nvg, nvgColor2);

        NanoVG.nvgStrokeWidth(nvg, strokeWidth);
        NanoVG.nvgStrokePaint(nvg, NanoVG.nvgLinearGradient(nvg, x + width / 2 - (max / 2) * MathUtils.cos(tick), y + height / 2 - (max / 2) * MathUtils.sin(tick), x + width / 2 + (max / 2) * MathUtils.cos(tick), y + height / 2 + (max + 2f) * MathUtils.sin(tick), nvgColor1, nvgColor2, getAvailablePaint()));
        NanoVG.nvgStroke(nvg);
    }

    public void drawArrow(float x, float y, float size, float angle, Color color) {
        drawArrow(x, y, size, angle, color.getRGB());
    }

    public void drawArrow(float x, float y, float size, float angle, int color) {

        save();

        NanoVG.nvgBeginPath(nvg);

        float offsetX = (float) (size * Math.cos(Math.toRadians(angle)));
        float offsetY = (float) (size * Math.sin(Math.toRadians(angle)));

        float diffX = x + (offsetX / 2);
        float diffY = y + (offsetY / 2);

        NanoVG.nvgTranslate(nvg, diffX, diffY);
        NanoVG.nvgRotate(nvg, (float) Math.toRadians(angle));

        NanoVG.nvgMoveTo(nvg, -size, -size / 2);
        NanoVG.nvgLineTo(nvg, 0, 0);
        NanoVG.nvgLineTo(nvg, -size, size / 2);

        NanoVG.nvgStrokeWidth(nvg, 0.8F);
        NanoVG.nvgStrokeColor(nvg, getColor(color));
        NanoVG.nvgStroke(nvg);

        restore();
    }


    public void drawShadow(float x, float y, float width, float height, float radius, int strength) {
        NVGPaint bg = getAvailablePaint();

        NanoVG.nvgBoxGradient(nvg, x, y, width, height, radius, strength * 2, getColor(0x32000000), getColor(0), bg);
        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRect(nvg, x - strength, y - strength, width + strength * 2, height + strength * 2);
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
        NanoVG.nvgPathWinding(nvg, NanoVG.NVG_HOLE);
        NanoVG.nvgFillPaint(nvg, bg);
        NanoVG.nvgFill(nvg);
    }

    public void drawShadow(float x, float y, float width, float height, float radius) {
        drawShadow(x, y, width, height, radius, 7);
    }

    public void drawGradientShadow(float x, float y, float width, float height, float radius, Color color1, Color color2) {
        drawGradientShadow(x, y, width, height, radius, color1.getRGB(), color2.getRGB());
    }

    public void drawGradientShadow(float x, float y, float width, float height, float radius, int color1, int color2) {

        int alpha = 1;

        for (float f = 10; f > 0; f--) {
            drawGradientOutlineRoundedRect(x - (f / 2), y - (f / 2), width + f, height + f, radius + 2, f, ColorUtils.applyAlpha(color1, alpha), ColorUtils.applyAlpha(color2, alpha));

            alpha += 3;
        }
    }

    public void drawRoundedGlow(float x, float y, float width, float height, float radius, Color color1, int strength) {
        drawRoundedGlow(x, y, width, height, radius, color1.getRGB(), strength);
    }

    public void drawRoundedGlow(float x, float y, float width, float height, float radius, int color1, int strength) {

        int alpha = 1;

        for (float f = strength; f > 0; f--) {
            drawGradientOutlineRoundedRect(x - (f / 2), y - (f / 2), width + f, height + f, radius + 2, f, ColorUtils.applyAlpha(color1, alpha), ColorUtils.applyAlpha(color1, alpha));

            alpha += 2;
        }
    }

    public void drawCircle(float x, float y, float radius, Color color) {
        drawCircle(x, y, radius, color.getRGB());
    }

    public void drawCircle(float x, float y, float radius, int color) {

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgCircle(nvg, x, y, radius);

        NVGColor nvgColor = getColor(color);

        NanoVG.nvgFillColor(nvg, nvgColor);
        NanoVG.nvgFill(nvg);
    }

    public void drawArc(float x, float y, float radius, float startAngle, float endAngle, float strokeWidth, Color color) {
        drawArc(x, y, radius, startAngle, endAngle, strokeWidth, color.getRGB());
    }

    public void drawArc(float x, float y, float radius, float startAngle, float endAngle, float strokeWidth, int color) {

        NVGColor nvgColor = getColor(color);

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgArc(nvg, x, y, radius, (float) Math.toRadians(startAngle), (float) Math.toRadians(endAngle), NanoVG.NVG_CW);
        NanoVG.nvgStrokeWidth(nvg, strokeWidth);
        NanoVG.nvgStrokeColor(nvg, nvgColor);
        NanoVG.nvgStroke(nvg);
    }

    public void drawGradientCircle(float x, float y, float radius, Color color1, Color color2) {
        drawGradientCircle(x, y, radius, color1.getRGB(), color2.getRGB());
    }

    public void drawGradientCircle(float x, float y, float radius, int color1, int color2) {
        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgCircle(nvg, x, y, radius);

        NVGColor nvgColor1 = getColor(color1);
        NVGColor nvgColor2 = getColor(color2);

        NanoVG.nvgFillColor(nvg, nvgColor1);
        NanoVG.nvgFillColor(nvg, nvgColor2);

        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x, y, radius, radius, nvgColor1, nvgColor2, getAvailablePaint()));
        NanoVG.nvgFill(nvg);
    }

    public void fontBlur(float blur) {
        NanoVG.nvgFontBlur(nvg, blur);
    }

    public void drawText(String text, float x, float y, Color color, float size, Font font) {
        drawText(text, x, y, color.getRGB(), size, font);
    }

    public void drawText(String text, float x, float y, int color, float size, Font font) {
        y += size / 2;

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgFontSize(nvg, size);
        NanoVG.nvgFontFace(nvg, font.getName());
        NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);

        NVGColor nvgColor = getColor(color);

        NanoVG.nvgFillColor(nvg, nvgColor);
        NanoVG.nvgText(nvg, x, y, text);
    }

    public void drawBlurredText(String text, float x, float y, Color color, float blurRadius, float size, int align, Font font) {
        drawBlurredText(text, x, y, color.getRGB(), blurRadius, size, align, font);
    }

    public void drawBlurredText(String text, float x, float y, int color, float blurRadius, float size, int align, Font font) {
//        y += size / 2;

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgFontBlur(nvg, blurRadius);
        NanoVG.nvgFontSize(nvg, size);
        NanoVG.nvgFontFace(nvg, font.getName());
        NanoVG.nvgTextAlign(nvg, align);

        NVGColor nvgColor = getColor(color);

        NanoVG.nvgFillColor(nvg, nvgColor);
        NanoVG.nvgText(nvg, x, y, text);
        NanoVG.nvgFontBlur(nvg, 0f);
    }

    public void drawTextWithShadow(String text, float x, float y, int color, float blurRadius, float size, Font font) {
        save();
        fontBlur(blurRadius);
        drawText(text, x, y, 0x62000000, size, font);
        fontBlur(0);
        restore();
        drawText(text, x, y, color, size, font);
    }

    public void drawCenteredTextWithShadow(String text, float x, float y, int color, float blurRadius, float size, Font font) {
        save();
        fontBlur(blurRadius);
        drawCenteredText(text, x, y, 0x62000000, size, font);
        fontBlur(0);
        restore();
        drawCenteredText(text, x, y, color, size, font);
    }

    public void drawTextGlowing(String text, float x, float y, Color color, float blurRadius, float size, Font font) {
        drawTextGlowing(text, x, y, color.getRGB(), blurRadius, size, font);
    }

    public void drawTextGlowing(String text, float x, float y, int color, float blurRadius, float size, Font font) {
        drawTextGlowingBg(text, x, y, color, size, blurRadius, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE, font);
        drawText(text, x, y, color, size, font);
    }

    public void drawCenteredTextGlowing(String text, float x, float y, Color color, float blurRadius, float size, Font font) {
        drawCenteredTextGlowing(text, x, y, color.getRGB(), blurRadius, size, font);
    }

    public void drawCenteredTextGlowing(String text, float x, float y, int color, float blurRadius, float size, Font font) {
        drawTextGlowingBg(text, x, y, color, size, blurRadius, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE, font);
        drawCenteredText(text, x, y, color, size, font);
    }

    private void drawTextGlowingBg(String text, float x, float y, Color color, float size, float blurRadius, int align, Font font) {
        drawTextGlowingBg(text, x, y, color.getRGB(), size, blurRadius, align, font);
    }

    private void drawTextGlowingBg(String text, float x, float y, int color, float size, float blurRadius, int align, Font font) {
        y += size / 2;

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgFontSize(nvg, size);
        NanoVG.nvgFontFace(nvg, font.getName());
        NanoVG.nvgTextAlign(nvg, align);
        NVGColor nvgColor = getColor(color);
        NanoVG.nvgFillColor(nvg, nvgColor);
        save();
        fontBlur(blurRadius);
        NanoVG.nvgText(nvg, x, y, text);
        restore();
    }


    public void drawTextBox(String text, float x, float y, float maxWidth, Color color, float size, Font font) {
        drawTextBox(text, x, y, maxWidth, color.getRGB(), size, font);
    }

    public void drawTextBox(String text, float x, float y, float maxWidth, int color, float size, Font font) {

        y += size / 2;

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgFontSize(nvg, size);
        NanoVG.nvgFontFace(nvg, font.getName());
        NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
        NVGColor nvgColor = getColor(color);

        NanoVG.nvgFillColor(nvg, nvgColor);
        NanoVG.nvgTextBox(nvg, x, y, maxWidth, text);
    }


    public void drawCenteredText(String text, float x, float y, Color color, float size, Font font) {
        drawCenteredText(text, x, y, color.getRGB(), size, font);
    }

    public void drawCenteredText(String text, float x, float y, int color, float size, Font font) {
//        y += size / 2;

        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgFontSize(nvg, size);
        NanoVG.nvgFontFace(nvg, font.getName());
        NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);

        NVGColor nvgColor = getColor(color);

        NanoVG.nvgFillColor(nvg, nvgColor);
        NanoVG.nvgText(nvg, x, y, text);
    }

    public float getTextWidth(String text, float size, Font font) {
        NanoVG.nvgFontSize(nvg, size);
        NanoVG.nvgFontFace(nvg, font.getName());
        NanoVG.nvgTextBounds(nvg, 0, 0, text, f4Buff);
        NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);

        return f4Buff.get(2) - f4Buff.get(0);
    }


    public float getTextHeight(String text, float size, Font font) {
        NanoVG.nvgFontSize(nvg, size);
        NanoVG.nvgFontFace(nvg, font.getName());
        NanoVG.nvgTextBounds(nvg, 0, 0, text, f4Buff);

        return f4Buff.get(3) - f4Buff.get(1);
    }

    public float getTextBoxHeight(String text, float size, Font font, float maxWidth) {
        NanoVG.nvgFontSize(nvg, size);
        NanoVG.nvgFontFace(nvg, font.getName());
        NanoVG.nvgTextBoxBounds(nvg, 0, 0, maxWidth, text, f4Buff);

        return f4Buff.get(3) - f4Buff.get(1);
    }

    public String getLimitText(String inputText, float fontSize, Font font, float width) {

        String text = inputText;
        boolean isInRange = false;
        boolean isRemoved = false;

        while (!isInRange) {

            if (getTextWidth(text, fontSize, font) > width) {
                text = text.substring(0, text.length() - 1);
                isRemoved = true;
            } else {
                isInRange = true;
            }
        }

        return text + (isRemoved ? "..." : "");
    }

    public void scale(float x, float y, float scale) {
        NanoVG.nvgTranslate(nvg, x, y);
        NanoVG.nvgScale(nvg, scale, scale);
        NanoVG.nvgTranslate(nvg, -x, -y);
    }

    public void scale(float x, float y, float width, float height, float scale) {
        NanoVG.nvgTranslate(nvg, (x + (x + width)) / 2, (y + (y + height)) / 2);
        NanoVG.nvgScale(nvg, scale, scale);
        NanoVG.nvgTranslate(nvg, -(x + (x + width)) / 2, -(y + (y + height)) / 2);
    }

    public void rotate(float x, float y, float width, float height, float angle) {
        NanoVG.nvgTranslate(nvg, (x + (x + width)) / 2, (y + (y + height)) / 2);
        NanoVG.nvgRotate(nvg, angle);
        NanoVG.nvgTranslate(nvg, -(x + (x + width)) / 2, -(y + (y + height)) / 2);
    }

    public void translate(float x, float y) {
        NanoVG.nvgTranslate(nvg, x, y);
    }

    public void setAlpha(float alpha) {
        NanoVG.nvgGlobalAlpha(nvg, alpha);
    }

    public void scissor(float x, float y, float width, float height) {
        NanoVG.nvgScissor(nvg, x, y, width, height);
    }

    public void drawSvg(ResourceLocation location, float x, float y, float width, float height, Color color) {
        drawSvg(location, x, y, width, height, color.getRGB());
    }

    public void drawSvg(ResourceLocation location, float x, float y, float width, float height, int color) {

        if (assetManager.loadSvg(nvg, location, width, height)) {
            NVGPaint imagePaint = getAvailablePaint();

            int image = assetManager.getSvg(location, width, height);

            NanoVG.nvgBeginPath(nvg);
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, 1, imagePaint);

            imagePaint.innerColor(getColor(color));
            imagePaint.outerColor(getColor(color));

            NanoVG.nvgRect(nvg, x, y, width, height);
            NanoVG.nvgFillPaint(nvg, imagePaint);
            NanoVG.nvgFill(nvg);
        }
    }

    public void drawImage(ResourceLocation location, Rect rect) {
        drawImage(location, rect.x, rect.y, rect.width, rect.height);
    }

    public void drawImage(ResourceLocation location, float x, float y, float width, float height) {

        if (assetManager.loadImage(nvg, location)) {
            NVGPaint imagePaint = getAvailablePaint();

            int image = assetManager.getImage(location);

            NanoVG.nvgBeginPath(nvg);
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, 1, imagePaint);

            NanoVG.nvgRect(nvg, x, y, width, height);
            NanoVG.nvgFillPaint(nvg, imagePaint);
            NanoVG.nvgFill(nvg);
        }
    }

    public void drawImage(File file, Rect rect) {
        drawImage(file, rect.x, rect.y, rect.width, rect.height);
    }

    public void drawImage(File file, float x, float y, float width, float height) {

        if (assetManager.loadImage(nvg, file)) {
            NVGPaint imagePaint = getAvailablePaint();

            int image = assetManager.getImage(file);

            NanoVG.nvgBeginPath(nvg);
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, 1, imagePaint);

            NanoVG.nvgRect(nvg, x, y, width, height);
            NanoVG.nvgFillPaint(nvg, imagePaint);
            NanoVG.nvgFill(nvg);
        }
    }

    public void drawImage(int texture, float x, float y, float width, float height, float alpha, int flags) {

        if (assetManager.loadImage(nvg, texture, width, height, flags)) {
            int image = assetManager.getImage(texture);

            NanoVG.nvgImageSize(nvg, image, new int[]{(int) width}, new int[]{-(int) height});
            NVGPaint p = getAvailablePaint();

            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, alpha, p);
            NanoVG.nvgBeginPath(nvg);
            NanoVG.nvgRect(nvg, x, y, width, height);
            NanoVG.nvgFillPaint(nvg, p);
            NanoVG.nvgFill(nvg);
            NanoVG.nvgClosePath(nvg);
        }
    }

    public void drawImage(int texture, float x, float y, float width, float height, float alpha) {
        drawImage(texture, x, y, width, height, alpha, 0);
    }

    public void drawImage(int texture, float x, float y, float width, float height) {
        drawImage(texture, x, y, width, height, 1.0F);
    }

    public void drawRoundedImage(int texture, float x, float y, float width, float height, float radius, float alpha) {

        if (assetManager.loadImage(nvg, texture, width, height)) {

            int image = assetManager.getImage(texture);

            NanoVG.nvgImageSize(nvg, image, new int[]{(int) width}, new int[]{-(int) height});
            NVGPaint p = getAvailablePaint();

            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, alpha, p);
            NanoVG.nvgBeginPath(nvg);
            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
            NanoVG.nvgFillPaint(nvg, p);
            NanoVG.nvgFill(nvg);
            NanoVG.nvgClosePath(nvg);
        }
    }

    public void drawRoundedImage(int texture, float x, float y, float width, float height, float radius) {
        drawRoundedImage(texture, x, y, width, height, radius, 1.0F);
    }


    public void drawPlayerHead(ResourceLocation location, float x, float y, float width, float height, float radius, float alpha) {

        if (location == null || mc.getTextureManager().getTexture(location) == null) {
            return;
        }

        int texture = mc.getTextureManager().getTexture(location).getGlTextureId();

        if (assetManager.loadImage(nvg, texture, width, height)) {

            int image = assetManager.getImage(texture);

            NanoVG.nvgImageSize(nvg, image, new int[]{(int) width}, new int[]{-(int) height});
            NVGPaint p = getAvailablePaint();

            float sizeMultiplier = 8;

            NanoVG.nvgImagePattern(nvg, x - width / 4 * sizeMultiplier / 2, y - height / 4 * sizeMultiplier / 2, width * sizeMultiplier, height * sizeMultiplier, 0, image, alpha, p);
            NanoVG.nvgBeginPath(nvg);
            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
            NanoVG.nvgFillPaint(nvg, p);
            NanoVG.nvgFill(nvg);
            NanoVG.nvgClosePath(nvg);

            NanoVG.nvgImagePattern(nvg, x - width * 3.25F * sizeMultiplier / 2, y - height / 4 * sizeMultiplier / 2, width * sizeMultiplier, height * sizeMultiplier, 0, image, alpha, p);
            NanoVG.nvgBeginPath(nvg);
            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
            NanoVG.nvgFillPaint(nvg, p);
            NanoVG.nvgFill(nvg);
            NanoVG.nvgClosePath(nvg);
        }
    }

    public void drawPlayerHead(ResourceLocation location, float x, float y, float width, float height, float radius) {
        drawPlayerHead(location, x, y, width, height, radius, 1.0F);
    }

    public void drawRoundedImage(ResourceLocation location, float x, float y, float width, float height, float radius, float alpha) {

        if (assetManager.loadImage(nvg, location)) {

            NVGPaint imagePaint = getAvailablePaint();

            int image = assetManager.getImage(location);

            NanoVG.nvgBeginPath(nvg);
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, alpha, imagePaint);

            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
            NanoVG.nvgFillPaint(nvg, imagePaint);
            NanoVG.nvgFill(nvg);
        }
    }

    public void drawRoundedImage(ResourceLocation location, float x, float y, float width, float height, float radius) {
        drawRoundedImage(location, x, y, width, height, radius, 1.0F);
    }

    public void drawRoundedImage(File file, float x, float y, float width, float height, float radius, float alpha) {

        if (assetManager.loadImage(nvg, file)) {

            NVGPaint imagePaint = getAvailablePaint();

            int image = assetManager.getImage(file);

            NanoVG.nvgBeginPath(nvg);
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, alpha, imagePaint);

            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
            NanoVG.nvgFillPaint(nvg, imagePaint);
            NanoVG.nvgFill(nvg);
        }
    }

    public void drawRoundedImage(File file, float x, float y, float width, float height, float radius) {
        drawRoundedImage(file, x, y, width, height, radius, 1.0F);
    }

    public void loadImage(File file) {
        assetManager.loadImage(nvg, file);
    }

    public void loadImage(ResourceLocation location) {
        assetManager.loadImage(nvg, location);
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public void save() {
        NanoVG.nvgSave(nvg);
    }

    public void restore() {
        NanoVG.nvgRestore(nvg);
    }

    public long getContext() {
        return nvg;
    }
}
