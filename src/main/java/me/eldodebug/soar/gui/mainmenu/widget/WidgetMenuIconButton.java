package me.eldodebug.soar.gui.mainmenu.widget;

import eu.shoroa.contrib.render.Blur;
import me.eldodebug.soar.gui.widget.WidgetButtonBase;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Font;
import me.eldodebug.soar.types.Color;
import me.eldodebug.soar.utils.MathUtils;
import org.lwjgl.nanovg.NanoVG;

public class WidgetMenuIconButton extends WidgetButtonBase {
    private final Font iconFont;
    private final String icon;
    private final Color hoverColor;

    private final Color COLOR = new Color(0x78E0E0E0);
    private final Color TEXT_BASE = new Color(-1);
    private final Color TEXT = new Color(-1);

    public WidgetMenuIconButton(Font iconFont, String icon, Color hoverColor, float x, float y, float width, float height, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.iconFont = iconFont;
        this.icon = icon;
        this.hoverColor = hoverColor;
    }

    @Override
    public void render(NanoVGManager renderer, float mouseX, float mouseY) {
        super.render(renderer, mouseX, mouseY);

        if (isHovered) hoverAnimation.forceFinish();

        Color.Interpolate(TEXT_BASE, hoverColor, hoverAnimation.getLinearValue(), TEXT);
        COLOR.setAlpha(0.3f + hoverAnimation.getLinearValue() * 0.2f);

        Blur.drawBlur(getBounds(), 4.5F);
        renderer.drawRoundedRect(getBounds(), 4.5F, COLOR.toARGB());
        renderer.drawBlurredText(icon, getX() + getWidth() / 2f, getY() + getHeight() / 2f, MathUtils.interpolateARGB(0xB2000000, hoverColor.toARGB(), hoverAnimation.getLinearValue()), 12f, 16F, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE, iconFont);
        renderer.drawCenteredText(icon, getX() + getWidth() / 2f, getY() + getHeight() / 2f, TEXT.toARGB(), 16F, iconFont);
    }
}
