package me.eldodebug.soar.gui.modmenu.category.impl.setting.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.GlideMeta;
import me.eldodebug.soar.gui.modmenu.category.impl.SettingCategory;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.SettingScene;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Font;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icons;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.ui.comp.Comp;
import me.eldodebug.soar.ui.comp.impl.CompKeybind;
import me.eldodebug.soar.ui.comp.impl.CompToggleButton;

public class AboutScene extends SettingScene {


	public AboutScene(SettingCategory parent) {
		super(parent, TranslateText.ABOUT, TranslateText.ABOUT_DESCRIPTION, Icons.INFO_20);
	}

	@Override
	public void initGui() {
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		ColorPalette palette = colorManager.getPalette();
        float yOff = 0;
        nvg.drawCenteredTextWithShadow(Icons.GLIDE, getX() + getWidth()/2, getY() + 30, palette.getFontColor(ColorType.DARK).getRGB(), 5, 30, Fonts.ICON_OUTLINE);
        yOff += nvg.getTextHeight(Icons.GLIDE, 30, Fonts.ICON_OUTLINE);
        nvg.drawCenteredTextWithShadow(GlideMeta.CLIENT_NAME + "Client", getX() + getWidth()/2, getY() + 30 + yOff, palette.getFontColor(ColorType.DARK).getRGB(), 5, 14, Fonts.MEDIUM);
        yOff += nvg.getTextHeight(GlideMeta.CLIENT_NAME  + "Client", 14, Fonts.MEDIUM);
        nvg.drawCenteredTextWithShadow(String.format("%s (%s)", GlideMeta.VERSION_NUMBER, GlideMeta.VERSION_IDENTIFIER), getX() + getWidth()/2, getY() + 30 + yOff, palette.getFontColor(ColorType.NORMAL).getRGB(), 5, 8, Fonts.REGULAR);

    }


	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		Glide instance = Glide.getInstance();
	}
}
