package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import me.eldodebug.soar.management.mods.settings.impl.*;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventKey;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;

public class InternalSettingsMod extends Mod {

	private static InternalSettingsMod instance;
	
	private ComboSetting modThemeSetting = new ComboSetting(TranslateText.HUD_THEME, this, TranslateText.NORMAL, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.NORMAL), new Option(TranslateText.GLOW), new Option(TranslateText.OUTLINE), new Option(TranslateText.VANILLA),
			new Option(TranslateText.OUTLINE_GLOW), new Option(TranslateText.VANILLA_GLOW), new Option(TranslateText.SHADOW),
			new Option(TranslateText.DARK), new Option(TranslateText.LIGHT), new Option(TranslateText.RECT), new Option(TranslateText.MODERN),
			new Option(TranslateText.TEXT), new Option(TranslateText.GRADIENT_SIMPLE))));

	
	private NumberSetting volumeSetting = new NumberSetting(TranslateText.VOLUME, this, 0.8, 0, 1, false);

	private KeybindSetting modMenuKeybindSetting = new KeybindSetting(TranslateText.KEYBIND, this, Keyboard.KEY_RSHIFT);

	private TextSetting capeNameSetting = new TextSetting(TranslateText.CUSTOM_CAPE, this, "None");

	private BooleanSetting clickEffectsSetting = new BooleanSetting(TranslateText.CLICK_EFFECT, this, true);

	public InternalSettingsMod() {
		super(TranslateText.NONE, TranslateText.NONE, ModCategory.OTHER);
		
		instance = this;
	}

	@Override
	public void setup() {
		this.setHide(true);
		this.setToggled(true);
	}
	
	@EventTarget
	public void onKey(EventKey event) {
		if(event.getKeyCode() == modMenuKeybindSetting.getKeyCode()) {
			mc.displayGuiScreen(Glide.getInstance().getModMenu());
		}
//		if (event.getKeyCode() == Keyboard.KEY_DOWN) {
//			int max = modThemeSetting.getOptions().size();
//			int modeIndex = modThemeSetting.getOptions().indexOf(modThemeSetting.getOption());
//
//			if (modeIndex > 0) {
//				modeIndex--;
//			} else {
//				modeIndex = max - 1;
//			}
//
//			modThemeSetting.setOption(modThemeSetting.getOptions().get(modeIndex));
//
//		}
	}

	public static InternalSettingsMod getInstance() {
		return instance;
	}

	public BooleanSetting getClickEffectsSetting(){return clickEffectsSetting;}

	public NumberSetting getVolumeSetting() {
		return volumeSetting;
	}

	public ComboSetting getModThemeSetting() {return modThemeSetting;}

	public KeybindSetting getModMenuKeybindSetting() {return modMenuKeybindSetting;}

	public String getCapeConfigName(){
		return capeNameSetting.getText();
	}

	public void setCapeConfigName(String a){
		capeNameSetting.setText(a);
	}
}
