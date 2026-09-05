package me.eldodebug.soar.management.profile.mainmenu.impl;

import me.eldodebug.soar.management.language.TranslateText;
import net.minecraft.util.ResourceLocation;

public class DefaultBackground extends Background {

	private TranslateText nameTranslate;
	private ResourceLocation image;
	
	public DefaultBackground(int id, TranslateText nameTranslate, ResourceLocation image) {
		super(id, nameTranslate.getText());
		this.nameTranslate = nameTranslate;
		this.image = image;
	}
	
	@Override
	public String getName() {
		return nameTranslate.getText();
	}

    public TranslateText getNameTranslate() {
        return nameTranslate;
    }

	public String getNameKey() {
		return nameTranslate.getKey();
	}

	public ResourceLocation getImage() {
		return image;
	}
}