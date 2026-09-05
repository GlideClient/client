package me.eldodebug.soar.gui.mainmenu.impl.welcome;

import java.awt.Color;

import eu.shoroa.contrib.render.Blur;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.mainmenu.GuiGlideMainMenu;
import me.eldodebug.soar.gui.mainmenu.MainMenuScene;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.Theme;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.other.DecelerateAnimation;
import me.eldodebug.soar.utils.buffer.ScreenAlpha;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.utils.mouse.Scroll;
import me.eldodebug.soar.utils.render.BlurUtils;
import net.minecraft.client.gui.ScaledResolution;

public class ThemeSelectScene extends MainMenuScene {

	private int x, y, width, height;
	
	private Animation fadeAnimation;
	private ScreenAlpha screenAlpha = new ScreenAlpha();
	private Scroll scroll = new Scroll();
	private Theme currentTheme = Theme.DARK;
	
	public ThemeSelectScene(GuiGlideMainMenu parent) {
		super(parent);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		
		width = 280;
		height = 146;
		x = sr.getScaledWidth() / 2 - (width / 2);
		y = sr.getScaledHeight() / 2 - (height / 2);
		
		if(fadeAnimation == null) {
			fadeAnimation = new DecelerateAnimation(800, 1);
			fadeAnimation.setDirection(Direction.FORWARDS);
			fadeAnimation.reset();
		}
		
		BlurUtils.drawBlurScreen(14);
		
		screenAlpha.wrap(() -> drawNanoVG(), fadeAnimation.getValueFloat());
		
		if(fadeAnimation.isDone(Direction.BACKWARDS)) {
			this.setCurrentScene(this.getSceneByClass(AccentColorSelectScene.class));
		}
	}
	
	private void drawNanoVG() {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		AccentColor currentColor = instance.getColorManager().getCurrentColor();
		
		int offsetX = 0;
		int index = 1;

		Blur.drawBlurMod(x, y, width, height, 8);
		nvg.drawRoundedRect(x, y, width, height, 8, this.getBackgroundColor());
		nvg.drawCenteredText("Choose a theme", x + (width / 2), y + 14, Color.WHITE, 16, Fonts.MEDIUM);
		nvg.drawRect(x, y + 27, width, 1, Color.WHITE);
		
		scroll.onScroll();
		scroll.onAnimation();
		
		nvg.save();
		nvg.scissor(x, y + 27, width, height - 27);
		nvg.translate(scroll.getValue(), 0);
		
		for(Theme t : Theme.values()) {
			
			t.getAnimation().setAnimation(currentTheme.equals(t) ? 1.0F : 0.0F, 16);
			
			drawModMenuExample(x + offsetX + 14, y + 42, t);
			nvg.save();
			nvg.setAlpha(t.getAnimation().getValue());
			nvg.drawGradientOutlineRoundedRect(x + offsetX + 14, y + 42, 90, 56, 6, 1, currentColor.getColor1(), currentColor.getColor2());
			nvg.restore();
			nvg.drawCenteredText(t.getName(), x + offsetX + 14 + (90 / 2), y + 109, Color.WHITE, 9.5F, Fonts.REGULAR);
			
			offsetX+=102;
			index++;
		}
		
		scroll.setMaxScroll((index - 3.58F) * 102);
		
		nvg.restore();
		
		nvg.drawRoundedRect(x + width - 86, y + height - 26, 80, 20, 6, this.getBackgroundColor());
		nvg.drawCenteredText("Next", x + width - 86 + (80 / 2), y + height - 15, Color.WHITE, 10, Fonts.REGULAR);
	}
	
	private void drawModMenuExample(float x, float y, Theme theme) {
		
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		
		float width = 90;
		float height = 56;
		float offsetY = 0;
		
		nvg.drawRoundedRect(x, y, width, height, 6, theme.getNormalBackgroundColor());
		nvg.drawRoundedRectVarying(x, y, 12, height, 6, 0, 6, 0, theme.getDarkBackgroundColor());
		
		for(int i = 0; i < 3; i++) {
			
			nvg.drawRoundedRect(x + (15), y + offsetY + (6), width - (20), 12, 2.5F, theme.getDarkBackgroundColor());
			nvg.drawRoundedRect(x + (17), y + offsetY + (7.5F), 9, 9, 2, theme.getNormalBackgroundColor());
			
			offsetY+=16;
		}
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		float offsetX = scroll.getValue();
		
		for(Theme t : Theme.values()) {
			
			if(MouseUtils.isInside(mouseX, mouseY, x + offsetX + 14, y + 42, 90, 56) && mouseButton == 0) {
				currentTheme = t;
			}
			
			offsetX+=102;
		}
		
		if(MouseUtils.isInside(mouseX, mouseY, x + width - 86, y + height - 26, 80, 20) && mouseButton == 0) {
			Glide.getInstance().getColorManager().setTheme(currentTheme);
			fadeAnimation.setDirection(Direction.BACKWARDS);
		}
	}
}
