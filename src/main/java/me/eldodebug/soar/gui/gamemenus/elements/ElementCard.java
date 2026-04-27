package me.eldodebug.soar.gui.gamemenus.elements;

import eu.shoroa.contrib.render.Blur;
import me.eldodebug.soar.gui.widget.WidgetButtonBase;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.types.Color;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.nanovg.NanoVG;

public class ElementCard extends WidgetButtonBase {
    private final TranslateText title;
    private final TranslateText description;
    private final ResourceLocation img;

    private final Color COLOR = new Color(0x00E0E0E0);

    private boolean selectable = false, selected = false;

    public ElementCard(TranslateText title, float x, float y, float width, float height, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.title = title;
        this.description = TranslateText.EMPTY_STRING;
        this.img = null;
    }

    public ElementCard(TranslateText title, float x, float y, float width, float height, boolean selectable, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.title = title;
        this.description = TranslateText.EMPTY_STRING;
        this.selectable = selectable;
        this.img = null;
    }

    public ElementCard(TranslateText title, TranslateText description, float x, float y, float width, float height, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.title = title;
        this.description = description;
        this.img = null;
    }

    public ElementCard(TranslateText title, TranslateText description, float x, float y, float width, float height, boolean selectable, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.title = title;
        this.description = description;
        this.selectable = selectable;
        this.img = null;
    }

    public ElementCard(TranslateText title, TranslateText description, float x, float y, float width, float height, boolean selectable, ResourceLocation img, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.title = title;
        this.description = description;
        this.selectable = selectable;
        this.img = img;
    }


    @Override
    public void render(NanoVGManager renderer, float mouseX, float mouseY) {
        super.render(renderer, mouseX, mouseY);

        if (isHovered) hoverAnimation.forceFinish();

        COLOR.setAlpha(0.3f + hoverAnimation.getLinearValue() * 0.2f);

        if (img != null){
            renderer.drawRoundedImage(img, getX(), getY(), getWidth(), getHeight(), 4.5F);
        } else {
            Blur.drawBlur(getBounds(), 4.5F);
            renderer.drawRoundedRect(getBounds(), 4.5F, COLOR.toARGB());
        }
        if(description != TranslateText.EMPTY_STRING){
            renderer.drawText(title.getText(), getX() + 5, getY() + getHeight() - 5 - renderer.getTextHeight(description.getText(), 10F, Fonts.SEMIBOLD) - renderer.getTextHeight(title.getText(), 10F, Fonts.SEMIBOLD), -1, 10F, Fonts.SEMIBOLD);
            renderer.drawText(description.getText(), getX() + 5, getY() + getHeight() - 5 -  renderer.getTextHeight(description.getText(), 7f,Fonts.REGULAR), -1, 7F, Fonts.REGULAR);
        } else {
            renderer.drawText(title.getText(), getX() + 5, getY() + getHeight() - 5 - renderer.getTextHeight(title.getText(), 9.5f,Fonts.SEMIBOLD), -1, 9.5F, Fonts.SEMIBOLD);
        }
    }
}
