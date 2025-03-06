package me.eldodebug.soar.gui.modmenu.category.impl.setting.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.category.impl.SettingCategory;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.SettingScene;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.ui.comp.Comp;
import me.eldodebug.soar.ui.comp.impl.CompKeybind;
import me.eldodebug.soar.ui.comp.impl.CompToggleButton;

public class GeneralScene extends SettingScene {

	private CompKeybind modMenuKeybind;
	private CompToggleButton clickEffectSetting;
	private CompToggleButton soundsUISetting;
	private CompToggleButton mcFontSetting;

	public GeneralScene(SettingCategory parent) {
		super(parent, TranslateText.GENERAL, TranslateText.GENERAL_DESCRIPTION, LegacyIcon.LIST);
	}

	@Override
	public void initGui() {
		modMenuKeybind = new CompKeybind(75, InternalSettingsMod.getInstance().getModMenuKeybindSetting());
		clickEffectSetting = new CompToggleButton(InternalSettingsMod.getInstance().getClickEffectsSetting());
		soundsUISetting  = new CompToggleButton(InternalSettingsMod.getInstance().getSoundsUISetting());
		mcFontSetting = new CompToggleButton(InternalSettingsMod.getInstance().getMCHUDFont());
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		ColorPalette palette = colorManager.getPalette();
		int offsetY = 0;
		drawItemInList(TranslateText.OPEN_MOD_MENU.getText(), "Choose which key should open this GUI", this.getY(), offsetY, 87, modMenuKeybind, nvg, palette, mouseX, mouseY, partialTicks); offsetY += 51;
		drawItemInList(TranslateText.CLICK_EFFECT.getText(), TranslateText.CLICK_EFFECT_DESCRIPTION.getText(), this.getY(), offsetY, 46, clickEffectSetting, nvg, palette, mouseX, mouseY, partialTicks); offsetY += 51;
		drawItemInList(TranslateText.UI_SOUNDS.getText(), TranslateText.UI_SOUNDS_DESCRIPTION.getText(), this.getY(), offsetY, 46, soundsUISetting, nvg, palette, mouseX, mouseY, partialTicks); offsetY += 51;
		drawItemInList(TranslateText.MC_FONT.getText(), "If the client should use the minecraft font for the hud", this.getY(), offsetY, 46, mcFontSetting, nvg, palette, mouseX, mouseY, partialTicks); offsetY += 51;
	}

	 private void drawItemInList(String title, String description, float y, float offset, float xRemove, Comp comp, NanoVGManager nvg, ColorPalette palette, int mouseX, int mouseY, float partialTicks) {
		 nvg.drawRoundedRect(this.getX(), y + offset, this.getWidth(), 41, 6, palette.getBackgroundColor(ColorType.DARK));
		 nvg.drawText(title, this.getX() + 8, y + 9.5F + offset, palette.getFontColor(ColorType.DARK), 12.5F, Fonts.MEDIUM);
		 nvg.drawText(description, this.getX() + 8, y + 23.5F + offset,  palette.getFontColor(ColorType.NORMAL), 7.5F, Fonts.REGULAR);
		 comp.setX(this.getX() + this.getWidth() - xRemove);
		 comp.setY(y + 12.5F + offset);
		 comp.draw(mouseX, mouseY, partialTicks);
	 }
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		Glide instance = Glide.getInstance();
		modMenuKeybind.mouseClicked(mouseX, mouseY, mouseButton);
		clickEffectSetting.mouseClicked(mouseX, mouseY, mouseButton);
		soundsUISetting.mouseClicked(mouseX, mouseY, mouseButton);
		mcFontSetting.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public void keyTyped(char typedChar, int keyCode) {
		if (modMenuKeybind.isBinding()) {
			modMenuKeybind.keyTyped(typedChar, keyCode);
		}
	}
}
