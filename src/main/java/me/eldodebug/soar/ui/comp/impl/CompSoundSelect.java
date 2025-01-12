package me.eldodebug.soar.ui.comp.impl;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.settings.impl.SoundSetting;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.ui.comp.Comp;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.file.FileUtils;
import me.eldodebug.soar.utils.mouse.MouseUtils;

public class CompSoundSelect extends Comp {

	private SoundSetting soundSetting;
	
	public CompSoundSelect(float x, float y, SoundSetting soundSetting) {
		super(x, y);
		this.soundSetting = soundSetting;
	}
	
	public CompSoundSelect(SoundSetting soundSetting) {
		super(0, 0);
		this.soundSetting = soundSetting;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		AccentColor accentColor = colorManager.getCurrentColor();
		ColorPalette palette = colorManager.getPalette();
		
		String name = soundSetting.getSound() == null ? TranslateText.NONE.getText() : soundSetting.getSound().getName();
		float nameWidth = nvg.getTextWidth(name, 9, Fonts.REGULAR);
		
		nvg.drawGradientRoundedRect(this.getX(), this.getY(), 16, 16, 4, accentColor.getColor1(), accentColor.getColor2());
		nvg.drawText(name, this.getX() - nameWidth - 5, this.getY() + 4, palette.getFontColor(ColorType.DARK), 9, Fonts.REGULAR);
		nvg.drawCenteredText(LegacyIcon.FOLDER, this.getX() + 8, this.getY() + 2.5F, Color.WHITE, 10, Fonts.LEGACYICON);
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		if(MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), 16, 16) && mouseButton == 0) {
			
			Multithreading.runAsync(() -> {
				
				File sound = FileUtils.selectSoundFile();
				
				if(sound != null) {
					
					FileManager fileManager = Glide.getInstance().getFileManager();
					File cacheDir = new File(fileManager.getCacheDir(), "custom-sound");
					
					fileManager.createDir(cacheDir);
					
					File newImage = new File(cacheDir, sound.getName());
					
					try {
						FileUtils.copyFile(sound, newImage);
					} catch (IOException e) {}
					
					soundSetting.setSound(newImage);
				}
			});
		}
	}
}