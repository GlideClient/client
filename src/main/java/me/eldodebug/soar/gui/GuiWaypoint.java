package me.eldodebug.soar.gui;

import java.awt.Color;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.management.waypoint.Waypoint;
import me.eldodebug.soar.management.waypoint.WaypointManager;
import me.eldodebug.soar.ui.comp.impl.field.CompTextBox;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.easing.EaseBackIn;
import me.eldodebug.soar.utils.buffer.ScreenAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.utils.render.BlurUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public class GuiWaypoint extends GuiScreen {

	private Animation introAnimation;
	private ScreenAnimation screenAnimation = new ScreenAnimation();
	
	private int x, y, width, height;
	private CompTextBox textBox = new CompTextBox();

	private Waypoint removeWaypoint;
	
	private ArrayList<Color> colors = new ArrayList<Color>(); {
		colors.add(Color.RED);
		colors.add(Color.GREEN);
		colors.add(Color.BLUE);
		colors.add(Color.ORANGE);
		colors.add(Color.YELLOW);
		colors.add(Color.MAGENTA);
		colors.add(Color.PINK);
		colors.add(Color.GRAY);
		colors.add(Color.DARK_GRAY);
	}
	
	private Color currentColor = Color.RED;
	
	@Override
	public void initGui() {
		
		ScaledResolution sr = new ScaledResolution(mc);
		
		int addX = 160;
		int addY = 80;
		
		x = (sr.getScaledWidth() / 2) - addX;
		y = (sr.getScaledHeight() / 2) - addY;
		width = addX * 2;
		height = addY * 2;
		
		introAnimation = new EaseBackIn(320, 1.0F, 2.0F);
		introAnimation.setDirection(Direction.FORWARDS);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		BlurUtils.drawBlurScreen(20);
		
		screenAnimation.wrap(() -> drawNanoVG(mouseX, mouseY, partialTicks), x, y, width, height, 2 - introAnimation.getValueFloat(), Math.min(introAnimation.getValueFloat(), 1), false);
	}
	
	private void drawNanoVG(int mouseX, int mouseY, float partialTicks) {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		WaypointManager waypointManager = instance.getWaypointManager();
		ColorManager colorManager = instance.getColorManager();
		ColorPalette palette = colorManager.getPalette();
		
		int offsetX = 0;
		int offsetY = 0;
		int index = 0;
		
		if(introAnimation.isDone(Direction.BACKWARDS)) {
			mc.displayGuiScreen(null);
		}
		
		nvg.drawShadow(x, y, width, height, 10);
		nvg.drawRoundedRect(x, y, width, height, 10, palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawText("Waypoint", x + 8, y + 8, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
		//nvg.drawRect(x, y + 24, width, 1, palette.getBackgroundColor(ColorType.DARK));
		
		for(Waypoint waypoint : waypointManager.getWaypoints()) {
			
			if(waypoint.getWorld().equals(waypointManager.getWorld())) {
				
				nvg.drawRoundedRect(x + 10, y + 35 + offsetY, 170, 28, 6, palette.getBackgroundColor(ColorType.DARK));
				nvg.drawRoundedRect(x + 16, y + 14 + offsetY + 26, 18, 18, 4, waypoint.getColor());
				nvg.drawText(waypoint.getName(), x + 40, y + 45.5F + offsetY, palette.getFontColor(ColorType.DARK), 9.5F, Fonts.REGULAR);
				
				nvg.drawText(LegacyIcon.TRASH, x + 162, y + 44 + offsetY, palette.getMaterialRed(), 11, Fonts.LEGACYICON);
				
				offsetY+=38;
				index++;
			}
		}
		
		nvg.drawRoundedRect(x + width - 130, y + 25 + 10, 120, height - 35 - 10, 6, palette.getBackgroundColor(ColorType.DARK));
		nvg.drawCenteredText("Create a waypoint", x + width - 130 + (120 / 2), y + 25 + 18, palette.getFontColor(ColorType.DARK), 10.5F, Fonts.MEDIUM);
		
		textBox.setDefaultText("Name");
		textBox.setPosition(x + width - 120, y + 25 + 34, 100, 18);
		textBox.draw(mouseX, mouseY, partialTicks);
		
		offsetX = 0;
		offsetY = 0;
		index = 0;
		
		for(Color c : colors) {
			
			nvg.drawRoundedRect(x + width - 120 + offsetX, y + 84 + offsetY, 13, 13, 2, c);
			
			if(currentColor.equals(c)) {
				nvg.drawText(LegacyIcon.CHECK, x + width - 118 + offsetX, y + 86.5F + offsetY, Color.WHITE, 9, Fonts.LEGACYICON);
			}
			
			offsetX+=17;
			index++;
			
			if(index % 6 == 0) {
				offsetY+=17;
				offsetX=0;
			}
		}
		
		nvg.drawRoundedRect(x + width - 85, y + height - 34, 65, 18, 6, palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawCenteredText("Save", x + width - 85 + (65 / 2), y + height - 29, palette.getFontColor(ColorType.DARK), 9, Fonts.REGULAR);
		
		if(removeWaypoint != null) {
			waypointManager.getWaypoints().remove(removeWaypoint);
			removeWaypoint = null;
			waypointManager.save();
		}
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		Glide instance = Glide.getInstance();
		WaypointManager waypointManager = instance.getWaypointManager();
		
		int offsetX = 0;
		int offsetY = 0;
		int index = 0;
		
		for(Waypoint waypoint : waypointManager.getWaypoints()) {
			
			if(waypoint.getWorld().equals(waypointManager.getWorld())) {
				
				if(MouseUtils.isInside(mouseX, mouseY, x + 160, y + 41 + offsetY, 16, 16) && mouseButton == 0) {
					removeWaypoint = waypoint;
				}
				
				offsetY+=38;
				index++;
			}
		}
		
		offsetX = 0;
		offsetY = 0;
		index = 0;
		
		for(Color c : colors) {
			
			if(MouseUtils.isInside(mouseX, mouseY, x + width - 120 + offsetX, y + 84 + offsetY, 13, 13) && mouseButton == 0) {
				currentColor = c;
			}
			
			offsetX+=17;
			index++;
			
			if(index % 6 == 0) {
				offsetY+=17;
				offsetX=0;
			}
		}
		
		if(MouseUtils.isInside(mouseX, mouseY, x + width - 85, y + height - 34, 65, 18) && mouseButton == 0 && !textBox.getText().isEmpty()) {
			waypointManager.addWaypoint(textBox.getText(), mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, currentColor);
			textBox.setText("");
			waypointManager.save();
		}
		
		textBox.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		
		if(keyCode == Keyboard.KEY_ESCAPE) {
			introAnimation.setDirection(Direction.BACKWARDS);
		}
		
		textBox.keyTyped(typedChar, keyCode);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
