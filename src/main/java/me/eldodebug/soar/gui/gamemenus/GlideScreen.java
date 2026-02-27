package me.eldodebug.soar.gui.gamemenus;

import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.Minecraft;

import java.awt.*;

public class GlideScreen {

    public Minecraft mc = Minecraft.getMinecraft();
    private MenuManager menuManager;
    private SimpleAnimation animation = new SimpleAnimation();
    private String menuName = "";

    public GlideScreen(MenuManager manager, String menuName) {
        this.menuManager = manager;
        this.menuName = menuName;
    }

    public void initScene() {}

    public void initGui() {}

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {}

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}

    public void keyTyped(char typedChar, int keyCode) {}

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    public void handleInput() {}

    public void onGuiClosed() {}

    public void onSceneClosed() {}

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public void setCurrentView(GlideScreen view) {
        menuManager.setCurrentView(view);
    }

    public Color getBackgroundColor() {
        return menuManager.getBackgroundColor();
    }

    public SimpleAnimation getAnimation() {
        return animation;
    }

    public GlideScreen getViewByClass(Class<? extends GlideScreen> clazz) {
        return menuManager.getViewByClass(clazz);
    }

    public String getMenuName(){
        return this.menuName;
    }
}
