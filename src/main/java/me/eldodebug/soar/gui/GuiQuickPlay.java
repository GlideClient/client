package me.eldodebug.soar.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.quickplay.QuickPlay;
import me.eldodebug.soar.management.quickplay.QuickPlayCommand;
import me.eldodebug.soar.management.quickplay.QuickPlayManager;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.easing.EaseBackIn;
import me.eldodebug.soar.utils.animation.normal.other.SmoothStepAnimation;
import me.eldodebug.soar.utils.buffer.ScreenAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.utils.mouse.Scroll;
import me.eldodebug.soar.utils.render.BlurUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public class GuiQuickPlay extends GuiScreen {

	private Scroll scroll = new Scroll();
	
	private Animation introAnimation;
	private ScreenAnimation screenAnimation = new ScreenAnimation();
	private Animation sceneChangeAnimation;
	
	private QuickPlay currentQuickPlay;
	private int x, y, width, height;
	
	@Override
	public void initGui() {
		
		ScaledResolution sr = new ScaledResolution(mc);
		
		int addX = 190;
		int addY = 110;
		
		x = (sr.getScaledWidth() / 2) - addX;
		y = (sr.getScaledHeight() / 2) - addY;
		width = addX * 2;
		height = addY * 2;
		
		introAnimation = new EaseBackIn(320, 1.0F, 2.0F);
		introAnimation.setDirection(Direction.FORWARDS);
		sceneChangeAnimation = new SmoothStepAnimation(260, 1.0);
		sceneChangeAnimation.setValue(1.0);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		
		BlurUtils.drawBlurScreen(20);
		
		screenAnimation.wrap(() -> {
			nvg.drawShadow(x, y, width, height, 12);
		}, 2 - introAnimation.getValueFloat(), Math.min(introAnimation.getValueFloat(), 1));
		
		screenAnimation.wrap(() -> drawNanoVG(), x, y, width, height, 2 - introAnimation.getValueFloat(), Math.min(introAnimation.getValueFloat(), 1), true);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	private void drawNanoVG() {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorPalette palette = instance.getColorManager().getPalette();
		QuickPlayManager quickPlayManager = instance.getQuickPlayManager();
		
		int offsetX = 0;
		int offsetY = 0;
		int index = 1;
		
		if(introAnimation.isDone(Direction.BACKWARDS)) {
			mc.displayGuiScreen(null);
		}
		
		if(sceneChangeAnimation.isDone(Direction.FORWARDS)) {
			currentQuickPlay = null;
		}
		
		nvg.drawRoundedRect(x, y, width, height, 12, palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawCenteredText("Choose a " + (currentQuickPlay != null ? "Mode" : "Game"), x + (width / 2), y + 10, palette.getFontColor(ColorType.DARK), 15, Fonts.MEDIUM);
		//nvg.drawRect(x, y + 28, width, 1, palette.getBackgroundColor(ColorType.DARK));
		
		nvg.save();
		nvg.translate((float) -(600 - (sceneChangeAnimation.getValue() * 600)), 0);
		
		for(QuickPlay q : quickPlayManager.getQuickPlays()) {
			
			nvg.drawRoundedRect(x + 15 + offsetX, y + 42 + offsetY, 110, 42, 6, palette.getBackgroundColor(ColorType.DARK));
			nvg.drawRoundedImage(q.getIcon(), x + 20 + offsetX, y + 47 + offsetY, 32, 32, 6);
			
			nvg.drawText(q.getName(), x + 58 + offsetX, y + 50 + offsetY, palette.getFontColor(ColorType.DARK), 10, Fonts.MEDIUM);
			
			offsetX+=120;
			
			if(index % 3 == 0) {
				offsetX = 0;
				offsetY+=52;
			}
			
			index++;
		}
		
		nvg.restore();
		
		nvg.save();
		nvg.translate((float) (sceneChangeAnimation.getValue() * 600), 0);
		
		if(currentQuickPlay != null) {
			
			int prevIndex = 0;
			
			index = 1;
			offsetX = 0;
			offsetY = 0;
			
			scroll.onScroll();
			scroll.onAnimation();
			
			nvg.scissor(x, y + 29, width, height);
			nvg.translate(0, scroll.getValue());
			
			nvg.drawRoundedImage(currentQuickPlay.getIcon(), x + (width / 2) - (46 / 2), y + 30 + 10, 46, 46, 6);
			nvg.drawCenteredText(currentQuickPlay.getName(), x + (width / 2), y + 94, palette.getFontColor(ColorType.DARK), 12, Fonts.MEDIUM);
			
			for(QuickPlayCommand c : currentQuickPlay.getCommands()) {
				
				nvg.drawRoundedRect(x + 15 + offsetX, y + 80 + 32 + offsetY, 110, 20, 6, palette.getBackgroundColor(ColorType.DARK));
				nvg.drawCenteredText(c.getName(), x + 15 + offsetX + (110 / 2), y + 80 + 38.5F + offsetY, palette.getFontColor(ColorType.NORMAL), 9, Fonts.REGULAR);
				
				offsetX+=120;
				
				if(index % 3 == 0) {
					offsetY+=30;
					offsetX = 0;
					prevIndex++;
				}
				
				index++;
			}
			
			scroll.setMaxScroll(prevIndex <= 3 ? 0 : (((prevIndex + (prevIndex % 3 == 0F ? 0.5F : 0F)) * 30) / 1.48F) - 30);
		}
		
		nvg.restore();
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		Glide instance = Glide.getInstance();
		QuickPlayManager quickPlayManager = instance.getQuickPlayManager();
		
		int offsetX = 0;
		int offsetY = 0;
		int index = 1;
		
		try {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		} catch (IOException e) {}
		
		if(currentQuickPlay == null) {
			for(QuickPlay q : quickPlayManager.getQuickPlays()) {
				
				if(MouseUtils.isInside(mouseX, mouseY, x + 15 + offsetX, y + 42 + offsetY, 110, 42) && mouseButton == 0) {
					scroll.resetAll();
					currentQuickPlay = q;
					sceneChangeAnimation.setDirection(Direction.BACKWARDS);
					return;
				}
				
				offsetX+=120;
				
				if(index % 3 == 0) {
					offsetX = 0;
					offsetY+=52;
				}
				
				index++;
			}
		} else {
			
			index = 1;
			offsetX = 0;
			offsetY = (int) (0 + scroll.getValue());
			
			for(QuickPlayCommand c : currentQuickPlay.getCommands()) {
				
				if(MouseUtils.isInside(mouseX, mouseY, x + 15 + offsetX, y + 80 + 32 + offsetY, 110, 20) && mouseButton == 0 && sceneChangeAnimation.isDone(Direction.BACKWARDS)) {
					mc.thePlayer.sendChatMessage(c.getCommand());
				}
				
				offsetX+=120;
				
				if(index % 3 == 0) {
					offsetY+=30;
					offsetX = 0;
				}
				
				index++;
			}
		}
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		
		if(keyCode == Keyboard.KEY_ESCAPE) {
			
			if(currentQuickPlay != null) {
				sceneChangeAnimation.setDirection(Direction.FORWARDS);
			} else {
				introAnimation.setDirection(Direction.BACKWARDS);
			}
		}
	}
	
	@Override
    public boolean doesGuiPauseGame() {
    	return false;
    }
}
