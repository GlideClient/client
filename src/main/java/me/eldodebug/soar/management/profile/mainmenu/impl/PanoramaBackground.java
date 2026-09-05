package me.eldodebug.soar.management.profile.mainmenu.impl;

import me.eldodebug.soar.management.language.TranslateText;
import net.minecraft.util.ResourceLocation;

public class PanoramaBackground extends Background {

	private TranslateText nameTranslate;
    private ResourceLocation image;

	public PanoramaBackground(int id, TranslateText nameTranslate, ResourceLocation image) {
		super(id, nameTranslate.getText());
		this.nameTranslate = nameTranslate;
        this.image = image;
	}
	
	@Override
	public String getName() {
		return nameTranslate.getText();
	}

	public String getNameKey() {
		return nameTranslate.getKey();
	}

    public TranslateText getNameTranslate() {
        return nameTranslate;
    }

    public ResourceLocation getImage() { return image; }
}