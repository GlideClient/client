package me.eldodebug.soar.gui.modmenu.category.impl;

import java.awt.*;
import java.util.ArrayList;

import me.eldodebug.soar.gui.modmenu.category.impl.setting.impl.GeneralScene;
import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.SettingScene;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.impl.AppearanceScene;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.impl.LanguageScene;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.other.SmoothStepAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;

public class SettingCategory extends Category {
	
	private Animation sceneAnimation;
	
	private ArrayList<SettingScene> scenes = new ArrayList<SettingScene>();
	private SettingScene currentScene;
	
	public SettingCategory(GuiModMenu parent) {
		super(parent, TranslateText.SETTINGS, LegacyIcon.SETTINGS, false, false);
		
		scenes.add(new AppearanceScene(this));
		scenes.add(new LanguageScene(this));
		scenes.add(new GeneralScene(this));
	}
	
	@Override
	public void initGui() {
		sceneAnimation = new SmoothStepAnimation(260, 1.0);
		sceneAnimation.setValue(1.0);
		
		for(SettingScene scene : scenes) {
			scene.initGui();
		}
	}

	@Override
	public void initCategory() {
		scroll.resetAll();
		sceneAnimation = new SmoothStepAnimation(260, 1.0);
		sceneAnimation.setValue(1.0);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorPalette palette = instance.getColorManager().getPalette();
		
		float offsetY = 15;
		
		if(sceneAnimation.isDone(Direction.FORWARDS)) {
			this.setCanClose(true);
			currentScene = null;
		}
		
		nvg.save();
		nvg.translate((float) -(600 - (sceneAnimation.getValue() * 600)), 0);
		
		for(SettingScene scene : scenes) {
			
			nvg.drawRoundedRect(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 40, 8, palette.getBackgroundColor(ColorType.DARK));
			//nvg.drawRoundedRect(this.getX() + 15, this.getY() + offsetY + 19.5F, this.getWidth() - 30, 1F, 0, new Color(255, 200, 10));
			nvg.drawText(scene.getIcon(), this.getX() + 26, this.getY() + offsetY + 13F, palette.getFontColor(ColorType.DARK), 14, Fonts.LEGACYICON);
			nvg.drawText(scene.getName(), this.getX() + 47, this.getY() + offsetY + 9F, palette.getFontColor(ColorType.DARK), 12.5F, Fonts.MEDIUM);
			nvg.drawText(scene.getDescription(), this.getX() + 47, this.getY() + offsetY + 23, palette.getFontColor(ColorType.NORMAL), 7.5F, Fonts.REGULAR);
			nvg.drawText(LegacyIcon.CHEVRON_RIGHT, this.getX() + this.getWidth() - 32, this.getY() + offsetY + 15, palette.getFontColor(ColorType.NORMAL), 10, Fonts.LEGACYICON);
			
			offsetY+=50;
		}
		
		nvg.restore();
		
		nvg.save();
		nvg.translate((float) (sceneAnimation.getValue() * 600), 0);
		
		if(currentScene != null) {
			currentScene.drawScreen(mouseX, mouseY, partialTicks);
		}
		
		nvg.restore();
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		float offsetY = 15;
		
		for(SettingScene scene : scenes) {
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 40) && mouseButton == 0 && currentScene == null) {
				currentScene = scene;
				this.setCanClose(false);
				sceneAnimation.setDirection(Direction.BACKWARDS);
			}
			
			offsetY+=50;
		}
		
		if(currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
			currentScene.mouseClicked(mouseX, mouseY, mouseButton);
		}

		if (!MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY() ,  this.getWidth(), this.getHeight()) && mouseButton == 0) {
			sceneAnimation.setDirection(Direction.FORWARDS);
		}

		if(currentScene != null && mouseButton == 3) {
			sceneAnimation.setDirection(Direction.FORWARDS);
		}
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		
		if(currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
			currentScene.mouseReleased(mouseX, mouseY, mouseButton);
		}
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		
		if(currentScene != null && keyCode == Keyboard.KEY_ESCAPE) {
			sceneAnimation.setDirection(Direction.FORWARDS);
		}
		if(currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
			currentScene.keyTyped(typedChar, keyCode);
		}
	}
	
	public int getSceneX() {
		return getX() + 15;
	}
	
	public int getSceneY() {
		return getY() + 15;
	}
	
	public int getSceneWidth() {
		return getWidth() - 30;
	}
	
	public int getSceneHeight() {
		return getHeight() - 30;
	}
}
