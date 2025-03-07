package me.eldodebug.soar.management.mods;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import eu.shoroa.contrib.render.ShBlur;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.GuiEditHUD;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Font;
import eu.shoroa.contrib.shader.UIShader;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.utils.ColorUtils;
import net.minecraft.util.ResourceLocation;

public class HUDMod extends Mod {

	private int x, y, draggingX, draggingY, width, height;
	private float scale;
	private boolean dragging, draggable;

	public HUDMod(TranslateText nameTranslate, TranslateText descriptionText) {
		super(nameTranslate, descriptionText, ModCategory.HUD);

		this.x = 100;
		this.y = 100;
		this.width = 100;
		this.height = 100;
		this.scale = 1.0F;
		this.draggable = true;
	}

	public HUDMod(TranslateText nameTranslate, TranslateText descriptionText, String alias) {
		super(nameTranslate, descriptionText, ModCategory.HUD, alias);

		this.x = 100;
		this.y = 100;
		this.width = 100;
		this.height = 100;
		this.scale = 1.0F;
		this.draggable = true;
	}

	public HUDMod(TranslateText nameTranslate, TranslateText descriptionText, String alias, boolean bannable) {
		super(nameTranslate, descriptionText, ModCategory.HUD, alias, bannable);

		this.x = 100;
		this.y = 100;
		this.width = 100;
		this.height = 100;
		this.scale = 1.0F;
		this.draggable = true;
	}

	public void save() {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();

		nvg.save();
	}

	public void restore() {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();

		nvg.restore();
	}

	public void scissor(float addX, float addY, float width, float height) {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();

		nvg.scissor(x + (addX * scale), y + (addY * scale), width * scale, height * scale);
	}

	public void drawPlayerHead(ResourceLocation location, float addX, float addY, float width, float height, float radius) {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();

		nvg.drawPlayerHead(location, x + (addX * scale), y + (addY * scale), width * scale, height * scale, radius * scale);
	}

	public void drawRoundedImage(int texture, float addX, float addY, float width, float height, float radius) {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();

		nvg.drawRoundedImage(texture, x + (addX * scale), y + (addY * scale), width * scale, height * scale, radius * scale);
	}

	public void drawRoundedImage(File file, float addX, float addY, float width, float height, float radius, float alpha) {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();

		nvg.drawRoundedImage(file, x + (addX * scale), y + (addY * scale), width * scale, height * scale, radius * scale, alpha);
	}

	public void drawRoundedImage(File file, float addX, float addY, float width, float height, float radius) {
		drawRoundedImage(file, addX, addY, width, height, radius, 1.0F);
	}

	public void drawRoundedImage(ResourceLocation location, float addX, float addY, float width, float height, float radius) {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();

		nvg.drawRoundedImage(location, x + (addX * scale), y + (addY * scale), width * scale, height * scale, radius * scale);
	}

	public void drawArc(float addX, float addY, float radius, float startAngle, float endAngle, float strokeWidth, Color color) {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();

		nvg.drawArc(x + (addX * scale), y + (addY * scale), radius * scale, startAngle, endAngle, strokeWidth * scale, color);
	}

	public void drawArc(float addX, float addY, float radius, float startAngle, float endAngle, float strokeWidth) {
		drawArc(addX, addY, radius, startAngle, endAngle, strokeWidth, this.getFontColor());
	}

	public void drawShadow(float addX, float addY, float width, float height, float radius) {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();

		nvg.drawShadow(x + (addX * scale), y + (addY * scale), width * scale, height * scale, radius * scale);
	}

	public void drawRect(float addX, float addY, float width, float height, Color color) {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();

		nvg.drawRect(x + (addX * scale), y + (addY * scale), width * scale, height * scale, color);
	}

	public void drawRect(float addX, float addY, float width, float height) {
		drawRect(addX, addY, width, height, this.getFontColor());
	}

	public void drawRoundedRect(float addX, float addY, float width, float height, float radius, Color color) {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();

		if(width < 0) {
			return;
		}

		if(height < 0) {
			return;
		}

		nvg.drawRoundedRect(x + (addX * scale), y + (addY * scale), width * scale, height * scale, radius * scale, color);
	}

