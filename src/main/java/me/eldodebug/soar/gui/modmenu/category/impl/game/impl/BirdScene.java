package me.eldodebug.soar.gui.modmenu.category.impl.game.impl;

import eu.shoroa.contrib.render.ShBlur;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.category.impl.GamesCategory;
import me.eldodebug.soar.gui.modmenu.category.impl.game.GameScene;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Random;

public class BirdScene extends GameScene {

	private static float deltaTime;

	// game stuff
	private int x, y, width, height, score = 0;
	private boolean gameStarted = false, shouldStart = false;
	private float gravity = 300F;


	// pipe stuff
	private final int pipeHoleHeight = 80, pipeWidth = 30;
	private float pipeOneX, pipeTwoX, pipeOneYGap, pipeTwoYGap, pipeSpeed;

	// player stuff
	private float playerWidth = 15, playerTargetYPosition = 0, playerActualYPosition = 0, playerX;
	private boolean inPipeOne = false, inPipeTwo = false, isPlayerDead = false;

	long deathTime = 0L;

	private static final Random random = new Random();

	public BirdScene(GamesCategory parent) {
		super(parent, "Flappy Glide", "A terrible clone of the game Flappy Bird", LegacyIcon.PLAY);
	}

	@Override
	public void initGui() {
		x = this.getX();
		y = this.getY();
		width = this.getWidth();
		height = this.getHeight();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		DeltaTime.getInstance().update(); // bad practice
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		ColorPalette palette = colorManager.getPalette();
		AccentColor currentColor = colorManager.getCurrentColor();
		deltaTime = DeltaTime.getInstance().getDeltaTime();

		nvg.save();
		nvg.scissor(x, y, width, height);
		if (InternalSettingsMod.getInstance().getBlurSetting().isToggled()) {
			ShBlur.getInstance().drawBlur(() -> nvg.drawRect(x, y, width, height,  palette.getBackgroundColor(ColorType.DARK)));
			Color colsidebar = palette.getBackgroundColor(ColorType.DARK);
			nvg.drawRect(x, y, width, height,  new Color(colsidebar.getRed(), colsidebar.getGreen(), colsidebar.getBlue(), 210));
		} else {
			nvg.drawRect(x, y, width, height,  palette.getBackgroundColor(ColorType.DARK));
		}
		if(gameStarted){
			nvg.drawText(score + "", x + 10, y + 10, currentColor.getColor1(), 8, Fonts.MEDIUM);
			drawPlayer(nvg, currentColor);
			drawPipes(nvg);
			nvg.restore();
			if (!isPlayerDead) { detectCollisions(); }
		} else {
			if(isPlayerDead) {
				nvg.drawCenteredText("You Died!", x + (width/2), y +(height/2) - 20, new Color(255, 31, 57), 15, Fonts.SEMIBOLD);
				nvg.drawCenteredText("Your score is " + score, x + (width/2), y +(height/2)- 2, palette.getFontColor(ColorType.DARK), 8, Fonts.MEDIUM);
				if (System.currentTimeMillis() - deathTime >= 400) {
					nvg.drawCenteredText("Press SPACE or CLICK to start!", x + (width/2), y +(height) - 20, palette.getFontColor(ColorType.DARK), 8, Fonts.MEDIUM);
				}
				if (shouldStart) {
					startGame();
				}
			} else {
				nvg.drawCenteredText("Welcome to Bird!", x + (width/2), y +(height/2) - 20, palette.getFontColor(ColorType.NORMAL), 15, Fonts.SEMIBOLD);
				nvg.drawCenteredText("Press SPACE or CLICK to start!", x + (width/2), y +(height/2)- 2, palette.getFontColor(ColorType.DARK), 8, Fonts.MEDIUM);
				if (shouldStart) {
					startGame();
				}
			}
		}
		nvg.drawOutlineRoundedRect(x, y, width, height, 10, 8, palette.getBackgroundColor(ColorType.NORMAL));
	}

	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		jump();
	}

	public void keyTyped(char typedChar, int keyCode) {
		switch(keyCode){
			case 1: ; break; // exit
			case Keyboard.KEY_SPACE:
			case Keyboard.KEY_UP:
			case Keyboard.KEY_W:
			case Keyboard.KEY_RETURN:
				jump();
				break;
		}
	}

	/*
	 * used to get the height of the top pipe for spawning it
	 */
	public float getNewPipeHeight() {
		float pipeRandomPercent = (random.nextFloat() * 95 - ((pipeHoleHeight*100F)/height));
		return Math.max(10, (pipeRandomPercent/100)*height);
	}

	private void die(){
		deathTime = System.currentTimeMillis();
		isPlayerDead = true;
		gameStarted = false;
	}

	private void detectCollisions(){
		// if player hits pipe 1
		if(pipeOneX <= playerX + (playerWidth/2) && pipeOneX + playerWidth >= playerX - (playerWidth/2)){
			if (y + playerActualYPosition - (playerWidth/2) <=  y + pipeOneYGap || y + playerActualYPosition + (playerWidth/2) >=  y + pipeOneYGap + pipeHoleHeight){ die(); }
			inPipeOne = true;
		} else if (inPipeOne) { score += 1; inPipeOne = false; }
		// if player hits pipe 2
		if(pipeTwoX <= playerX + (playerWidth/2) && pipeTwoX + playerWidth >= playerX - (playerWidth/2)){
			if (y + playerActualYPosition - (playerWidth/2) <=  y + pipeTwoYGap || y + playerActualYPosition + (playerWidth/2) >=  y + pipeTwoYGap + pipeHoleHeight){ die(); }
			inPipeTwo = true;
		} else if (inPipeTwo) { score += 1; inPipeTwo = false; }
		// if player hits the ground
		if (playerActualYPosition > height - (playerWidth/2)) { die(); }
	}

	private void jump(){
		if(!gameStarted){
			if (isPlayerDead){
				if (System.currentTimeMillis() - deathTime >= 400) { shouldStart = true;}
			} else {
				shouldStart = true;
			}
		}
		playerTargetYPosition = playerTargetYPosition - (gravity /3);
	}

	private void drawPlayer(NanoVGManager nvg, AccentColor currentColor){
		if(!isPlayerDead) {
			playerTargetYPosition = Math.min(height - 5, playerTargetYPosition + (gravity * deltaTime)) ;
			playerActualYPosition = linear(playerActualYPosition, playerTargetYPosition, 10F);
		}
		nvg.drawGradientRoundedRect(playerX - (playerWidth/2), y + playerActualYPosition - (playerWidth/2), playerWidth, playerWidth, 3, currentColor.getColor1(), currentColor.getColor2());
	}

	private void drawPipes(NanoVGManager nvg){
		// new pipes
		if (pipeOneX < (x - pipeWidth)){
			pipeOneX = x + width + pipeWidth;
			pipeOneYGap = getNewPipeHeight();
		}
		if (pipeTwoX < (x - pipeWidth)){
			pipeTwoX = x + width + pipeWidth;
			pipeTwoYGap = getNewPipeHeight();
		}
		// move pipes
		if (!isPlayerDead) {
			pipeOneX = pipeOneX - (pipeSpeed * deltaTime);
			pipeTwoX = pipeTwoX - (pipeSpeed * deltaTime);
		}
		// draw xd
		nvg.drawRect(pipeOneX, y, pipeWidth, pipeOneYGap, new Color(75, 255, 158));
		nvg.drawRect(pipeOneX, y + pipeOneYGap + pipeHoleHeight, pipeWidth, height - pipeOneYGap - pipeHoleHeight, new Color(75, 255, 158));

		nvg.drawRect(pipeTwoX, y, pipeWidth, pipeTwoYGap, new Color(75, 255, 158));
		nvg.drawRect(pipeTwoX, y + pipeTwoYGap + pipeHoleHeight, pipeWidth, height - pipeTwoYGap - pipeHoleHeight, new Color(75, 255, 158));
	}

	private void startGame(){
		pipeOneX = x + width + pipeWidth;
		pipeTwoX = x + width + (width /2) + pipeWidth;
		pipeOneYGap = getNewPipeHeight();
		pipeTwoYGap = getNewPipeHeight();
		pipeSpeed = 100f;
		score = 0;
		playerX = x + 40;
		playerTargetYPosition = height/2;
		playerActualYPosition = height/2;
		inPipeOne = false;
		inPipeTwo = false;
		isPlayerDead = false;
		shouldStart = false;
		gameStarted = true;
	}


	public static float linear(float prevVal, float finalVal, float rate) {
		if (prevVal == finalVal || rate == 0) { return finalVal; }
		float difference = finalVal - prevVal;
		float nextValue = prevVal + (difference * (DeltaTime.getInstance().getDeltaTime() * rate));
		return finalVal > prevVal
				? Math.min(nextValue, finalVal)
				: Math.max(nextValue, finalVal);
	}
}
