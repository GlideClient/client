package me.eldodebug.soar.gui.gamemenus.views;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.gamemenus.GlideScreen;
import me.eldodebug.soar.gui.gamemenus.MenuManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icons;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class MainMenuClassic extends GlideScreen {

    public MainMenuClassic(MenuManager manager) {
        super(manager, "Main Menu");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Glide instance = Glide.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        nvg.setupAndDraw(() -> drawNanoVG(nvg, instance));
    }

    private void drawNanoVG(NanoVGManager nvg, Glide glideInstance) {

        ScaledResolution sr = new ScaledResolution(mc);

        float yPos = sr.getScaledHeight() / 2 - 22;

        nvg.drawCenteredText(Icons.GLIDE, sr.getScaledWidth() / 2, sr.getScaledHeight() / 2 - (nvg.getTextHeight(Icons.GLIDE, 54, Fonts.ICON_FILLED) / 2) - 60, Color.WHITE, 54, Fonts.ICON_FILLED);

        nvg.drawRoundedRect(sr.getScaledWidth() / 2 - (180 / 2), yPos, 180, 20, 4.5F, this.getBackgroundColor());
        nvg.drawCenteredText(TranslateText.SINGLEPLAYER.getText(), sr.getScaledWidth() / 2, yPos + 11F, Color.WHITE, 9.5F, Fonts.REGULAR);

        nvg.drawRoundedRect(sr.getScaledWidth() / 2 - (180 / 2), yPos + 26, 180, 20, 4.5F, this.getBackgroundColor());
        nvg.drawCenteredText(TranslateText.MULTIPLAYER.getText(), sr.getScaledWidth() / 2, yPos + 11F + 26, Color.WHITE, 9.5F, Fonts.REGULAR);

        nvg.drawRoundedRect(sr.getScaledWidth() / 2 - (180 / 2), yPos + (26 * 2), 180, 20, 4.5F, this.getBackgroundColor());
        nvg.drawCenteredText(TranslateText.SETTINGS.getText(), sr.getScaledWidth() / 2, yPos + 11F + (26 * 2), Color.WHITE, 9.5F, Fonts.REGULAR);

        String copyright = "Copyright Mojang AB. Do not distribute!";
        nvg.drawText(copyright, sr.getScaledWidth() - (nvg.getTextWidth(copyright, 9, Fonts.REGULAR)) - 4, sr.getScaledHeight() - 12, new Color(255, 255, 255), 9, Fonts.REGULAR);
		nvg.drawText("Glide Client v" + glideInstance.getVersion(), 4, sr.getScaledHeight() - 12, new Color(255, 255, 255), 9, Fonts.REGULAR);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        ScaledResolution sr = new ScaledResolution(mc);

        float yPos = sr.getScaledHeight() / 2 - 22;

        if(mouseButton == 0) {

            if(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2 - (160 / 2), yPos, 160, 20)) {
                mc.displayGuiScreen(new GuiSelectWorld(this.getMenuManager()));
            }

            if(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2 - (180 / 2), yPos + 26, 180, 20)) {
                mc.displayGuiScreen(new GuiMultiplayer(this.getMenuManager()));
            }

            if(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2 - (180 / 2), yPos + (26 * 2), 180, 20)) {
                mc.displayGuiScreen(new GuiOptions(this.getMenuManager(), mc.gameSettings));
            }
        }
    }
}
