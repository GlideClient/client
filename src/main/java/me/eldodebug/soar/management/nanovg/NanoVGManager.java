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
import java.util.HashMap;

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

public class NanoVGManager {

	private Minecraft mc = Minecraft.getMinecraft();
	
	private HashMap<Integer, NVGColor> colorCache = new HashMap<Integer, NVGColor>();
	
	private long nvg;
	
	private FontManager fontManager;
	private AssetManager assetManager;
	
	public NanoVGManager() {
		
		nvg = NanoVGGL2.nvgCreate(NanoVGGL2.NVG_ANTIALIAS);
		
		if(nvg == 0) {
			GlideLogger.error("Failed to create NanoVG context");
			mc.shutdown();
		}
		
		fontManager = new FontManager();
		fontManager.init(nvg);
		
		assetManager = new AssetManager();
	}
	
    public void setupAndDraw(Runnable task, boolean scale) {

    	ScaledResolution sr = new ScaledResolution(mc);
    	
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        NanoVG.nvgBeginFrame(nvg, mc.displayWidth, mc.displayHeight, 1);
        
        if(scale) {
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
    	
        NVGPaint bg = NVGPaint.create();
        
        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
        NVGColor nvgColor = getColor(color);
        NVGColor nvgColor2 = getColor(new Color(0, 0, 0, 0));
        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x, y, x + width, y, nvgColor2, nvgColor, bg));
        NanoVG.nvgFill(nvg);
    }
    
    public void drawHSBBox(float x, float y, float width, float height, float radius, Color color) {
    	
        drawRoundedRect(x, y, width, height, radius, color);

        NVGPaint bg = NVGPaint.create();
        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
        NVGColor nvgColor = getColor(Color.WHITE);
        NVGColor nvgColor2 = getColor(new Color(0, 0, 0, 0));
        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x + 8, y + 8, x + width, y, nvgColor, nvgColor2, bg));
        NanoVG.nvgFill(nvg);

        NVGPaint bg2 = NVGPaint.create();
        NanoVG.nvgBeginPath(nvg);
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
        NVGColor nvgColor3 = getColor(new Color(0, 0, 0, 0));
        NVGColor nvgColor4 = getColor(Color.BLACK);
        
        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x + 8, y + 8, x, y + height, nvgColor3, nvgColor4, bg2));
        NanoVG.nvgFill(nvg);
    }
    
	public void drawRect(float x, float y, float width, float height, Color color) {
		
		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgRect(nvg, x, y, width, height);
		
		NVGColor nvgColor = getColor(color);
		
		NanoVG.nvgFillColor(nvg, nvgColor);
		NanoVG.nvgFill(nvg);
	}
	
	public void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
		
		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
		
		NVGColor nvgColor = getColor(color);
		
		NanoVG.nvgFillColor(nvg, nvgColor);
		NanoVG.nvgFill(nvg);
	}
	
	public void drawRoundedRectVarying(float x, float y, float width, float height, float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius, Color color) {
		
		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgRoundedRectVarying(nvg, x, y, width, height, topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);
		
		NVGColor nvgColor = getColor(color);
		
		NanoVG.nvgFillColor(nvg, nvgColor);
		NanoVG.nvgFill(nvg);
	}

	public void drawVerticalGradientRect(float x, float y, float width, float height, Color color1, Color color2) {

		NVGPaint bg = NVGPaint.create();

		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgRect(nvg, x, y, width, height);

		NVGColor nvgColor1 = getColor(color1);
		NVGColor nvgColor2 = getColor(color2);

		NanoVG.nvgFillColor(nvg, nvgColor1);
		NanoVG.nvgFillColor(nvg, nvgColor2);

		NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x, y, x, y+height, nvgColor1, nvgColor2, bg));
		NanoVG.nvgFill(nvg);
	}

	public void drawHorizontalGradientRect(float x, float y, float width, float height, Color color1, Color color2) {

		NVGPaint bg = NVGPaint.create();

		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgRect(nvg, x, y, width, height);

		NVGColor nvgColor1 = getColor(color1);
		NVGColor nvgColor2 = getColor(color2);

		NanoVG.nvgFillColor(nvg, nvgColor1);
		NanoVG.nvgFillColor(nvg, nvgColor2);

		NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x, y, x+ width, y, nvgColor1, nvgColor2, bg));
		NanoVG.nvgFill(nvg);
	}

	public void drawGradientRect(float x, float y, float width, float height, Color color1, Color color2) {

		NVGPaint bg = NVGPaint.create();

		float tick = ((System.currentTimeMillis() % 3600) / 570F);
		float max = Math.max(width, height);

		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgRect(nvg, x, y, width, height);

		NVGColor nvgColor1 = getColor(color1);
		NVGColor nvgColor2 = getColor(color2);

		NanoVG.nvgFillColor(nvg, nvgColor1);
		NanoVG.nvgFillColor(nvg, nvgColor2);

		NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x + width / 2 - (max / 2) * MathUtils.cos(tick), y + height / 2 - (max / 2) * MathUtils.sin(tick), x + width / 2 + (max / 2) * MathUtils.cos(tick), y + height / 2 + (max + 2f) * MathUtils.sin(tick), nvgColor1, nvgColor2, bg));
		NanoVG.nvgFill(nvg);
	}
	
	public void drawGradientRoundedRect(float x, float y, float width, float height, float radius, Color color1, Color color2) {
		
		NVGPaint bg = NVGPaint.create();
		
		float tick = ((System.currentTimeMillis() % 3600) / 570F);
		float max = Math.max(width, height);
		
		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
		
		NVGColor nvgColor1 = getColor(color1);
		NVGColor nvgColor2 = getColor(color2);
		
		NanoVG.nvgFillColor(nvg, nvgColor1);
		NanoVG.nvgFillColor(nvg, nvgColor2);
		
		NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x + width / 2 - (max / 2) * MathUtils.cos(tick), y + height / 2 - (max / 2) * MathUtils.sin(tick), x + width / 2 + (max / 2) * MathUtils.cos(tick), y + height / 2 + (max + 2f) * MathUtils.sin(tick), nvgColor1, nvgColor2, bg));
		NanoVG.nvgFill(nvg);
	}
	
	public void drawOutlineRoundedRect(float x, float y, float width, float height, float radius, float strokeWidth, Color color) {
		
		NVGColor nvgColor = getColor(color);
		
		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
		NanoVG.nvgStrokeWidth(nvg, strokeWidth);
		NanoVG.nvgStrokeColor(nvg, nvgColor);
		NanoVG.nvgStroke(nvg);
	}
	
	public void drawGradientOutlineRoundedRect(float x, float y, float width, float height, float radius, float strokeWidth, Color color1, Color color2) {
		
		NVGPaint bg = NVGPaint.create();
		
		float tick = ((System.currentTimeMillis() % 3600) / 570F);
		float max = Math.max(width, height);
		
		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
		
		NVGColor nvgColor1 = getColor(color1);
		NVGColor nvgColor2 = getColor(color2);
		
		NanoVG.nvgFillColor(nvg, nvgColor1);
		NanoVG.nvgFillColor(nvg, nvgColor2);
		
		NanoVG.nvgStrokeWidth(nvg, strokeWidth);
		NanoVG.nvgStrokePaint(nvg, NanoVG.nvgLinearGradient(nvg, x + width / 2 - (max / 2) * MathUtils.cos(tick), y + height / 2 - (max / 2) * MathUtils.sin(tick), x + width / 2 + (max / 2) * MathUtils.cos(tick), y + height / 2 + (max + 2f) * MathUtils.sin(tick), nvgColor1, nvgColor2, bg));
		NanoVG.nvgStroke(nvg);
	}
	
	public void drawArrow(float x, float y, float size, float angle, Color color) {

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
		
		int alpha = 1;
		
		for(float f = strength; f > 0; f--) {
			drawOutlineRoundedRect(x - (f / 2), y - (f / 2), width + f, height + f, radius + 2, f, new Color(0, 0, 0, alpha));

			alpha+=2;
		}
	}
	
	public void drawShadow(float x, float y, float width, float height, float radius) {
		drawShadow(x, y, width, height, radius, 7);
	}
	
	public void drawGradientShadow(float x, float y, float width, float height, float radius, Color color1, Color color2) {
		
		int alpha = 1;
		
		for(float f = 10; f > 0; f--) {
			drawGradientOutlineRoundedRect(x - (f / 2), y - (f / 2), width + f, height + f, radius + 2, f, ColorUtils.applyAlpha(color1, alpha), ColorUtils.applyAlpha(color2, alpha));

			alpha+=3;
		}
	}
	public void drawRoundedGlow(float x, float y, float width, float height, float radius, Color color1, int strength) {

		int alpha = 1;

		for(float f = strength; f > 0; f--) {
			drawGradientOutlineRoundedRect(x - (f / 2), y - (f / 2), width + f, height + f, radius + 2, f, ColorUtils.applyAlpha(color1, alpha), ColorUtils.applyAlpha(color1, alpha));

			alpha+=2;
		}
	}

	
	public void drawCircle(float x, float y, float radius, Color color) {
		
		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgCircle(nvg, x, y, radius);
		
		NVGColor nvgColor = getColor(color);
		
		NanoVG.nvgFillColor(nvg, nvgColor);
		NanoVG.nvgFill(nvg);
	}
	
	public void drawArc(float x, float y, float radius, float startAngle, float endAngle, float strokeWidth, Color color) {
		
		NVGColor nvgColor = getColor(color);
		
		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgArc(nvg, x, y, radius, (float) Math.toRadians(startAngle), (float) Math.toRadians(endAngle), NanoVG.NVG_CW);
		NanoVG.nvgStrokeWidth(nvg, strokeWidth);
		NanoVG.nvgStrokeColor(nvg, nvgColor);
		NanoVG.nvgStroke(nvg);
	}
	
	public void drawGradientCircle(float x, float y, float radius, Color color1, Color color2) {
		
		NVGPaint bg = NVGPaint.create();
		
		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgCircle(nvg, x, y, radius);
		
		NVGColor nvgColor1 = getColor(color1);
		NVGColor nvgColor2 = getColor(color2);
		
		NanoVG.nvgFillColor(nvg, nvgColor1);
		NanoVG.nvgFillColor(nvg, nvgColor2);
		
		NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x, y, radius, radius, nvgColor1, nvgColor2, bg));
		NanoVG.nvgFill(nvg);
	}

	public void fontBlur(float blur){
		NanoVG.nvgFontBlur(nvg, blur);
	}

	public void drawText(String text, float x, float y, Color color, float size, Font font) {

		y+=size / 2;
		
		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgFontSize(nvg, size);
		NanoVG.nvgFontFace(nvg, font.getName());
		NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
		
		NVGColor nvgColor = getColor(color);
		
		NanoVG.nvgFillColor(nvg, nvgColor);
		NanoVG.nvgText(nvg, x, y, text);
	}

	public void drawTextGlowing(String text, float x, float y, Color color, float blurRadius, float size, Font font) {
		drawTextGlowingBg(text, x, y, color, size, blurRadius, font);
		drawText(text, x,y, color, size, font);
	}

	private void drawTextGlowingBg(String text, float x, float y, Color color, float size, float blurRadius, Font font) {
		y+=size / 2;

		NanoVG.nvgBeginPath(nvg);
		NanoVG.nvgFontSize(nvg, size);
		NanoVG.nvgFontFace(nvg, font.getName());
		NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
		NVGColor nvgColor = getColor(color);
		NanoVG.nvgFillColor(nvg, nvgColor);
		save();
		fontBlur(blurRadius);
		NanoVG.nvgText(nvg, x, y, text);
		restore();
	}




	public void drawTextBox(String text, float x, float y, float maxWidth, Color color, float size, Font font) {

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
		
		int textWidth = (int) getTextWidth(text, size, font);
		
		drawText(text, x - (textWidth >> 1), y, color, size, font);
	}
	
	public float getTextWidth(String text, float size, Font font) {
		
	    float[] bounds = new float[4];
	    
	    NanoVG.nvgFontSize(nvg, size);
	    NanoVG.nvgFontFace(nvg, font.getName());
	    NanoVG.nvgTextBounds(nvg, 0, 0, text, bounds);
	    NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
	    
	    return bounds[2] - bounds[0];
	}

	
	public float getTextHeight(String text, float size, Font font) {
		
	    float[] bounds = new float[4];
	    
	    NanoVG.nvgFontSize(nvg, size);
	    NanoVG.nvgFontFace(nvg, font.getName());
	    NanoVG.nvgTextBounds(nvg, 0, 0, text, bounds);

	    return bounds[3] - bounds[1];
	}

	public float getTextBoxHeight(String text, float size, Font font, float maxWidth) {
		float[] bounds = new float[4];

		NanoVG.nvgFontSize(nvg, size);
		NanoVG.nvgFontFace(nvg, font.getName());
		NanoVG.nvgTextBoxBounds(nvg, 0, 0, maxWidth, text, bounds);

		return bounds[3] - bounds[1];
	}

	public String getLimitText(String inputText, float fontSize, Font font, float width) {

		String text = inputText;
		boolean isInRange = false;
		boolean isRemoved = false;

		while(!isInRange) {

			if(getTextWidth(text, fontSize, font) > width) {
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
		
        if (assetManager.loadSvg(nvg, location, width, height)) {
        	
            NVGPaint imagePaint = NVGPaint.calloc();
            
            int image = assetManager.getSvg(location, width, height);
            
            NanoVG.nvgBeginPath(nvg);
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, 1, imagePaint);
            
            imagePaint.innerColor(getColor(color));
            imagePaint.outerColor(getColor(color));
            
            NanoVG.nvgRect(nvg, x, y, width, height);
            NanoVG.nvgFillPaint(nvg, imagePaint);
            NanoVG.nvgFill(nvg);            

            imagePaint.free();
        }
	}
	
	public void drawImage(ResourceLocation location, float x, float y, float width, float height) {
		
		if(assetManager.loadImage(nvg, location)) {
			
			NVGPaint imagePaint = NVGPaint.calloc();
			
			int image = assetManager.getImage(location);
			
			NanoVG.nvgBeginPath(nvg);
			NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, 1, imagePaint);
			
			NanoVG.nvgRect(nvg, x, y, width, height);
			NanoVG.nvgFillPaint(nvg, imagePaint);
			NanoVG.nvgFill(nvg);
			
			imagePaint.free();
		}
	}
	
	public void drawImage(File file, float x, float y, float width, float height) {
		
		if(assetManager.loadImage(nvg, file)) {
			
			NVGPaint imagePaint = NVGPaint.calloc();
			
			int image = assetManager.getImage(file);
			
			NanoVG.nvgBeginPath(nvg);
			NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, 1, imagePaint);
			
			NanoVG.nvgRect(nvg, x, y, width, height);
			NanoVG.nvgFillPaint(nvg, imagePaint);
			NanoVG.nvgFill(nvg);
			
			imagePaint.free();
		}
	}

	public void drawImage(int texture, float x, float y, float width, float height, float alpha) {
		
		if(assetManager.loadImage(nvg, texture, width, height)) {
			
			int image = assetManager.getImage(texture);
			
			NanoVG.nvgImageSize(nvg, image, new int[]{(int) width}, new int[]{-(int) height});
	        NVGPaint p = NVGPaint.calloc();
	        
	        NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, alpha, p);
	        NanoVG.nvgBeginPath(nvg);
	        NanoVG.nvgRect(nvg, x, y, width, height);
	        NanoVG.nvgFillPaint(nvg, p);
	        NanoVG.nvgFill(nvg);
	        NanoVG.nvgClosePath(nvg);
	        
	        p.free();
		}
	}
	
	public void drawImage(int texture, float x, float y, float width, float height) {
		drawImage(texture, x, y, width, height, 1.0F);
	}
	
	public void drawRoundedImage(int texture, float x, float y, float width, float height, float radius, float alpha) {
		
		if(assetManager.loadImage(nvg, texture, width, height)) {
			
			int image = assetManager.getImage(texture);
			
			NanoVG.nvgImageSize(nvg, image, new int[]{(int) width}, new int[]{-(int) height});
	        NVGPaint p = NVGPaint.calloc();
	        
	        NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, alpha, p);
	        NanoVG.nvgBeginPath(nvg);
	        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
	        NanoVG.nvgFillPaint(nvg, p);
	        NanoVG.nvgFill(nvg);
	        NanoVG.nvgClosePath(nvg);
	        
	        p.free();
		}
	}
	
	public void drawRoundedImage(int texture, float x, float y, float width, float height, float radius) {
		drawRoundedImage(texture, x, y, width, height, radius, 1.0F);
	}
	
	public void drawPlayerHead(ResourceLocation location, float x, float y, float width, float height, float radius, float alpha) {
		
		if(location == null || mc.getTextureManager().getTexture(location) == null) {
			return;
		}
		
		int texture = mc.getTextureManager().getTexture(location).getGlTextureId();
		
		if(assetManager.loadImage(nvg, texture, width, height)) {
			
			int image = assetManager.getImage(texture);
			
			NanoVG.nvgImageSize(nvg, image, new int[]{(int) width}, new int[]{-(int) height});
	        NVGPaint p = NVGPaint.calloc();
	        
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
	        
	        p.free();
		}
	}
	
	public void drawPlayerHead(ResourceLocation location, float x, float y, float width, float height, float radius) {
		drawPlayerHead(location, x, y, width, height, radius, 1.0F);
	}
	
	public void drawRoundedImage(ResourceLocation location, float x, float y, float width, float height, float radius, float alpha) {
		
		if(assetManager.loadImage(nvg, location)) {
			
			NVGPaint imagePaint = NVGPaint.calloc();
			
			int image = assetManager.getImage(location);
			
			NanoVG.nvgBeginPath(nvg);
			NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, alpha, imagePaint);
			
			NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
			NanoVG.nvgFillPaint(nvg, imagePaint);
			NanoVG.nvgFill(nvg);
			
			imagePaint.free();
		}
	}
	
	public void drawRoundedImage(ResourceLocation location, float x, float y, float width, float height, float radius) {
		drawRoundedImage(location, x, y, width, height, radius, 1.0F);
	}
	
	public void drawRoundedImage(File file, float x, float y, float width, float height, float radius, float alpha) {
		
		if(assetManager.loadImage(nvg, file)) {
			
			NVGPaint imagePaint = NVGPaint.calloc();
			
			int image = assetManager.getImage(file);
			
			NanoVG.nvgBeginPath(nvg);
			NanoVG.nvgImagePattern(nvg, x, y, width, height, 0, image, alpha, imagePaint);
			
			NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
			NanoVG.nvgFillPaint(nvg, imagePaint);
			NanoVG.nvgFill(nvg);
			
			imagePaint.free();
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
	
	public NVGColor getColor(Color color) {
		
		if(color == null) {
			color = Color.RED;
		}
		
		if(colorCache.containsKey(color.getRGB())) {
			return colorCache.get(color.getRGB());
		}
		
		NVGColor nvgColor = NVGColor.create();
		
		NanoVG.nvgRGBA((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) color.getAlpha(), nvgColor);
		
		colorCache.put(color.getRGB(), nvgColor);
		
		return nvgColor;
	}
	
	public long getContext() {
		return nvg;
	}
}
