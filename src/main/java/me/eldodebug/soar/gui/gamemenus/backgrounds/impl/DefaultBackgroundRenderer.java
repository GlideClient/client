package me.eldodebug.soar.gui.gamemenus.backgrounds.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.profile.mainmenu.impl.Background;
import me.eldodebug.soar.management.profile.mainmenu.impl.CustomBackground;
import me.eldodebug.soar.management.profile.mainmenu.impl.DefaultBackground;
import me.eldodebug.soar.types.Rect;
import me.eldodebug.soar.types.Size;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

public class DefaultBackgroundRenderer extends AbstractBackground {

    private final SimpleAnimation[] backgroundParallaxAnimations = new SimpleAnimation[2];

    private Rect screenRect = new Rect();
    private Rect backgroundRect = new Rect();
    private Size backgroundSize = new Size();

    public void draw(ScaledResolution sr, Glide instance, NanoVGManager nvg, float partialTicks){
        Background currentBackground = instance.getProfileManager().getBackgroundManager().getCurrentBackground();

        DefaultBackground bg = (DefaultBackground) currentBackground;

        nvg.setupAndDraw(() -> {
            screenRect.set(-21 + backgroundParallaxAnimations[0].getValue() / 90, backgroundParallaxAnimations[1].getValue() * -1 / 90, sr.getScaledWidth() + 21, sr.getScaledHeight() + 20);
            if (nvg.getAssetManager().loadImage(nvg.getContext(), bg.getImage())) {
                nvg.imageSize(nvg.getAssetManager().getImage(bg.getImage()), backgroundSize);
                Rect.cover(backgroundSize, screenRect, backgroundRect);
                nvg.drawImage(bg.getImage(), backgroundRect);
            }
        });

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