	public void drawRoundedRect(float addX, float addY, float width, float height, float radius) {
		drawRoundedRect(addX, addY, width, height, radius, this.getFontColor());
	}

	public void drawBackground(float addX, float addY, float width, float height, float radius) {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		AccentColor currentColor = colorManager.getCurrentColor();
		ComboSetting setting = InternalSettingsMod.getInstance().getModThemeSetting();
		Option theme = setting.getOption();

		boolean isNormal = theme.getTranslate().equals(TranslateText.NORMAL);
		boolean isVanilla = theme.getTranslate().equals(TranslateText.VANILLA);
		boolean isGlow = theme.getTranslate().equals(TranslateText.GLOW);
		boolean isVanillaGlow = theme.getTranslate().equals(TranslateText.VANILLA_GLOW);
		boolean isOutline = theme.getTranslate().equals(TranslateText.OUTLINE);
		boolean isOutlineGlow = theme.getTranslate().equals(TranslateText.OUTLINE_GLOW);
		boolean isShadow = theme.getTranslate().equals(TranslateText.SHADOW);
		boolean isDark = theme.getTranslate().equals(TranslateText.DARK);
		boolean isLight = theme.getTranslate().equals(TranslateText.LIGHT);
		boolean isRect = theme.getTranslate().equals(TranslateText.RECT);
		boolean isModern = theme.getTranslate().equals(TranslateText.MODERN);
		boolean isSimpGrad = theme.getTranslate().equals(TranslateText.GRADIENT_SIMPLE);
		boolean isBlur = InternalSettingsMod.getInstance().getBlurSetting().isToggled();

		float lastWidth = width * scale;
		float lastHeight = height * scale;
		float x = this.x + (addX * scale);
		float y = this.y + (addY * scale);

		if (isBlur) ShBlur.getInstance().drawBlur(x,y,lastWidth,lastHeight,radius);

		if(isNormal || isVanilla || isShadow || isDark || isLight || isModern) {
			nvg.drawShadow(x, y, lastWidth, lastHeight, radius - 0.75F);
		}else if(isGlow || isVanillaGlow) {
			nvg.drawGradientShadow(x, y, lastWidth, lastHeight, radius, currentColor.getColor1(), currentColor.getColor2());
		}else if(isOutline || isOutlineGlow) {
			if(isOutline) {
				nvg.drawShadow(x - 2, y - 2, lastWidth + 4, lastHeight + 4, radius + 2);
			}else if(isOutlineGlow) {
				nvg.drawGradientShadow(x - 2, y - 2, lastWidth + 4, lastHeight + 4, radius + 2, currentColor.getColor1(), currentColor.getColor2());
			}
		}

		if(isOutline || isOutlineGlow) {
			nvg.drawGradientOutlineRoundedRect(x - 1, y - 1, lastWidth + 2, lastHeight + 2, radius + 1, 1.5F, currentColor.getColor1(), currentColor.getColor2());
		}

		if(isVanilla || isVanillaGlow || isOutline || isOutlineGlow) {
			nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, new Color(0, 0, 0, 100));
		}else if(isNormal || isGlow) {
			nvg.drawGradientRoundedRect(x, y, lastWidth, lastHeight, radius, ColorUtils.applyAlpha(currentColor.getColor1(), 220), ColorUtils.applyAlpha(currentColor.getColor2(), 220));
		} else if(isLight) {
			nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, new Color(240, 240, 240, 220));
		} else if(isDark) {
			nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, new Color(20, 20, 20, 220));
		}
		if(isRect || isSimpGrad) {
			nvg.drawRect(x, y, lastWidth, lastHeight, new Color(20, 20, 20, 165));
		}
		if(isSimpGrad){
			nvg.drawHorizontalGradientRect(x, y - (2 * scale), lastWidth, (2* scale),  ColorUtils.interpolateColors(8, 0, currentColor.getColor1(), currentColor.getColor2()), ColorUtils.interpolateColors(10, 20, currentColor.getColor1(), currentColor.getColor2()));
		}
		if(isModern) {
			nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, new Color(0, 0, 0, 110));
			nvg.drawOutlineRoundedRect(x - 0.5F, y - 0.5F, lastWidth + 1, lastHeight + 1, radius + 0.5F, 0.7F,  new Color(255,255,255,110));
		}

	}

	public void drawBackground(float width, float height) {
		drawBackground(0, 0, width, height, 6 * scale);
	}

	public void drawBackground(float addX, float addY, float width, float height) {
		drawBackground(addX, addY, width, height, 6 * scale);
	}

	public void drawBackground(float width, float height, float radius) {
		drawBackground(0, 0, width, height, radius);
	}

	public void drawText(String text, float addX, float addY, float size, Font font, Color color) {

		if(font == Fonts.MOJANGLES) {
			addX = addX  - 0.5F;
			addY = addY  - 1.3F;
		}

		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		float lastSize = size * scale;
		Option theme = InternalSettingsMod.getInstance().getModThemeSetting().getOption();
		boolean isText = theme.getTranslate().equals(TranslateText.TEXT);

		if(isText){
			nvg.save();
			nvg.fontBlur(20);
			nvg.drawText(text, x + (addX * scale), y + (addY * scale), new Color(0,0,0, 150), lastSize, font);
			nvg.restore();
		}

		nvg.drawText(text, x + (addX * scale), y + (addY * scale), new Color(color.getRed(), color.getGreen(), color.getBlue(), 180), lastSize, font);
	}

	public void scale(float addX, float addY, float width, float height, float nvgScale) {

		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();

		nvg.scale(x + (addX * scale), y + (addY * scale), width * scale, height * scale, nvgScale);
	}

	public void drawText(String text, float addX, float addY, float size, Font font) {
		drawText(text, addX, addY, size, font, getFontColor());
	}

	public void drawCenteredText(String text, float addX, float addY, float size, Font font, Color color) {

		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		float lastSize = size * scale;

		nvg.drawCenteredText(text, x + (addX * scale), y + (addY * scale), color, lastSize, font);
	}

	public void drawCenteredText(String text, float addX, float addY, float size, Font font) {
		drawCenteredText(text, addX, addY, size, font, getFontColor());
	}

	public float getTextWidth(String text, float size, Font font) {

		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();

		return nvg.getTextWidth(text, size, font);
	}

	public Color getFontColor(int alpha) {

		ComboSetting setting = InternalSettingsMod.getInstance().getModThemeSetting();
		Option theme = setting.getOption();

		boolean isDark = theme.getTranslate().equals(TranslateText.DARK);
		boolean isLight = theme.getTranslate().equals(TranslateText.LIGHT);

		if(isDark || isLight) {
			return Glide.getInstance().getColorManager().getCurrentColor().getInterpolateColor(alpha);
		}

		return new Color(255, 255, 255, alpha);
	}

	public Color getFontColor() {
		return getFontColor(255);
	}

	public boolean isEditing() {
		return mc.currentScreen instanceof GuiEditHUD;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getDraggingX() {
		return draggingX;
	}

	public void setDraggingX(int draggingX) {
		this.draggingX = draggingX;
	}

	public int getDraggingY() {
		return draggingY;
	}

	public void setDraggingY(int draggingY) {
		this.draggingY = draggingY;
	}

	public int getWidth() {
		return (int) (width * scale);
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return (int) (height * scale);
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isDragging() {
		return dragging;
	}

	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {

		if(scale > 5.0 || scale < 0.2) {

			if(scale > 5.0) {
				this.scale = 5.0F;
			}

			if(scale < 0.2) {
				this.scale = 0.2F;
			}

			return;
		}

		this.scale = scale;
	}

	public boolean isDraggable() {
		return draggable;
	}

	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
	}

	public Font getHudFont(int in){
		if(InternalSettingsMod.getInstance().getMCHUDFont().isToggled()) return Fonts.MOJANGLES;
		switch (in) {
			case 1:
				return Fonts.REGULAR;
			case 2:
				return Fonts.MEDIUM;
			case 3:
				return Fonts.SEMIBOLD;
			default:
				return Fonts.REGULAR;
		}
	}

}
