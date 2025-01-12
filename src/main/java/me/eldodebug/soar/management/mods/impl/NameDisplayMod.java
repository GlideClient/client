package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.SimpleHUDMod;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

public class NameDisplayMod extends SimpleHUDMod {

	private BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);
	
	private ComboSetting prefixSetting = new ComboSetting(TranslateText.PREFIX, this, TranslateText.NAME, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.NAME), new Option(TranslateText.IGN))));
	
	public NameDisplayMod() {
		super(TranslateText.NAME_DISPLAY, TranslateText.NAME_DISPLAY_DESCRIPTION);
	}

	@Override
	public String getText() {
		
		Option option = prefixSetting.getOption();
		String prefix = "null";
		
		if(option.getTranslate().equals(TranslateText.NAME)) {
			prefix = "Name";
		}
		
		if(option.getTranslate().equals(TranslateText.IGN)) {
			prefix = "Ign";
		}
		
		return prefix + ": " + mc.thePlayer.getGameProfile().getName();
	}
	
	@Override
	public String getIcon() {
		return iconSetting.isToggled() ? LegacyIcon.USER : null;
	}
}
