package me.eldodebug.soar.gui.modmenu.category.impl.setting.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.category.impl.SettingCategory;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.SettingScene;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.Language;
import me.eldodebug.soar.management.language.LanguageManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.utils.mouse.Scroll;

public class LanguageScene extends SettingScene {

	public LanguageScene(SettingCategory parent) {
		super(parent, TranslateText.LANGUAGE, TranslateText.LANGUAGE_DESCRIPTION, LegacyIcon.GLOBE);
	}

	private Scroll languageScroll = new Scroll();

	@Override
	public void initGui() {
		languageScroll.resetAll();
	}


	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorPalette palette = instance.getColorManager().getPalette();
		AccentColor currentColor = instance.getColorManager().getCurrentColor();
		LanguageManager languageManager = instance.getLanguageManager();
		
		float offsetY = 0;

		nvg.scissor(this.getX(), this.getY() - 15, this.getWidth(), this.getHeight() + 45);
		nvg.translate(0, languageScroll.getValue());
		
		for(Language lang : Language.values()) {
			
			nvg.drawRoundedRect(this.getX(), this.getY() + offsetY, this.getWidth(), 40, 8, palette.getBackgroundColor(ColorType.DARK));
			nvg.drawRoundedImage(lang.getFlag(), this.getX() + 6, this.getY() + offsetY + 6, 3 * 14, 2 * 14, 4);
			nvg.drawText(lang.getName(), this.getX() + 56, this.getY() + offsetY + 15F, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
			
			lang.getAnimation().setAnimation(lang.equals(languageManager.getCurrentLanguage()) ? 1.0F : 0.0F, 16);
			
			nvg.drawText(LegacyIcon.CHECK, this.getX() + this.getWidth() - 28, this.getY() + 12 + offsetY, ColorUtils.applyAlpha(currentColor.getInterpolateColor(), (int) (lang.getAnimation().getValue() * 255)), 16, Fonts.LEGACYICON);
			
			offsetY+=50;
		}

		languageScroll.onScroll();
		languageScroll.onAnimation();
		languageScroll.setMaxScroll((Language.values().length - 5.25F) * 50);
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		Glide instance = Glide.getInstance();
		LanguageManager languageManager = instance.getLanguageManager();

		float offsetY = 0 + languageScroll.getValue();
		if (!MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY() - 15,  this.getWidth(), this.getHeight() + 45)) {return;}
		for(Language lang : Language.values()) {
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY() + offsetY, this.getWidth(), 40) && mouseButton == 0) {
				languageManager.setCurrentLanguage(lang);
			}
			
			offsetY+=50;
		}
	}

	public void keyTyped(char typedChar, int keyCode) {
		languageScroll.onKey(keyCode);
	}
}
