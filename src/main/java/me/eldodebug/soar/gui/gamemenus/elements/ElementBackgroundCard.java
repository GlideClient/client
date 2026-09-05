package me.eldodebug.soar.gui.gamemenus.elements;

import eu.shoroa.contrib.render.Blur;
import me.eldodebug.soar.gui.widget.WidgetButtonBase;
import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icons;
import me.eldodebug.soar.management.profile.mainmenu.BackgroundManager;
import me.eldodebug.soar.management.profile.mainmenu.impl.Background;
import me.eldodebug.soar.management.profile.mainmenu.impl.CustomBackground;
import me.eldodebug.soar.management.profile.mainmenu.impl.DefaultBackground;
import me.eldodebug.soar.management.profile.mainmenu.impl.PanoramaBackground;
import me.eldodebug.soar.types.Color;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.util.ResourceLocation;

import java.io.File;

public class ElementBackgroundCard extends WidgetButtonBase {
    private final String title;
    private final Background bg;
    private final BackgroundManager bm;
    private final File imgFile;
    private final ResourceLocation img;
    String imgIcon = Icons.DOCUMENT_QUESTION_MARK_20;
    Runnable doSomething = null;
    String doSomethingIcon = null;

    private final Color COLOR = new Color(0x00E0E0E0);

    public ElementBackgroundCard(Background bg, BackgroundManager bm, float x, float y, float width, float height, Runnable onClick) {
        this(bg, bm, x, y, width, height, onClick, null, null);
    }

    public ElementBackgroundCard(Background bg, BackgroundManager bm, float x, float y, float width, float height, String imgIcon, Runnable onClick) {
        this(bg, bm, x, y, width, height, onClick, null, null);
        this.imgIcon = imgIcon;
    }


    public ElementBackgroundCard(Background bg, BackgroundManager bm, float x, float y, float width, float height, Runnable mainAction, String icon, Runnable doSomething) {
        super(x, y, width, height, mainAction);
        this.bg = bg;
        this.bm = bm;
        this.doSomething = doSomething;
        this.doSomethingIcon = icon;

        if (bg instanceof CustomBackground){
            this.title = bg.getName();
            this.imgFile = ((CustomBackground) bg).getImage();
            this.img = null;
        } else {
            this.imgFile = null;
            if (bg instanceof DefaultBackground) {
                this.title = bg.getName();
                if (bg.getId() == 999){
                    img = null;  // fake add button
                } else {
                    img = ((DefaultBackground) bg).getImage();
                }
            } else if (bg instanceof PanoramaBackground){
                this.title = bg.getName();
                img = ((PanoramaBackground) bg).getImage();
            } else {
                throw new IllegalStateException("WHAT THE FUCK ADD THE IMAGE INTO THE BG SELECTOR U ABSOLUTE UPSIDE DOWN BUCKET, IF YOU AS A USER SEE THIS CALL BREADCAT AN IDIOT");
            }
        }
    }


    @Override
    public void render(NanoVGManager renderer, float mouseX, float mouseY) {
        super.render(renderer, mouseX, mouseY);

        if (isHovered) hoverAnimation.forceFinish();

        COLOR.setAlpha(0.3f + hoverAnimation.getLinearValue() * 0.2f);

        renderer.drawShadow(getX(), getY(), getWidth(), getHeight(), 4.5F);
        if (img != null){
            renderer.drawRoundedImage(img, getX(), getY(), getWidth(), getHeight(), 4.5F);
        } else if (imgFile != null) {
            renderer.drawRoundedImage(imgFile, getX(), getY(), getWidth(), getHeight(), 4.5F);
        } else {
            Blur.drawBlurMod(getBounds(), 4.5F);
            renderer.drawRoundedRect(getBounds(), 4.5F, COLOR.toARGB());
            renderer.drawTextWithShadow(imgIcon, getX() + 5, getY() + 5, 0xFFFFFFFF, 5, 20F, Fonts.ICON_OUTLINE);
        }

        renderer.drawTextWithShadow(renderer.getLimitText(title, 10, Fonts.SEMIBOLD, getWidth() - 20), getX() + 5, getY() + getHeight() - 5 - renderer.getTextHeight(title, 10f, Fonts.SEMIBOLD), -1, 5, 10F, Fonts.SEMIBOLD);

        if (doSomethingIcon != null) {
            renderer.drawRoundedRect(getX() + getWidth() - 25, getY() + 5, 20, 20, 2.4F, 0x45FFFFFF);
            renderer.drawCenteredTextWithShadow(doSomethingIcon, getX() + getWidth() - 15, getY() +  15, 0xFFFFFFFF, 5, 10F, Fonts.ICON_OUTLINE);
        }

        if (bm.getCurrentBackground().equals(bg)){
            renderer.drawOutlineRoundedRect(getBounds().x, getBounds().y, getBounds().width, getBounds().height, 4.5F, 2,  0xFFEFEFEF);
        }
    }

    @Override
    public boolean mouseReleased(float mouseX, float mouseY, int button) {
        if (MouseUtils.isInside((int) mouseX, (int) mouseY, getX() + getWidth() - 25, getY() + 5, 20, 20)) {
            try {
                if (doSomething != null && doSomethingIcon != null) doSomething.run();
                wasClicked = false;
                return true;
            } catch (Exception whatFuck) {
                GlideLogger.error("You found an unintentional feature...", whatFuck);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public Background getBg() {
        return bg;
    }
}
