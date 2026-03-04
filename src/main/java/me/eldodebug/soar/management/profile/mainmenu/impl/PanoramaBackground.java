package me.eldodebug.soar.management.profile.mainmenu.impl;

import me.eldodebug.soar.management.language.TranslateText;
import net.minecraft.util.ResourceLocation;

public class PanoramaBackground extends Background {

	private TranslateText nameTranslate;

	public PanoramaBackground(int id, TranslateText nameTranslate) {
		super(id, nameTranslate.getText());
		this.nameTranslate = nameTranslate;
	}
	
	@Override
	public String getName() {
		return nameTranslate.getText();
	}

	public String getNameKey() {
		return nameTranslate.getKey();
	}

}