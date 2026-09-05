package me.eldodebug.soar.gui.gamemenus;

import eu.shoroa.contrib.render.Blur;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.GlideMeta;
import me.eldodebug.soar.gui.gamemenus.backgrounds.BackgroundsHandler;
import me.eldodebug.soar.gui.gamemenus.views.BackgroundSelector;
import me.eldodebug.soar.gui.gamemenus.views.MainMenuClassic;
import me.eldodebug.soar.gui.gamemenus.views.SplashFade;
import me.eldodebug.soar.gui.gamemenus.views.UpdateScreen;
import me.eldodebug.soar.management.event.impl.EventRenderNotification;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icons;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class MenuManager extends GuiScreen {

    private GlideScreen currentView;
    BackgroundsHandler backgroundsHandler;

    private final ArrayList<ViewMenuButton> viewMenuButtons = new ArrayList<>();

    private ArrayList<GlideScreen> views = new ArrayList<GlideScreen>();

    public MenuManager() {
        Glide instance = Glide.getInstance();
        backgroundsHandler = new BackgroundsHandler();
        addViews();
        setView(instance);
    }

    @Override
    public void initGui() {
        currentView.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        backgroundsHandler.update(width, height);

        ScaledResolution sr = new ScaledResolution(mc);
        Glide instance = Glide.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        boolean isFirstLogin = instance.isFirstLogin();

        backgroundsHandler.draw(sr, instance, nvg, partialTicks);

        Blur.render(5f);

        if(currentView != null) {
            currentView.drawScreen(mouseX, mouseY, partialTicks);
        }

        nvg.setupAndDraw(() -> drawNanoVG(sr, instance, nvg, mouseX, mouseY));

        nvg.setupAndDraw(() -> {
            new EventRenderNotification().call();
        });


        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    private void drawNanoVG(ScaledResolution sr, Glide instance, NanoVGManager nvg, int mouseX, int mouseY) {
        if (GlideMeta.BUILD_TYPE == GlideMeta.Type.DEV){
            nvg.drawTextWithShadow("DEVELOPMENT TEST BUILD, EXPECT BROKEN STUFF", 15, height - 35, 0xFFFF2323, 10, 12, Fonts.SEMIBOLD);
            nvg.drawTextWithShadow(GlideMeta.VERSION_NUMBER + ", " + GlideMeta.VERSION_IDENTIFIER + ", " + GlideMeta.BUILD_TYPE.kind + ", " + GlideMeta.SITE, 15, height - 22, 0xFFFF2323, 10, 10, Fonts.SEMIBOLD);
        }

        if (!(currentView instanceof SplashFade)){
            drawMenuBar(mouseX, mouseY, sr, nvg);
        }
    }


    private void drawMenuBar(int mouseX, int mouseY, ScaledResolution sr, NanoVGManager nvg){

        // draw logo
        nvg.drawTextWithShadow(Icons.GLIDE, 10f, 10f, 0xDFFFFFFF, 5, 18, Fonts.ICON_FILLED);
        // menu title
        nvg.drawTextWithShadow(currentView.getMenuName(), 32, 12, 0xDFFFFFFF, 5, 15, Fonts.MEDIUM);

        drawMenuButtons(mouseX, mouseY, sr, nvg);

    }


    private void drawMenuButtons(int mouseX, int mouseY, ScaledResolution sr, NanoVGManager nvg) {

        if (currentView == null) return;

        float xOffset = sr.getScaledWidth() - 25;
        float yPos = 12;

        for (ViewMenuButton action : currentView.getMenuActions()) {

            boolean hovered = MouseUtils.isInside(mouseX, mouseY, xOffset, yPos, 20, 20);
            Color drawColor = hovered ? action.getHoverColor() : new Color(200, 200, 200);

            nvg.drawText(action.getIcon(), xOffset, yPos, drawColor, 15, Fonts.ICON_OUTLINE);

            xOffset -= 25;
        }
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
        views.add(new SplashFade(this));
        views.add(new UpdateScreen(this));
    }

    public void setView(Glide instance){
        if (!instance.started) {
            currentView = getViewByClass(SplashFade.class);
        } else if (instance.isFirstLogin()) {
            mc.gameSettings.useVbo = true;
            currentView = getViewByClass(MainMenuClassic.class); // change to setup
        } else {
            if (instance.getUpdateNeeded()) {
                currentView = getViewByClass(UpdateScreen.class);
            } else {
                currentView = getViewByClass(MainMenuClassic.class);
            }
        }
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
