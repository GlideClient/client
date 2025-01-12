package me.eldodebug.soar.gui.modmenu.category.impl;

import java.awt.Color;
import java.io.File;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.ModManager;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.management.profile.Profile;
import me.eldodebug.soar.management.profile.ProfileIcon;
import me.eldodebug.soar.management.profile.ProfileManager;
import me.eldodebug.soar.management.profile.ProfileType;
import me.eldodebug.soar.ui.comp.impl.field.CompTextBox;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.SearchUtils;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.other.SmoothStepAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;

public class ProfileCategory extends Category {
// todo: ADD SCROLL tf why isnt that here xd
	private ProfileType currentType;
	private Animation profileAnimation;
	private boolean openProfile;
	private ProfileIcon currentIcon;
	private CompTextBox nameBox = new CompTextBox();
	private CompTextBox serverIpBox = new CompTextBox();
	
	public ProfileCategory(GuiModMenu parent) {
		super(parent, TranslateText.PROFILE, LegacyIcon.EDIT, true, true);
	}

	@Override
	public void initGui() {
		currentType = ProfileType.ALL;
		currentIcon = ProfileIcon.COMMAND;
		openProfile = false;
		profileAnimation = new SmoothStepAnimation(260, 1.0);
		profileAnimation.setValue(1.0);
	}
	
