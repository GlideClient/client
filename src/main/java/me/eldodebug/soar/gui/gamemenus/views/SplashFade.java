package me.eldodebug.soar.gui.gamemenus.views;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.gamemenus.GlideScreen;
import me.eldodebug.soar.gui.gamemenus.MenuManager;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icons;
import me.eldodebug.soar.utils.MathUtils;
import me.eldodebug.soar.utils.Sound;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.easing.BackOutAnimation;
import me.eldodebug.soar.utils.animation.normal.other.DecelerateAnimation;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class SplashFade extends GlideScreen {


    private Animation iconAnimation, fadeBackgroundAnimation;
    boolean soundPlayed = false;

    public SplashFade(MenuManager manager) {
        super(manager, null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        Glide instance = Glide.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        if (fadeBackgroundAnimation == null || !fadeBackgroundAnimation.isDone(Direction.FORWARDS)) {
            nvg.setupAndDraw(() -> drawNanoVG(nvg, sr));
            if (!soundPlayed) {
                Sound.play("soar/audio/start.wav", true);
                soundPlayed = true;
            }
        } else {
            instance.started = true;
            setMenu(instance);
        }
    }

    private void drawNanoVG(NanoVGManager nvg, ScaledResolution sr) {

        if (iconAnimation == null) {
            iconAnimation = new BackOutAnimation(750, 1.0, 0.7f, Direction.FORWARDS);
            iconAnimation.reset();
        }

        if (iconAnimation != null) {

            if (iconAnimation.isDone(Direction.FORWARDS) && fadeBackgroundAnimation == null) {
                fadeBackgroundAnimation = new DecelerateAnimation(400, 1, Direction.FORWARDS);
            }

            double progress = iconAnimation.getValue();
            float iconSize = MathUtils.interpolateFloat(130f, 18f, progress);
            float iconX = MathUtils.interpolateFloat(sr.getScaledWidth() / 2f, 19F, progress);
            float iconY = MathUtils.interpolateFloat(sr.getScaledHeight() / 2f, 19f, progress);

            nvg.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), new Color(0, 0, 0, fadeBackgroundAnimation != null ? (int) (255 - (fadeBackgroundAnimation.getValue() * 255)) : 255));
            nvg.drawCenteredTextWithShadow(Icons.GLIDE, iconX, iconY, new Color(255, 255, 255, 255).getRGB(), 5, iconSize, Fonts.ICON_OUTLINE); // size 130
        }
    }

}
