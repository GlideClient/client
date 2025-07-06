package me.eldodebug.soar.gui.modmenu.category.impl.setting.impl;

import java.awt.Color;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.category.impl.SettingCategory;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.SettingScene;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.Theme;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.ui.comp.impl.CompComboBox;
import me.eldodebug.soar.ui.comp.impl.CompToggleButton;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.utils.mouse.Scroll;

public class AppearanceScene extends SettingScene {

    private Scroll themeScroll = new Scroll();
    private Scroll accentScroll = new Scroll();
    private CompComboBox modTheme;
    private CompToggleButton uiBlur;

    public AppearanceScene(SettingCategory parent) {
        super(parent, TranslateText.APPEARANCE, TranslateText.APPEARANCE_DESCRIPTION, LegacyIcon.MONITOR);
    }

    @Override
    public void initGui() {
        modTheme = new CompComboBox(110, InternalSettingsMod.getInstance().getModThemeSetting());
        uiBlur = new CompToggleButton(InternalSettingsMod.getInstance().getBlurSetting());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Glide instance = Glide.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor currentColor = colorManager.getCurrentColor();

        float offsetX = 0;
        int Index1 = 1;
        int Index2 = 1;
        float yMov2 = 26F;

        nvg.drawRoundedRect(this.getX(), this.getY(), this.getWidth(), 76 + 61, 6, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawText(TranslateText.THEME.getText(), this.getX() + 8, this.getY() + 8, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);

        nvg.save();
        nvg.scissor(this.getX(), this.getY() + 24, this.getWidth(), 72);
        nvg.translate(themeScroll.getValue(), 0);

        if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY() + 18, this.getWidth(), 50)) {
            themeScroll.onScroll();
            themeScroll.onAnimation();
        }

        for (Theme t : Theme.values()) {

            int alpha = (int) (t.getAnimation().getValue() * 255);

            nvg.drawRoundedRect(this.getX() + offsetX + 12, this.getY() + 28, 36, 36, 6, t.getNormalBackgroundColor(255));

            t.getAnimation().setAnimation(t.equals(colorManager.getTheme()) ? 1.0F : 0.0F, 16);

            nvg.drawGradientOutlineRoundedRect(this.getX() + offsetX + 12, this.getY() + 28, 36, 36, 6, 1.4F * t.getAnimation().getValue(), ColorUtils.applyAlpha(currentColor.getColor1(), alpha), ColorUtils.applyAlpha(currentColor.getColor2(), alpha));

            offsetX += 46;
            Index1++;
        }

        themeScroll.setMaxScroll((Index1 - 9.1F) * 44F);

        nvg.restore();

        offsetX = 0;

        nvg.drawText(TranslateText.ACCENT_COLOR.getText(), this.getX() + 8, this.getY() + 99 - yMov2, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);

        nvg.save();
        nvg.scissor(this.getX(), this.getY() + 91 - yMov2, this.getWidth(), 72);
        nvg.translate(accentScroll.getValue(), 0f);

        if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY() + 18 + 91 - yMov2, this.getWidth(), 50)) {
            accentScroll.onScroll();
            accentScroll.onAnimation();
        }

        for (AccentColor c : colorManager.getColors()) {

            nvg.drawGradientRoundedRect(this.getX() + offsetX + 12, this.getY() + 28 + 91 - yMov2, 32, 32, 6, c.getColor1(), c.getColor2());

            c.getAnimation().setAnimation(c.equals(currentColor) ? 1.0F : 0.0F, 16);

            nvg.drawCenteredText(LegacyIcon.CHECK, this.getX() + offsetX + 12 + (32 / 2), this.getY() + 28 + 99 - yMov2, new Color(255, 255, 255, (int) (c.getAnimation().getValue() * 255)), 16, Fonts.LEGACYICON);

            offsetX += 40F;
            Index2++;
        }

        accentScroll.setMaxScroll((Index2 - 10.3F) * 40);

        nvg.restore();

        nvg.drawRoundedRect(this.getX(), this.getY() + 91 + 87 - yMov2, this.getWidth(), 41, 6, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawText(TranslateText.HUD_THEME.getText(), this.getX() + 8, this.getY() + 11.5F + (91 * 2) - yMov2, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);

        nvg.drawRoundedRect(this.getX(), this.getY() + 208, this.getWidth(), 41, 6, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawText(TranslateText.UI_BLUR.getText(), this.getX() + 8, this.getY() + 11.5F + 212, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);


        modTheme.setX(this.getX() + this.getWidth() - 120);
        modTheme.setY(this.getY() + 9.5F + (91 * 2) - yMov2);
        modTheme.draw(mouseX, mouseY, partialTicks);

        uiBlur.setX(this.getX() + this.getWidth() - 44);
        uiBlur.setY(this.getY() + 9.5F + 212);
        uiBlur.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        Glide instance = Glide.getInstance();
        ColorManager colorManager = instance.getColorManager();

        float offsetX = themeScroll.getValue();

        for (Theme t : Theme.values()) {

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + offsetX + 12, this.getY() + 28, 36, 36) && mouseButton == 0) {
                colorManager.setTheme(t);
            }

            offsetX += 46;
        }

        offsetX = accentScroll.getValue();

        for (AccentColor c : colorManager.getColors()) {

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + offsetX + 12, this.getY() + 28 + 91 - 26F, 32, 32) && mouseButton == 0) {
                colorManager.setCurrentColor(c);
            }

            offsetX += 40F;
        }

        modTheme.mouseClicked(mouseX, mouseY, mouseButton);
        uiBlur.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
