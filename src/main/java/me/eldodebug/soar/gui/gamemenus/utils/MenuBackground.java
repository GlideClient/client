package me.eldodebug.soar.gui.gamemenus.utils;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.profile.mainmenu.impl.Background;
import me.eldodebug.soar.management.profile.mainmenu.impl.CustomBackground;
import me.eldodebug.soar.management.profile.mainmenu.impl.DefaultBackground;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

public class MenuBackground {

    private SimpleAnimation[] backgroundParallaxAnimations = new SimpleAnimation[2];

    public void drawBackground(ScaledResolution sr, Glide instance, NanoVGManager nvg){
        Background currentBackground = instance.getProfileManager().getBackgroundManager().getCurrentBackground();

        if(currentBackground instanceof DefaultBackground) {

            DefaultBackground bg = (DefaultBackground) currentBackground;

            nvg.drawImage(bg.getImage(), -21 + backgroundParallaxAnimations[0].getValue() / 90, backgroundParallaxAnimations[1].getValue() * -1 / 90, sr.getScaledWidth() + 21, sr.getScaledHeight() + 20);
        }else if(currentBackground instanceof CustomBackground) {

            CustomBackground bg = (CustomBackground) currentBackground;

            nvg.drawImage(bg.getImage(), -21 + backgroundParallaxAnimations[0].getValue() / 90, backgroundParallaxAnimations[1].getValue() * -1 / 90, sr.getScaledWidth() + 21, sr.getScaledHeight() + 20);
        }
    }

    public void initBackground() {
        for(int i = 0; i < backgroundParallaxAnimations.length; i++) {
            backgroundParallaxAnimations[i] = new SimpleAnimation();
        }
    }

    public void updateParallax() {
        backgroundParallaxAnimations[0].setAnimation(Mouse.getX(), 16);
        backgroundParallaxAnimations[1].setAnimation(Mouse.getY(), 16);
    }

}
