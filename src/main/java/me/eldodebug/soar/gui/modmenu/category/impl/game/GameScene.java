package me.eldodebug.soar.gui.modmenu.category.impl.game;

import me.eldodebug.soar.gui.modmenu.category.impl.GamesCategory;
import me.eldodebug.soar.management.language.TranslateText;

public class GameScene {

	private GamesCategory parent;
	private String icon;
	private String name, description;

	public GameScene(GamesCategory parent, String name, String description, String icon) {
		this.parent = parent;
		this.name = name;
		this.description = description;
		this.icon = icon;
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
		return name;
	}
	
	public String getDescription() {return description;}
	
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
