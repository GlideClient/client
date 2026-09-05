package me.eldodebug.soar.gui.gamemenus.elements;

import eu.shoroa.contrib.render.Blur;
import me.eldodebug.soar.gui.widget.WidgetButtonBase;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.types.Color;
import net.minecraft.util.ResourceLocation;

import java.io.File;

public class ElementCard extends WidgetButtonBase {
    private final TranslateText title;
    private final String titleS;
    private final TranslateText description;
    private final ResourceLocation img;
    private final File imgFile;

    private final Color COLOR = new Color(0x00E0E0E0);

    private boolean selectable = false, selected = false;

    public ElementCard(TranslateText title, float x, float y, float width, float height, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.titleS = "";
        this.title = title;
        this.description = TranslateText.EMPTY_STRING;
        this.img = null;
        this.imgFile = null;
    }

    public ElementCard(TranslateText title, float x, float y, float width, float height, boolean selectable, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.titleS = "";
        this.title = title;
        this.description = TranslateText.EMPTY_STRING;
        this.selectable = selectable;
        this.img = null;
        this.imgFile = null;
    }

    public ElementCard(TranslateText title, TranslateText description, float x, float y, float width, float height, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.titleS = "";
        this.title = title;
        this.description = description;
        this.img = null;
        this.imgFile = null;
    }

    public ElementCard(TranslateText title, TranslateText description, float x, float y, float width, float height, boolean selectable, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.titleS = "";
        this.title = title;
        this.description = description;
        this.selectable = selectable;
        this.img = null;
        this.imgFile = null;
    }

    public ElementCard(TranslateText title, TranslateText description, float x, float y, float width, float height, boolean selectable, ResourceLocation img, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.titleS = "";
        this.title = title;
        this.description = description;
        this.selectable = selectable;
        this.img = img;
        this.imgFile = null;
    }

    public ElementCard(String titleS, float x, float y, float width, float height, boolean selectable, boolean selected, ResourceLocation img, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.titleS = titleS;
        this.title = TranslateText.EMPTY_STRING;
        this.description = TranslateText.EMPTY_STRING;
        this.selectable = selectable;
        this.img = img;
        this.imgFile = null;
        this.selected = selected;
    }

    public ElementCard(String titleS, float x, float y, float width, float height, boolean selectable, boolean selected, File img, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.titleS = titleS;
        this.title = TranslateText.EMPTY_STRING;
        this.description = TranslateText.EMPTY_STRING;
        this.selectable = selectable;
        this.img = null;
        this.imgFile = img;
        this.selected = selected;
    }


    @Override
    public void render(NanoVGManager renderer, float mouseX, float mouseY) {
        super.render(renderer, mouseX, mouseY);

        if (isHovered) hoverAnimation.forceFinish();

        COLOR.setAlpha(0.3f + hoverAnimation.getLinearValue() * 0.2f);

        if (img != null){
            renderer.drawRoundedImage(img, getX(), getY(), getWidth(), getHeight(), 4.5F);
        } else if (imgFile != null) {
            renderer.drawRoundedImage(imgFile, getX(), getY(), getWidth(), getHeight(), 4.5F);
        } else {
            Blur.drawBlurMod(getBounds(), 4.5F);
            renderer.drawRoundedRect(getBounds(), 4.5F, COLOR.toARGB());
        }
        if(description != TranslateText.EMPTY_STRING){
            renderer.drawText(title.getText(), getX() + 5, getY() + getHeight() - 5 - renderer.getTextHeight(description.getText(), 10F, Fonts.SEMIBOLD) - renderer.getTextHeight(title.getText(), 10F, Fonts.SEMIBOLD), -1, 10F, Fonts.SEMIBOLD);
            renderer.drawText(description.getText(), getX() + 5, getY() + getHeight() - 5 -  renderer.getTextHeight(description.getText(), 7f,Fonts.REGULAR), -1, 7F, Fonts.REGULAR);
        } else {
            if (title != TranslateText.EMPTY_STRING) {
                renderer.drawText(title.getText(), getX() + 5, getY() + getHeight() - 5 - renderer.getTextHeight(title.getText(), 9.5f, Fonts.SEMIBOLD), -1, 9.5F, Fonts.SEMIBOLD);
            } else {
                renderer.drawText(titleS, getX() + 5, getY() + getHeight() - 5 - renderer.getTextHeight(title.getText(), 9.5f, Fonts.SEMIBOLD), -1, 9.5F, Fonts.SEMIBOLD);
            }
        }
        if (this.selected){
            renderer.drawOutlineRoundedRect(getBounds().x, getBounds().y, getBounds().width, getBounds().height, 4.5F, 2,  0xFFEFEFEF);
        }
    }

    public boolean mouseReleased(float mouseX, float mouseY, int button) {
        this.selected = this.isHovered && button == 0;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public void setSelected(Boolean state) {
        this.selected = state;
    }
}
