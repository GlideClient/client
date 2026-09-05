package me.eldodebug.soar.gui.gamemenus.views;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.GlideMeta;
import me.eldodebug.soar.gui.gamemenus.GlideScreen;
import me.eldodebug.soar.gui.gamemenus.MenuManager;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.remote.changelog.Changelog;
import me.eldodebug.soar.management.remote.update.Update;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class UpdateScreen extends GlideScreen {

    int startX = 15, startY = 35;
    Update u;

    public UpdateScreen(MenuManager manager) {
        super(manager, "Update Available");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Glide instance = Glide.getInstance();
        u = instance.getUpdateInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        nvg.setupAndDraw(() -> drawNanoVG(nvg, instance, mouseX, mouseY));
    }

    private void drawNanoVG(NanoVGManager nvg, Glide glideInstance, int mouseX, int mouseY) {
        ScaledResolution sr = new ScaledResolution(mc);
        float posY = startY;
        nvg.drawTextWithShadow("Ready to update?", startX, posY, 0xBBFFFFFF, 5, 16, Fonts.MEDIUM);
        posY += nvg.getTextHeight("Ready to update?", 16, Fonts.MEDIUM) + 4;
        nvg.drawTextWithShadow("A new version of GlideClient is now available!", startX, posY, 0xBBFFFFFF, 5, 12, Fonts.REGULAR);
        posY += nvg.getTextHeight("A new version of GlideClient is now available!", 12, Fonts.REGULAR) + 4;
        nvg.drawTextWithShadow(String.format("%s (%s) -> %s (%s)",GlideMeta.VERSION_NUMBER, GlideMeta.VERSION_IDENTIFIER, u.getVersionString(), u.getBuildID()), startX, posY, 0xBBFFFFFF, 5, 8, Fonts.REGULAR);
        posY += 15;
        try {
            for(Changelog c : u.updateChangelog.getChangelogs()) {
                float tbSize = nvg.getTextBoxHeight(c.getText(), 8, Fonts.MEDIUM, 250);
                nvg.drawRoundedRect(startX, posY, 13, 13, 7F, c.getType().getColor());
                nvg.drawCenteredTextWithShadow(c.getType().getText(), startX + 6.5F, posY + 6.5F, 0xFFFFFFFF, 5, 7, Fonts.LEGACYICON);
                nvg.drawTextBox(c.getText(), (float) startX + 17F, posY + 4, 250, Color.WHITE, 8F, Fonts.MEDIUM);
                posY += (tbSize + 12);
            }
        } catch (Exception ignored) {}

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    /*

	private void drawNanoVG(int mouseX, int mouseY, ScaledResolution sr, Glide instance, NanoVGManager nvg) {
		nvg.drawRect(0,0, sr.getScaledWidth(), sr.getScaledHeight(), new Color(0,0,0, 100));
		int acWidth = 220;
		int acHeight = 190;
		int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
		int acY = sr.getScaledHeight() / 2 - (acHeight / 2);
		Update u = instance.getUpdateInstance();
		Blur.drawBlurMod(acX, acY, acWidth, acHeight, 8);
		nvg.drawRoundedRect(acX, acY, acWidth, acHeight, 8, this.getBackgroundColor());
		nvg.drawCenteredText("Update Available", acX + (acWidth / 2), acY + 12, Color.WHITE, 14, Fonts.MEDIUM);
		nvg.drawCenteredText("Would you like to update?", acX + (acWidth / 2), acY + 30, Color.WHITE, 9, Fonts.REGULAR);
		nvg.drawCenteredText(GlideMeta.VERSION_NUMBER + " -> " + u.getVersionString(), acX + (acWidth / 2), acY + 48, Color.WHITE, 9, Fonts.REGULAR);
		nvg.drawCenteredText(GlideMeta.VERSION_IDENTIFIER + " -> " + u.getBuildID(), acX + (acWidth / 2), acY + 60, Color.WHITE, 5, Fonts.REGULAR);
		nvg.drawRoundedRect(acX + acWidth/2 - 90, acY + acHeight - 64, 180, 20, 4.5F, this.getBackgroundColor());
		nvg.drawCenteredText("Go to update", acX + acWidth/2, acY + acHeight - 54 - (nvg.getTextHeight("Go to update", 9.5F, Fonts.REGULAR)/2), Color.WHITE, 9.5F, Fonts.REGULAR);
		nvg.drawRoundedRect(acX + acWidth/2 - 90, acY + acHeight - 32, 180, 20, 4.5F, this.getBackgroundColor());
		nvg.drawCenteredText("Maybe Later", acX + acWidth/2, acY + acHeight - 22 - (nvg.getTextHeight("Maybe Later", 9.5F, Fonts.REGULAR)/2), Color.WHITE, 9.5F, Fonts.REGULAR);
	}

	public void exitGui(){
		Glide instance = Glide.getInstance();
		instance.setUpdateNeeded(false);
		this.setCurrentScene(this.getSceneByClass(MainScene.class));
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			ScaledResolution sr = new ScaledResolution(mc);
			int acWidth = 220;
			int acHeight = 190;
			int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
			int acY = sr.getScaledHeight() / 2 - (acHeight / 2);
			Glide instance = Glide.getInstance();
			if (MouseUtils.isInside(mouseX, mouseY, acX + acWidth/2 - 90, acY + acHeight - 64, 180, 20)) {
				try{ Desktop.getDesktop().browse(new URI(instance.getUpdateInstance().getUpdateLink())); } catch (Exception ignored) {}
			}
			if (MouseUtils.isInside(mouseX, mouseY, acX + acWidth/2 - 90, acY + acHeight - 32, 180, 20)) {
				exitGui();
			}
		}
	}
     */
}
