package eu.shoroa.contrib.gui;

import eu.shoroa.contrib.cosmetic.Cosmetic;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.ui.comp.Comp;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.other.SmoothStepAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;

public class CompCosmetic extends Comp {
    private final Cosmetic cosmetic;

    private final float width = 90f;
    private final float height = 140f;
    private final Animation animation;

    public CompCosmetic(Cosmetic cosmetic) {
        super(0f, 0f);
        this.cosmetic = cosmetic;
        animation = new SmoothStepAnimation(300, 1.0);
        animation.setValue(cosmetic.isEnabled() ? 1f : 0f);
    }

    public void translate(float x, float y) {
        setX(x);
        setY(y);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        animation.setDirection(cosmetic.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);

        Glide instance = Glide.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        AccentColor accentColor = colorManager.getCurrentColor();
        ColorPalette palette = colorManager.getPalette();

        nvg.drawRoundedRect(getX(), getY(), width, height, 9f, ColorUtils.interpolateColor(palette.getBackgroundColor(ColorType.DARK), accentColor.getInterpolateColor(), animation.getValue()));
        nvg.drawRoundedRect(getX() + 2f, getY() + 2f, width - 4f, height - 4f, 7f, palette.getBackgroundColor(ColorType.NORMAL));
//        nvg.drawRoundedRect(getX() + 6f, getY() + 6f, width - 12f, height - 24f, 5f, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawRoundedImage(cosmetic.getPreviewImage(), getX() + 6f, getY() + 6f, (width - 12f), height - 24f, 5f);
        nvg.drawCenteredText(cosmetic.getName(), getX() + getWidth() / 2f, getY() + getHeight() - 15f, palette.getFontColor(ColorType.NORMAL), 10f, Fonts.REGULAR);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight()))
            return;

        if (mouseButton == 0) {
            cosmetic.toggle();
        }
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
