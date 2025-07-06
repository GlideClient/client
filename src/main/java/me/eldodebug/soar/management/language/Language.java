package me.eldodebug.soar.management.language;

import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.util.ResourceLocation;

public enum Language {
	JAPANESE("ja-jp", "日本語 (日本)", new ResourceLocation("soar/flag/ja.png")),
	CHINESE("zh-cn", "中文 (中國)", new ResourceLocation("soar/flag/cn.png")),
	ENGLISHGB("en-gb", "English (United Kingdom)", new ResourceLocation("soar/flag/gb.png")),
	ENGLISH("en-us", "English (United States)", new ResourceLocation("soar/flag/us.png")),
	FRENCH("fr-fr", "Français (France)", new ResourceLocation("soar/flag/fr.png")),
	SPANISH("es-es", "Español (España)", new ResourceLocation("soar/flag/es.png")),
	VIETNAMESE("vi-vn", "Tiếng Việt (Việt Nam)", new ResourceLocation("soar/flag/vn.png")), 
	RUSSIAN("ru-ru", "русский (россия)", new ResourceLocation("soar/flag/ru.png")), 
	PORTUGESE("pt-pt", "Português (Portugal)", new ResourceLocation("soar/flag/pt.png")), 
	PERSIAN("fa-ir", "Fārsi (Irān)", new ResourceLocation("soar/flag/ir.png")), 
	LOLCAT("lc-koc", "LOLCAT (Kinduim ov catos)", new ResourceLocation("soar/flag/koc.png")),
	WOOFLANG("wo-yap", "Wooflang (Yappington)", new ResourceLocation("soar/flag/wo.png"));
	
	private SimpleAnimation animation = new SimpleAnimation();
	
	private String id;
	private String nameTranslate;
	private ResourceLocation flag;
	
	private Language(String id, String nameTranslate, ResourceLocation flag) {
		this.id = id;
		this.nameTranslate = nameTranslate;
		this.flag = flag;
	}

	public String getId() {
		return id;
	}
	
	public String getName() {
		return nameTranslate;
	}
	
	public ResourceLocation getFlag() {
		return flag;
	}

	public SimpleAnimation getAnimation() {
		return animation;
	}

	public String getNameTranslate() {
		return nameTranslate;
	}

	public static Language getLanguageById(String id) {
		
		for(Language lang : Language.values()) {
			if(lang.getId().equals(id)) {
				return lang;
			}
		}
		
		return Language.ENGLISHGB;
	}
}
