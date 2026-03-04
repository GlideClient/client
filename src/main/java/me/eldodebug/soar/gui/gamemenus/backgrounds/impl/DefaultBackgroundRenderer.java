package me.eldodebug.soar.gui.gamemenus.backgrounds.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.profile.mainmenu.impl.Background;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

public class DefaultBackgroundRenderer extends AbstractBackground {

    private final SimpleAnimation[] backgroundParallaxAnimations = new SimpleAnimation[2];

    public void draw(ScaledResolution sr, Glide instance, NanoVGManager nvg, float partialTicks){
        Background currentBackground = instance.getProfileManager().getBackgroundManager().getCurrentBackground();

        me.eldodebug.soar.management.profile.mainmenu.impl.DefaultBackground bg = (me.eldodebug.soar.management.profile.mainmenu.impl.DefaultBackground) currentBackground;

        nvg.setupAndDraw(() -> nvg.drawImage(bg.getImage(), -21 + backgroundParallaxAnimations[0].getValue() / 90, backgroundParallaxAnimations[1].getValue() * -1 / 90, sr.getScaledWidth() + 21, sr.getScaledHeight() + 20));
    }

    public void init() {
        for(int i = 0; i < backgroundParallaxAnimations.length; i++) {
            backgroundParallaxAnimations[i] = new SimpleAnimation();
        }

    }

    public void update(float width, float height) {
        backgroundParallaxAnimations[0].setAnimation(Mouse.getX(), 16);
        backgroundParallaxAnimations[1].setAnimation(Mouse.getY(), 16);
    }

}
