package me.eldodebug.soar.gui.mainmenu.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.mainmenu.GuiGlideMainMenu;
import me.eldodebug.soar.gui.mainmenu.MainMenuScene;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.remote.update.Update;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.net.URI;

public class DiscontinuedSoar8 extends MainMenuScene {

	public DiscontinuedSoar8(GuiGlideMainMenu parent) {
		super(parent);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		
		nvg.setupAndDraw(() -> drawNanoVG(mouseX, mouseY, sr, instance, nvg));
	}

	private void drawNanoVG(int mouseX, int mouseY, ScaledResolution sr, Glide instance, NanoVGManager nvg) {
		nvg.drawRect(0,0, sr.getScaledWidth(), sr.getScaledHeight(), new Color(0, 0, 0, 210));
		int acWidth = 350;
		int acHeight = 190;
		int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
		int acY = sr.getScaledHeight() / 2 - (acHeight / 2);

		nvg.drawRoundedRect(acX, acY, acWidth, acHeight, 20, new Color(245, 249, 239));
		nvg.drawCenteredText("Soar Client 8", acX + (acWidth / 2), acY + 12, new Color(24, 29, 23), 14, Fonts.SEMIBOLD);

		nvg.drawCenteredText("Soar Client V8 is finally ready for public use and because of this", acX + (acWidth / 2), acY + 35, new Color(66, 73, 64), 9, Fonts.REGULAR);
		nvg.drawCenteredText("GlideClient is now discontinued so we ask that you get Soar 8.", acX + (acWidth / 2), acY + 45, new Color(66, 73, 64), 9, Fonts.REGULAR);

		nvg.drawCenteredText("Soar 8 features many new modern features such as its material", acX + (acWidth / 2), acY + 60, new Color(66, 73, 64), 9, Fonts.REGULAR);
		nvg.drawCenteredText("you design, better performance and so much more to discover!", acX + (acWidth / 2), acY + 70, new Color(66, 73, 64), 9, Fonts.REGULAR);

		nvg.drawCenteredText("Thank you for supporting Glide we appreciated it so much <3", acX + (acWidth / 2), acY + 90, new Color(66, 73, 64), 9, Fonts.REGULAR);

		nvg.drawCenteredText("Anyway... It's time to stop Gliding and start Soaring!", acX + (acWidth / 2), acY + 135, new Color(66, 73, 64), 9, Fonts.REGULAR);

		nvg.drawRoundedRect(acX + acWidth/2 + 5, acY + acHeight - 32, 90, 20, 10F, new Color(58, 105, 58));
		nvg.drawCenteredText("Get Soar 8", acX + acWidth/2 + 50, acY + acHeight - 22 - (nvg.getTextHeight("Get Soar 8", 9.5F, Fonts.REGULAR)/2), Color.WHITE, 9.5F, Fonts.REGULAR);

		nvg.drawRoundedRect(acX + acWidth/2 - 95, acY + acHeight - 32, 90, 20, 10F, new Color(212, 231, 206));
		nvg.drawCenteredText("Maybe Later", acX + acWidth/2 - 50, acY + acHeight - 22 - (nvg.getTextHeight("Maybe Later", 9.5F, Fonts.REGULAR)/2), new Color(59, 75, 57), 9.5F, Fonts.REGULAR);

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
			int acWidth = 350;
			int acHeight = 190;
			int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
			int acY = sr.getScaledHeight() / 2 - (acHeight / 2);
			if (MouseUtils.isInside(mouseX, mouseY, acX + acWidth/2 + 5, acY + acHeight - 32, 90, 20)) {
				try{ Desktop.getDesktop().browse(new URI("https://glideclient.github.io/soar8")); } catch (Exception ignored) {}
			}
			if (MouseUtils.isInside(mouseX, mouseY, acX + acWidth/2 - 95, acY + acHeight - 32, 90, 20)) {
				exitGui();
			}
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			exitGui();
		}
	}
}
