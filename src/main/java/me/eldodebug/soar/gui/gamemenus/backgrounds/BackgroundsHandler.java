package me.eldodebug.soar.gui.gamemenus.backgrounds;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.gamemenus.backgrounds.impl.AbstractBackground;
import me.eldodebug.soar.gui.gamemenus.backgrounds.impl.CustomBackgroundRenderer;
import me.eldodebug.soar.gui.gamemenus.backgrounds.impl.DefaultBackgroundRenderer;
import me.eldodebug.soar.gui.gamemenus.backgrounds.impl.PanoramaBackgroundRenderer;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.profile.mainmenu.impl.Background;
import me.eldodebug.soar.management.profile.mainmenu.impl.CustomBackground;
import me.eldodebug.soar.management.profile.mainmenu.impl.DefaultBackground;
import me.eldodebug.soar.management.profile.mainmenu.impl.PanoramaBackground;
import net.minecraft.client.gui.ScaledResolution;

public class BackgroundsHandler {

    private AbstractBackground currentBackground;
    private Background lastBackground;

    public BackgroundsHandler() {
        selectBackground(Glide.getInstance());
    }

    private void selectBackground(Glide instance) {

        Background background = instance.getProfileManager().getBackgroundManager().getCurrentBackground();

        if (background instanceof DefaultBackground) {
            currentBackground = new DefaultBackgroundRenderer();
        } else if (background instanceof CustomBackground) {
            currentBackground = new CustomBackgroundRenderer();
        } else if (background instanceof PanoramaBackground) {
            currentBackground = new PanoramaBackgroundRenderer();
        }

        currentBackground.init();

        this.lastBackground = background;
    }

    public void update(float width, float height) {
        currentBackground.update(width, height);
    }

    public void draw(ScaledResolution sr, Glide instance, NanoVGManager nvg, float partialTicks) {
        Background currentBg = instance.getProfileManager().getBackgroundManager().getCurrentBackground();

        if (currentBg != lastBackground) {
            selectBackground(instance);
        }

        currentBackground.draw(sr, instance, nvg, partialTicks);
    }

}