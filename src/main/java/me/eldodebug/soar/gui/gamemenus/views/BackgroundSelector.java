package me.eldodebug.soar.gui.gamemenus.views;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.gamemenus.GlideScreen;
import me.eldodebug.soar.gui.gamemenus.MenuManager;
import me.eldodebug.soar.gui.gamemenus.elements.ElementCard;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.management.profile.mainmenu.BackgroundManager;
import me.eldodebug.soar.management.profile.mainmenu.impl.Background;
import me.eldodebug.soar.management.profile.mainmenu.impl.CustomBackground;
import me.eldodebug.soar.management.profile.mainmenu.impl.DefaultBackground;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.file.FileUtils;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.utils.mouse.Scroll;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BackgroundSelector extends GlideScreen {

	private Scroll scroll = new Scroll();

	public ArrayList<ElementCard> cards = new ArrayList<>();


	public BackgroundSelector(MenuManager parent) {
		super(parent, "Select A Background");

		 cards.add(0, new ElementCard(TranslateText.NIGHT, TranslateText.APPEARANCE_DESCRIPTION, 50, 50, 120, 65, true, new ResourceLocation("soar/mainmenu/background-unity.png") ,() -> mc.displayGuiScreen(new GuiSelectWorld(this.getMenuManager()))));
	}


	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		ScaledResolution sr = new ScaledResolution(mc);
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		nvg.setupAndDraw(() -> drawNanoVG(mouseX, mouseY, sr, instance, nvg));

	}

	private void drawNanoVG(int mouseX, int mouseY, ScaledResolution sr, Glide instance, NanoVGManager nvg) {
		BackgroundManager backgroundManager = instance.getProfileManager().getBackgroundManager();
		ColorPalette palette = instance.getColorManager().getPalette();

		cards.get(0).render(nvg, mouseX, mouseY);



	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		ScaledResolution sr = new ScaledResolution(mc);

		Glide instance = Glide.getInstance();
		FileManager fileManager = instance.getFileManager();
		BackgroundManager backgroundManager = instance.getProfileManager().getBackgroundManager();

		cards.get(0).mouseClicked(mouseX, mouseY, mouseButton);
		cards.get(0).mouseReleased(mouseX, mouseY, mouseButton);
	}

}