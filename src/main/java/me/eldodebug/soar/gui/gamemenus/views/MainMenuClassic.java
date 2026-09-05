package me.eldodebug.soar.gui.gamemenus.views;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.GlideMeta;
import me.eldodebug.soar.gui.gamemenus.GlideScreen;
import me.eldodebug.soar.gui.gamemenus.MenuManager;
import me.eldodebug.soar.gui.gamemenus.elements.ElementButton;
import me.eldodebug.soar.gui.gamemenus.ViewMenuButton;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icons;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class MainMenuClassic extends GlideScreen {

    private ElementButton buttonSingleplayer, buttonMultiplayer, buttonSettings;

    public MainMenuClassic(MenuManager manager) {
        super(manager, "");

        addMenuAction(new ViewMenuButton(Icons.POWER_24, Color.WHITE, ViewMenuButton.Direction.IN,  () -> {
            System.exit(0);
        }));

        addMenuAction(new ViewMenuButton(Icons.IMAGE_EDIT_20, Color.WHITE, ViewMenuButton.Direction.IN, () -> {
            setCurrentView(getViewByClass(BackgroundSelector.class));
        }));



        buttonSettings = new ElementButton(TranslateText.SETTINGS, 0, 0, 180, 20, () -> mc.displayGuiScreen(new GuiOptions(this.getMenuManager(), mc.gameSettings)));
        buttonMultiplayer = new ElementButton(TranslateText.MULTIPLAYER, 0, 0, 180, 20, () -> mc.displayGuiScreen(new GuiMultiplayer(this.getMenuManager())));
        buttonSingleplayer = new ElementButton(TranslateText.SINGLEPLAYER, 0, 0, 180, 20, () -> mc.displayGuiScreen(new GuiSelectWorld(this.getMenuManager())));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Glide instance = Glide.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        nvg.setupAndDraw(() -> drawNanoVG(nvg, instance, mouseX, mouseY));
    }

    private void drawNanoVG(NanoVGManager nvg, Glide glideInstance, int mouseX, int mouseY) {

        ScaledResolution sr = new ScaledResolution(mc);

        float yPos = sr.getScaledHeight() / 2F - 22;
        final float iconY = sr.getScaledHeight() / 2f - (nvg.getTextHeight(Icons.GLIDE, 54, Fonts.GLICONIC) / 2) - 40;

        nvg.drawCenteredText(Icons.GLIDE, sr.getScaledWidth() / 2f, iconY, Color.WHITE, 54, Fonts.GLICONIC);

        buttonSingleplayer.setPosition(sr.getScaledWidth() / 2f - (180 / 2f), yPos);
        buttonMultiplayer.setPosition(sr.getScaledWidth() / 2f - (180 / 2f), yPos + 26);
        buttonSettings.setPosition(sr.getScaledWidth() / 2f - (180 / 2f), yPos + (26 * 2));

        buttonSingleplayer.render(nvg, mouseX, mouseY);
        buttonMultiplayer.render(nvg, mouseX, mouseY);
        buttonSettings.render(nvg, mouseX, mouseY);

        String copyright = "Copyright Mojang AB. Do not distribute!";
        nvg.drawText(copyright, sr.getScaledWidth() - (nvg.getTextWidth(copyright, 9, Fonts.REGULAR)) - 4, sr.getScaledHeight() - 12, new Color(255, 255, 255), 9, Fonts.REGULAR);
		nvg.drawText("GlideClient v" + GlideMeta.VERSION_NUMBER, 4, sr.getScaledHeight() - 12, new Color(255, 255, 255), 9, Fonts.REGULAR);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (buttonSingleplayer.mouseClicked(mouseX, mouseY, mouseButton) || buttonMultiplayer.mouseClicked(mouseX, mouseY, mouseButton) || buttonSettings.mouseClicked(mouseX, mouseY, mouseButton)) {}
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (buttonSingleplayer.mouseReleased(mouseX, mouseY, mouseButton) || buttonMultiplayer.mouseReleased(mouseX, mouseY, mouseButton) || buttonSettings.mouseReleased(mouseX, mouseY, mouseButton)) {}
    }
}
