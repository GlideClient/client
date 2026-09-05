package me.eldodebug.soar.gui.mainmenu.widget;

import eu.shoroa.contrib.render.Blur;
import me.eldodebug.soar.gui.widget.WidgetButtonBase;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.types.Color;
import org.lwjgl.nanovg.NanoVG;

public class WidgetPlayButton extends WidgetButtonBase {
    private final TranslateText text;

    private final Color COLOR = new Color(0x78E0E0E0);

    public WidgetPlayButton(TranslateText text, float x, float y, float width, float height, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.text = text;
    }

    @Override
    public void render(NanoVGManager renderer, float mouseX, float mouseY) {
        super.render(renderer, mouseX, mouseY);

        if (isHovered) hoverAnimation.forceFinish();

        COLOR.setAlpha(0.3f + hoverAnimation.getLinearValue() * 0.2f);

        Blur.drawBlurMod(getBounds(), 4.5F);
        renderer.drawRoundedRect(getBounds(), 4.5F, COLOR.toARGB());
        renderer.drawBlurredText(text.getText(), getX() + getWidth() / 2f, getY() + getHeight() / 2f, 0xFF000000, 12f, 9.5F, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE, Fonts.SEMIBOLD);
        renderer.drawCenteredText(text.getText(), getX() + getWidth() / 2f, getY() + getHeight() / 2f, -1, 9.5F, Fonts.SEMIBOLD);
    }
}
