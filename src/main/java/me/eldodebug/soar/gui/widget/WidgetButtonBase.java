package me.eldodebug.soar.gui.widget;

import eu.shoroa.contrib.animation.Animate;
import eu.shoroa.contrib.animation.Easing;
import me.eldodebug.soar.management.nanovg.NanoVGManager;

public abstract class WidgetButtonBase extends Widget {
    private Runnable onClick;

    public boolean wasClicked = false;
    protected boolean isHovered = false;
    /**
     * 0 = left click,
     * 1 = right click,
     * 2 = middle click
     */
    public int actionButton = 0;

    public final Animate hoverAnimation = new Animate(5f, Easing.EXPO_OUT).easeIf(() -> isHovered);
    public final Animate clickAnimation = new Animate(8f, Easing.QUINT_OUT).easeIf(() -> wasClicked);

    public WidgetButtonBase(float x, float y, float width, float height, Runnable onClick) {
        super(x, y, width, height);
        this.onClick = onClick;
    }

    @Override
    public void render(NanoVGManager renderer, float mouseX, float mouseY) {
        isHovered = isHovered(mouseX, mouseY);

        hoverAnimation.update();
        clickAnimation.update();
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (isHovered) {
            if (button == actionButton) {
                if (!wasClicked) {
                    wasClicked = true;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(float mouseX, float mouseY, int button) {
        if (isHovered) {
            if (wasClicked) {
                if (button == actionButton) {
                    onClick.run();
                    wasClicked = false;
                    return true;
                }
            }
        } else {
            wasClicked = false;
        }
        return false;
    }
}