	@Override
	public void initCategory() {
		scroll.resetAll();
		openProfile = false;
		profileAnimation = new SmoothStepAnimation(260, 1.0);
		profileAnimation.setValue(1.0);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ProfileManager profileManager = instance.getProfileManager();
		ColorManager colorManager = instance.getColorManager();
		AccentColor accentColor = colorManager.getCurrentColor();
		ColorPalette palette = colorManager.getPalette();
		
		int offsetX = 0;
		float offsetY = 13;
		int index = 1;
		
		profileAnimation.setDirection(openProfile ? Direction.BACKWARDS : Direction.FORWARDS);
		
		if(profileAnimation.isDone(Direction.FORWARDS)) {
			nameBox.setText("");
			serverIpBox.setText("");
			this.setCanClose(true);
		}
		
		// Draw profile scene
		nvg.save();
		nvg.translate((float) -(600 - (profileAnimation.getValue() * 600)), 0);
		
		for(ProfileType t : ProfileType.values()) {
			
			float textWidth = nvg.getTextWidth(t.getName(), 9, Fonts.MEDIUM);
			boolean isCurrentCategory = t.equals(currentType);
			
			t.getBackgroundAnimation().setAnimation(isCurrentCategory ? 1.0F : 0.0F, 16);
			
			Color defaultColor = palette.getBackgroundColor(ColorType.DARK);
			Color color1 = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (t.getBackgroundAnimation().getValue() * 255));
			Color color2 = ColorUtils.applyAlpha(accentColor.getColor2(), (int) (t.getBackgroundAnimation().getValue() * 255));
			Color textColor = t.getTextColorAnimation().getColor(isCurrentCategory ? Color.WHITE : palette.getFontColor(ColorType.DARK), 20);

			nvg.drawRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, defaultColor);
			nvg.drawGradientRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, color1, color2);
			
			nvg.drawText(t.getName(), this.getX() + 15 + offsetX + ((textWidth + 20) - textWidth) / 2, this.getY() + offsetY + 1.5F, textColor, 9, Fonts.MEDIUM);
			
			offsetX+=textWidth + 28;
		}
		
		offsetX = 0;
		offsetY = offsetY + 23;
		
		for(Profile p : profileManager.getProfiles()) {
			
			if(filter(p)) {
				continue;
			}
			
			nvg.drawRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY, 123, 46, 6, palette.getBackgroundColor(ColorType.DARK));
			
			if(p.getIcon() != null) {
				nvg.drawRoundedImage(p.getIcon().getIcon(), this.getX() + 15 + offsetX + 6, this.getY() + offsetY + 6, 34, 34, 6);
			}
			
			if(p.getName() != "") {
				nvg.drawText(nvg.getLimitText(p.getName(), 10, Fonts.MEDIUM, 68), this.getX() + 62 + offsetX, this.getY() + offsetY + 9, palette.getFontColor(ColorType.DARK), 10, Fonts.MEDIUM);
			}
			
			if(p.getId() == 999) {
				nvg.drawCenteredText(LegacyIcon.PLUS, this.getX() + offsetX + 14.5F + (123 / 2), this.getY() + offsetY + 13, palette.getFontColor(ColorType.DARK), 20, Fonts.LEGACYICON);
			}else {
				nvg.drawText(LegacyIcon.STAR, this.getX() + 62 + offsetX, this.getY() + 29 + offsetY, palette.getMaterialYellow(), 11, Fonts.LEGACYICON);
				
				p.getStarAnimation().setAnimation(p.getType().equals(ProfileType.FAVORITE) ? 1.0F : 0.0F, 16);
				
				nvg.drawText(LegacyIcon.STAR_FILL, this.getX() + 62 + offsetX, this.getY() + 29 + offsetY, palette.getMaterialYellow((int) (p.getStarAnimation().getValue() * 255)), 11, Fonts.LEGACYICON);
				
				nvg.drawText(LegacyIcon.TRASH, this.getX() + 62 + 14 + offsetX, this.getY() + 29 + offsetY, palette.getMaterialRed(), 11, Fonts.LEGACYICON);
			}
			
			offsetX+=133;
			
			if(index % 3 == 0) {
				offsetX = 0;
				offsetY+=56;
			}
			
			index++;
		}
		
		nvg.restore();
		
		// Draw profile add scene
		
		nvg.save();
		nvg.translate((float) (profileAnimation.getValue() * 600), 0);
		
		offsetY = 15;
		offsetX = 0;
		
		nvg.drawRoundedRect(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, this.getHeight() - 30, 10, palette.getBackgroundColor(ColorType.DARK));
		nvg.drawRect(this.getX() + 15, this.getY() + offsetY + 27, this.getWidth() - 30, 1, palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawText(TranslateText.ADD_PROFILE.getText(), this.getX() + 26, this.getY() + offsetY + 9, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
		nvg.drawText(TranslateText.ICON.getText(), this.getX() + 30, this.getY() + offsetY + 35, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
		
		for(ProfileIcon icon : ProfileIcon.values()) {
			
			int alpha = (int) (icon.getAnimation().getValue() * 255);
			
			icon.getAnimation().setAnimation(currentIcon.equals(icon) ? 1.0F : 0.0F, 16);
			
			nvg.drawRoundedImage(icon.getIcon(), this.getX() + 32 + offsetX, this.getY() + offsetY + 53, 32, 32, 6);
			
			nvg.drawGradientOutlineRoundedRect(this.getX() + 32 + offsetX, this.getY() + offsetY + 53, 32, 32, 6, 1.6F * icon.getAnimation().getValue(), ColorUtils.applyAlpha(accentColor.getColor1(), alpha), ColorUtils.applyAlpha(accentColor.getColor2(), alpha));
			
			offsetX+=40;
		}
		
		nvg.drawText(TranslateText.NAME.getText(), this.getX() + 30, this.getY() + offsetY + 96, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
		
		nameBox.setPosition(this.getX() + 32, this.getY() + 128, 130, 18);
		nameBox.setDefaultText(TranslateText.NAME.getText());
		nameBox.draw(mouseX, mouseY, partialTicks);
		
		nvg.drawText(TranslateText.AUTO_LOAD.getText(), this.getX() + (this.getWidth() / 2), this.getY() + offsetY + 95, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
		
		serverIpBox.setPosition(this.getX() + (this.getWidth() / 2) + 2, this.getY() + 128, 130, 18);
		serverIpBox.setDefaultText(TranslateText.SERVER_IP.getText());
		serverIpBox.draw(mouseX, mouseY, partialTicks);
		
		nvg.drawRoundedRect(this.getX() + this.getWidth() - 124, this.getY() + this.getHeight() - 44, 
				100, 21, 6, palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawCenteredText(TranslateText.CREATE.getText(), this.getX() + this.getWidth() - 124 + 50, this.getY() + this.getHeight() - 37.5F,
				palette.getFontColor(ColorType.DARK), 10, Fonts.REGULAR);
		
		nvg.restore();
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		Glide instance = Glide.getInstance();
		ProfileManager profileManager = instance.getProfileManager();
		NanoVGManager nvg = instance.getNanoVGManager();
		ModManager modManager = instance.getModManager();
		FileManager fileManager = instance.getFileManager();
		
		int offsetX = 0;
		float offsetY = 13;
		int index = 1;
		
		if(openProfile) {
			
			offsetY = 15;
			offsetX = 0;

			for(ProfileIcon icon : ProfileIcon.values()) {
				
				if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 32 + offsetX, this.getY() + offsetY + 53, 32, 32) && mouseButton == 0) {
					currentIcon = icon;
				}
				
				offsetX+=40;
			}
			
			nameBox.mouseClicked(mouseX, mouseY, mouseButton);
			serverIpBox.mouseClicked(mouseX, mouseY, mouseButton);
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 124, this.getY() + this.getHeight() - 44,  100, 21) && mouseButton == 0) {
				
				if(!nameBox.getText().isEmpty()) {
					
					String serverIp = "";
					
					if(!serverIpBox.getText().isEmpty()) {
						serverIp = serverIpBox.getText();
					}
					
					profileManager.save(new File(fileManager.getProfileDir(), nameBox.getText() + ".json"), serverIp, ProfileType.ALL, currentIcon);
					profileManager.loadProfiles(false);
					
					openProfile = false;
				}
			}
		} else {
			
			for(ProfileType t : ProfileType.values()) {
				
				float textWidth = nvg.getTextWidth(t.getName(), 9, Fonts.MEDIUM);
				
				if(MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight())) {
					
					if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16) && mouseButton == 0) {
						currentType = t;
					}
				}
				
				offsetX+=textWidth + 28;
			}
			
			offsetX = 0;
			offsetY = offsetY + 23;
			
			for(Profile p : profileManager.getProfiles()) {
				
				if(filter(p)) {
					continue;
				}
				
				if(mouseButton == 0) {
					
					boolean favorite = MouseUtils.isInside(mouseX, mouseY, this.getX() + 61 + offsetX, this.getY() + 28 + offsetY, 13, 13);
					boolean delete = MouseUtils.isInside(mouseX, mouseY, this.getX() + 62 + 13 + offsetX, this.getY() + 28 + offsetY, 13, 13);
					boolean inside = MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY, 123, 46);
					
					if(inside) {
						
						if(p.getId() == 999 && inside) {
							openProfile = true;
							this.setCanClose(false);
						} else if(!favorite && !delete) {
							modManager.disableAll();
							profileManager.load(p.getJsonFile());
						}
					}
					
					if(p.getId() != 999) {
						if(favorite) {
							
							if(p.getType().equals(ProfileType.FAVORITE)) {
								p.setType(ProfileType.ALL);
							}else {
								p.setType(ProfileType.FAVORITE);
							}
							
							profileManager.save(p.getJsonFile(), p.getServerIp(), p.getType(), p.getIcon());
						}
						
						if(delete) {
							profileManager.delete(p);
						}
					}
				}
				
				offsetX+=133;
				
				if(index % 3 == 0) {
					offsetX = 0;
					offsetY+=56;
				}
				
				index++;
			}
		}
		
		if(mouseButton == 3) {
			openProfile = false;
		}
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		
		if(openProfile) {
			
			nameBox.keyTyped(typedChar, keyCode);
			serverIpBox.keyTyped(typedChar, keyCode);
			
			if(keyCode == Keyboard.KEY_ESCAPE) {
				openProfile = false;
			}
		} else {
			if(keyCode != 0xD0 && keyCode != 0xC8 && keyCode != Keyboard.KEY_ESCAPE) this.getSearchBox().setFocused(true);
		}
	}
	
	private boolean filter(Profile p) {
		
		if(currentType.equals(ProfileType.FAVORITE) && !p.getType().equals(ProfileType.FAVORITE)) {
			return true;
		}
		
		if(!this.getSearchBox().getText().isEmpty() && !SearchUtils.isSimillar(p.getName(), this.getSearchBox().getText())) {
			return true;
		}
		
		return false;
	}
}
