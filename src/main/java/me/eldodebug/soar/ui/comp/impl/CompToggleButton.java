package me.eldodebug.soar.ui.comp.impl;

import java.awt.Color;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.ui.comp.Comp;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.animation.ColorAnimation;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;

public class CompToggleButton extends Comp {

	private SimpleAnimation opacityAnimation = new SimpleAnimation();
	private SimpleAnimation toggleAnimation = new SimpleAnimation();
	private ColorAnimation circleAnimation = new ColorAnimation();
	
	private BooleanSetting setting;
	
	private float scale;
	
	public CompToggleButton(float x, float y, float scale, BooleanSetting setting) {
		super(x, y);
		
		ColorPalette palette = Glide.getInstance().getColorManager().getPalette();
		
		this.setting = setting;
		this.scale = scale;
		toggleAnimation.setValue(setting.isToggled() ? 20.5F : 2.5F);
		circleAnimation.setColor(setting.isToggled() ? Color.WHITE : palette.getBackgroundColor(ColorType.DARK));
	}
	
	public CompToggleButton(BooleanSetting setting) {
		super(0, 0);
		
		ColorPalette palette = Glide.getInstance().getColorManager().getPalette();
		
		this.setting = setting;
		this.scale = 1.0F;
		toggleAnimation.setValue(setting.isToggled() ? 20.5F : 2.5F);
		circleAnimation.setColor(setting.isToggled() ? Color.WHITE : palette.getBackgroundColor(ColorType.DARK));
	}
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		AccentColor accentColor = colorManager.getCurrentColor();
		ColorPalette palette = colorManager.getPalette();
		
		float x = this.getX();
		float y = this.getY();
		float width = 34 * scale;
		float height = 16 * scale;
		float circle = 11 * scale;
		boolean toggled = setting.isToggled();
		
		opacityAnimation.setAnimation(toggled ? 1.0F : 0.0F, 14);
		toggleAnimation.setAnimation(toggled ? 20.5F : 2.5F, 14);
		
		nvg.drawRoundedRect(x, y, width, height, (7 * scale), palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawGradientRoundedRect(x, y, width, height, (7.4F * scale), ColorUtils.applyAlpha(accentColor.getColor1(), (int) (opacityAnimation.getValue() * 255)), ColorUtils.applyAlpha(accentColor.getColor2(), (int) (opacityAnimation.getValue() * 255)));
		nvg.drawRoundedRect(x + (toggleAnimation.getValue() * scale), y + (2.5F * scale), circle, circle, circle / 2, circleAnimation.getColor(toggled ? Color.WHITE : palette.getBackgroundColor(ColorType.DARK), 16));
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		float x = this.getX();
		float y = this.getY();
		float width = 34 * scale;
		float height = 16 * scale;
		
		if(MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && mouseButton == 0) {
			setting.setToggled(!setting.isToggled());
		}
	}
	
	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public BooleanSetting getSetting() {
		return setting;
	}
}
