package me.eldodebug.soar.gui.modmenu.category.impl;

import java.awt.Color;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.management.cape.CapeCategory;
import me.eldodebug.soar.management.cape.CapeManager;
import me.eldodebug.soar.management.cape.impl.Cape;
import me.eldodebug.soar.management.cape.impl.CustomCape;
import me.eldodebug.soar.management.cape.impl.NormalCape;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.SearchUtils;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import org.lwjgl.input.Keyboard;

public class CosmeticsCategory extends Category {

	private CapeCategory currentCategory;
	Color noColour = new Color(0, 0, 0, 0);

	public CosmeticsCategory(GuiModMenu parent) {
		super(parent, TranslateText.COSMETICS, LegacyIcon.SHOPPING, true, true);
	}

	@Override
	public void initGui() {
		currentCategory = CapeCategory.ALL;
	}

	@Override
	public void initCategory() {
		scroll.resetAll();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		AccentColor accentColor = colorManager.getCurrentColor();
		ColorPalette palette = colorManager.getPalette();
		CapeManager capeManager = instance.getCapeManager();

		int offsetX = 0;
		float offsetY = 13;
		int index = 1;
		int prevIndex = 1;

		nvg.save();
		nvg.translate(0, scroll.getValue());

		for (CapeCategory c : CapeCategory.values()) {

			float textWidth = nvg.getTextWidth(c.getName(), 9, Fonts.MEDIUM);
			boolean isCurrentCategory = c.equals(currentCategory);

			c.getBackgroundAnimation().setAnimation(isCurrentCategory ? 1.0F : 0.0F, 16);

			Color defaultColor = palette.getBackgroundColor(ColorType.DARK);
			Color color1 = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (c.getBackgroundAnimation().getValue() * 255));
			Color color2 = ColorUtils.applyAlpha(accentColor.getColor2(), (int) (c.getBackgroundAnimation().getValue() * 255));
			Color textColor = c.getTextColorAnimation().getColor(isCurrentCategory ? Color.WHITE : palette.getFontColor(ColorType.DARK), 20);

			nvg.drawRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, defaultColor);
			nvg.drawGradientRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, color1, color2);

			nvg.drawText(c.getName(), this.getX() + 15 + offsetX + ((textWidth + 20) - textWidth) / 2, this.getY() + offsetY + 1.5F, textColor, 9, Fonts.MEDIUM);

			offsetX += (int) (textWidth + 28);
		}

		offsetX = 0;
		offsetY = offsetY + 23;

		for (Cape cape : capeManager.getCapes()) {

			if (filterCape(cape)) {
				continue;
			}

			cape.getAnimation().setAnimation(cape.equals(capeManager.getCurrentCape()) ? 1.0F : 0.0F, 16);
			nvg.drawGradientRoundedRect(this.getX() + 15 + offsetX - 2, this.getY() + offsetY - 2, 88 + 4, 135 + 4, 8.5F, ColorUtils.applyAlpha(accentColor.getColor1(), (int) (cape.getAnimation().getValue() * 255)), ColorUtils.applyAlpha(accentColor.getColor2(), (int) (cape.getAnimation().getValue() * 255)));
			nvg.drawRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY, 88, 135, 8, palette.getBackgroundColor(ColorType.DARK));

			if (cape instanceof NormalCape) {

				NormalCape c = ((NormalCape) cape);

				if (c.getSample() != null) {
					nvg.drawRoundedImage(c.getSample(), this.getX() + 24 + offsetX, this.getY() + offsetY + 9, 70, 105, 8);
				}
			} else if (cape instanceof CustomCape) {

				CustomCape c = ((CustomCape) cape);

				if (c.getSample() != null) {
					nvg.drawRoundedImage(c.getSample(), this.getX() + 24 + offsetX, this.getY() + offsetY + 9, 70, 105, 8);
				}
			}
			Color cColour = palette.getFontColor(ColorType.DARK);
			nvg.drawCenteredText(cape.getName(), this.getX() + 15 + offsetX + (88 / 2), this.getY() + offsetY + 120.5F, cColour, 10, Fonts.MEDIUM);

			offsetX += 100;

			if (index % 4 == 0) {
				offsetX = 0;
				offsetY += 147;
				prevIndex++;
			}

			index++;
		}

		scroll.setMaxScroll(prevIndex == 1 ? 0 : offsetY - (147 / 1.48F));

		nvg.restore();
		nvg.drawVerticalGradientRect(getX() + 15,  this.getY(), getWidth() - 30, 12,  palette.getBackgroundColor(ColorType.NORMAL), noColour); //top
		nvg.drawVerticalGradientRect(getX() + 15,  this.getY()+ this.getHeight() - 12, getWidth() - 30, 12, noColour, palette.getBackgroundColor(ColorType.NORMAL)); // bottom
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();

		int offsetX = 0;
		float offsetY = 13 + scroll.getValue();
		CapeManager capeManager = instance.getCapeManager();
		int index = 1;

		for (CapeCategory c : CapeCategory.values()) {

			float textWidth = nvg.getTextWidth(c.getName(), 9, Fonts.MEDIUM);

			if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16) && mouseButton == 0) {
				currentCategory = c;
			}

			offsetX += textWidth + 28;
		}

		offsetX = 0;
		offsetY = offsetY + 23;

		for (Cape cape : capeManager.getCapes()) {

			if (filterCape(cape)) {
				continue;
			}

			if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY, 88, 135) && mouseButton == 0) {
				capeManager.setCurrentCape(cape);
			}

			offsetX += 100;

			if (index % 4 == 0) {
				offsetX = 0;
				offsetY += 147;
			}

			index++;
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		scroll.onKey(keyCode);
		if(keyCode != 0xD0 && keyCode != 0xC8 && keyCode != Keyboard.KEY_ESCAPE)  this.getSearchBox().setFocused(true);
	}

	private boolean filterCape(Cape cape) {
		
		if(!currentCategory.equals(CapeCategory.ALL) && !currentCategory.equals(cape.getCategory())) {
			return true;
		}
		
		if(!this.getSearchBox().getText().isEmpty() && !SearchUtils.isSimillar(cape.getName(), this.getSearchBox().getText())) {
			return true;
		}
		
		return false;
	}
}
