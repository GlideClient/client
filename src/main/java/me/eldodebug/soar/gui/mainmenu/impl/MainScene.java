package me.eldodebug.soar.gui.mainmenu.impl;

import java.awt.Color;

import eu.shoroa.contrib.render.Blur;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.mainmenu.GuiGlideMainMenu;
import me.eldodebug.soar.gui.mainmenu.MainMenuScene;
import me.eldodebug.soar.gui.mainmenu.widget.WidgetPlayButton;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icons;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.nanovg.NanoVG;

public class MainScene extends MainMenuScene {

    private WidgetPlayButton buttonSingleplayer, buttonMultiplayer, buttonSettings;

    public MainScene(GuiGlideMainMenu parent) {
        super(parent);

        buttonSettings = new WidgetPlayButton(TranslateText.SETTINGS, 0, 0, 180, 20, () -> mc.displayGuiScreen(new GuiOptions(this.getParent(), mc.gameSettings)));
        buttonMultiplayer = new WidgetPlayButton(TranslateText.MULTIPLAYER, 0, 0, 180, 20, () -> mc.displayGuiScreen(new GuiMultiplayer(this.getParent())));
        buttonSingleplayer = new WidgetPlayButton(TranslateText.SINGLEPLAYER, 0, 0, 180, 20, () -> mc.displayGuiScreen(new GuiSelectWorld(this.getParent())));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Glide instance = Glide.getInstance();
        if (instance.getUpdateNeeded()) {
            instance.setUpdateNeeded(false);
            this.setCurrentScene(this.getSceneByClass(UpdateScene.class));
        }
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.setupAndDraw(() -> drawNanoVG(nvg, mouseX, mouseY));
    }

    private void drawNanoVG(NanoVGManager nvg, int mouseX, int mouseY) {

        ScaledResolution sr = new ScaledResolution(mc);

        float yPos = sr.getScaledHeight() / 2f - 22;
        final float iconY = sr.getScaledHeight() / 2f - (nvg.getTextHeight(Icons.GLIDE, 54, Fonts.GLICONIC) / 2) - 40;

        nvg.drawBlurredText(Icons.GLIDE, sr.getScaledWidth() / 2f, iconY, new Color(0, 0, 0, 180), 8f, 54, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE, Fonts.GLICONIC);
        nvg.drawCenteredText(Icons.GLIDE, sr.getScaledWidth() / 2f, iconY, Color.WHITE, 54, Fonts.GLICONIC);

        buttonSingleplayer.setPosition(sr.getScaledWidth() / 2f - (180 / 2f), yPos);
        buttonMultiplayer.setPosition(sr.getScaledWidth() / 2f - (180 / 2f), yPos + 26);
        buttonSettings.setPosition(sr.getScaledWidth() / 2f - (180 / 2f), yPos + (26 * 2));

        buttonSingleplayer.render(nvg, mouseX, mouseY);
        buttonMultiplayer.render(nvg, mouseX, mouseY);
        buttonSettings.render(nvg, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (buttonSingleplayer.mouseClicked(mouseX, mouseY, mouseButton) || buttonMultiplayer.mouseClicked(mouseX, mouseY, mouseButton) || buttonSettings.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (buttonSingleplayer.mouseReleased(mouseX, mouseY, mouseButton) || buttonMultiplayer.mouseReleased(mouseX, mouseY, mouseButton) || buttonSettings.mouseReleased(mouseX, mouseY, mouseButton)) {
            return;
        }
    }
}
