package me.eldodebug.soar.gui.modmenu.category.impl.setting;

import me.eldodebug.soar.gui.modmenu.category.impl.SettingCategory;
import me.eldodebug.soar.management.language.TranslateText;

public class SettingScene {

	private SettingCategory parent;
	private String icon;
	private TranslateText nameTranslate, descriptionTranslate;
	private boolean show = true;
	
	public SettingScene(SettingCategory parent, TranslateText nameTranslate, TranslateText descriptionTranslate, String icon, boolean show) {
		this.parent = parent;
		this.nameTranslate = nameTranslate;
		this.descriptionTranslate = descriptionTranslate;
		this.icon = icon;
		this.show = show;
	}

	public void initGui() {}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {}
	
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}
	
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}
	
	public void keyTyped(char typedChar, int keyCode) {}
	
	public String getIcon() {
		return icon;
	}

	public String getName() {
		return nameTranslate.getText();
	}
	
	public String getDescription() {
		return descriptionTranslate.getText();
	}

	public boolean shouldShow() {
		return show;
	}

	public int getX() {
		return parent.getSceneX();
	}
	
	public int getY() {
		return parent.getSceneY();
	}
	
	public int getWidth() {
		return parent.getSceneWidth();
	}
	
	public int getHeight() {
		return parent.getSceneHeight();
	}
}
