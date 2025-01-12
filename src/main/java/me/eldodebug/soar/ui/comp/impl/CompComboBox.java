package me.eldodebug.soar.ui.comp.impl;

import java.awt.Color;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.ui.comp.Comp;
import me.eldodebug.soar.utils.MathUtils;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;

public class CompComboBox extends Comp {

	private SimpleAnimation changeAnimation = new SimpleAnimation();
	private ComboSetting setting;
	private float width;
	private int changeDirection;
	
	public CompComboBox(float x, float y, float width, ComboSetting setting) {
		super(x, y);
		this.width = width;
		this.setting = setting;
		this.changeDirection = 1;
		this.changeAnimation.setValue(1);
	}
	
	public CompComboBox(float width, ComboSetting setting) {
		super(0, 0);
		this.width = width;
		this.setting = setting;
		this.changeDirection = 1;
		this.changeAnimation.setValue(1);
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		AccentColor accentColor = colorManager.getCurrentColor();
		
		changeAnimation.setAnimation(changeDirection, 16);
		
		nvg.drawGradientRoundedRect(this.getX(), this.getY(), width, 16, 4, accentColor.getColor1(), accentColor.getColor2());
		
		nvg.drawCenteredText(setting.getOption().getName(), this.getX() + (width / 2) + ((changeDirection - changeAnimation.getValue()) * 22), this.getY() + 5F, new Color(255, 255, 255, (int) (MathUtils.abs(changeAnimation.getValue()  * 255))), 8, Fonts.REGULAR);
		
		nvg.drawText("<", this.getX() + 4, this.getY() + 4F, Color.WHITE, 10, Fonts.REGULAR);
		nvg.drawText(">", this.getX() + width - 10, this.getY() + 4F, Color.WHITE, 10, Fonts.REGULAR);
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		int max = setting.getOptions().size();
		int modeIndex = setting.getOptions().indexOf(setting.getOption());
		
		if(mouseButton == 0) {
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), 16, 16)) {
				
				changeAnimation.setValue(0);
				
				if(modeIndex > 0) {
					modeIndex--;
				}else {
					modeIndex = max - 1;
				}
				
				changeDirection = 1;
				setting.setOption(setting.getOptions().get(modeIndex));
			}
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + width - 16, this.getY(), 16, 16)) {
				
				changeAnimation.setValue(0);
				
				if(modeIndex < max - 1) {
					modeIndex++;
				}else {
					modeIndex = 0;
				}
				
				changeDirection = -1;
				setting.setOption(setting.getOptions().get(modeIndex));
			}
		}
	}

	public ComboSetting getSetting() {
		return setting;
	}
}
