package me.eldodebug.soar.gui.gamemenus;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.gamemenus.backgrounds.BackgroundsHandler;
import me.eldodebug.soar.gui.gamemenus.views.BackgroundSelector;
import me.eldodebug.soar.gui.gamemenus.views.MainMenuClassic;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icons;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class MenuManager extends GuiScreen {

    private GlideScreen currentView;
    BackgroundsHandler backgroundsHandler = new BackgroundsHandler();


    private ArrayList<GlideScreen> views = new ArrayList<GlideScreen>();

    public MenuManager() {
        Glide instance = Glide.getInstance();

        backgroundsHandler = new BackgroundsHandler();


        addViews();

        // add curent scene setting

        currentView = getViewByClass(BackgroundSelector.class);
    }

    @Override
    public void updateScreen() {
        backgroundsHandler.update(width, height);
    }

    @Override
    public void initGui() {
        currentView.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        ScaledResolution sr = new ScaledResolution(mc);
        Glide instance = Glide.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        boolean isFirstLogin = instance.isFirstLogin();

        backgroundsHandler.draw(sr, instance, nvg, partialTicks);

        nvg.setupAndDraw(() -> drawNanoVG(sr, instance, nvg, mouseX, mouseY));

        if(currentView != null) {
            currentView.drawScreen(mouseX, mouseY, partialTicks);
        }

        // add splash stuff

        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    private void drawNanoVG(ScaledResolution sr, Glide instance, NanoVGManager nvg, int mouseX, int mouseY) {
        drawMenuBar(mouseX, mouseY, sr, nvg);

    }


    private void drawMenuBar(int mouseX, int mouseY, ScaledResolution sr, NanoVGManager nvg){

        // draw logo
        nvg.drawText(Icons.GLIDE, 10, 10, Color.WHITE, 18, Fonts.ICON_FILLED);
        // menu title
        nvg.drawText(currentView.getMenuName(), 32, 12, Color.WHITE, 15, Fonts.MEDIUM);

    }


    private void drawMenuButtons(int mouseX, int mouseY, ScaledResolution sr, NanoVGManager nvg) {

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        ScaledResolution sr = new ScaledResolution(mc);
        Glide instance = Glide.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        boolean isFirstLogin = instance.isFirstLogin();

        if(mouseButton == 0 && !isFirstLogin) {

            // mouse inside logic for buttons

        }

        currentView.mouseClicked(mouseX, mouseY, mouseButton);
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (IOException ignored) {}
    }


    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        currentView.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        currentView.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleInput() throws IOException {
        super.handleInput();
    }

    @Override
    public void onGuiClosed() {
        currentView.onGuiClosed();
    }

    public GlideScreen getCurrentView() {
        return currentView;
    }

    public void setCurrentView(GlideScreen currentView) {

        if(this.currentView != null) {
            this.currentView.onSceneClosed();
        }

        this.currentView = currentView;

        if(this.currentView != null) {
            this.currentView.initScene();
        }
    }

    public void addViews(){
        views.add(new BackgroundSelector(this));
        views.add(new MainMenuClassic(this));
    }

    public GlideScreen getViewByClass(Class<? extends GlideScreen > clazz) {

        for(GlideScreen v : views) {
            if(v.getClass().equals(clazz)) {
                return v;
            }
        }

        return null;
    }

    public Color getBackgroundColor() {
        return new Color(230, 230, 230, 120);
    }

}
