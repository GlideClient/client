package me.eldodebug.soar.gui.modmenu.category.impl.game.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.category.impl.GamesCategory;
import me.eldodebug.soar.gui.modmenu.category.impl.game.GameScene;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Random;

public class ClickyCatScene extends GameScene {

	// I wrote this barely awake don't bully me too hard please -breadcat

	private final ResourceLocation catImage =  new ResourceLocation("soar/breadcat.png");

	// cat stuff
	private float catX = 0, catY = 0;
	private int rounds, currentRound;

	// score stuff
	private float lastAverage;
	private Long currentTimeStart;
	private float currentTimeAverage;

	// game logic stuff
	private boolean showTitle = true;
	private boolean gameStarted = false;
	private boolean gameFinished = false;
	private long gameFinishedTime = 0L;
	private static float deltaTime;

	// display stuff
	private float progressBarProgress = 0;

	private static final Random random = new Random();

	public ClickyCatScene(GamesCategory parent) {
		super(parent, "Clicky Cat", "Test your reaction time!", LegacyIcon.PLAY);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		DeltaTime.getInstance().update();
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		ColorPalette palette = colorManager.getPalette();
		AccentColor currentColor = colorManager.getCurrentColor();
		deltaTime = DeltaTime.getInstance().getDeltaTime();

		nvg.save();
		nvg.scissor(getX(), getY(), getWidth(), getHeight());

		drawBackground(nvg, palette);
		if (gameStarted){
			gameLogic(nvg, currentColor);
		} else {
			// menus
			if (showTitle){
				nvg.drawCenteredText("Welcome to Clicky Cat!", getX() + (getWidth()/2F), getY() + (getHeight()/2F) - 12, palette.getFontColor(ColorType.NORMAL), 15, Fonts.SEMIBOLD);
				nvg.drawCenteredText("CLICK to start!", getX() + (getWidth()/2F), getY() + (getHeight()/2F), palette.getFontColor(ColorType.DARK), 8, Fonts.MEDIUM);
			} else if (gameFinished) {
				nvg.drawCenteredText("Game Finished!", getX() + (getWidth()/2F), getY() + (getHeight()/2F) - 12, new Color(255, 31, 57), 15, Fonts.SEMIBOLD);
				nvg.drawCenteredText("Your average is " + (int)(lastAverage) + " MS", getX() + (getWidth()/2F), getY() + (getHeight()/2F), palette.getFontColor(ColorType.DARK), 8, Fonts.MEDIUM);
				if ((System.currentTimeMillis() - gameFinishedTime) > 400) {
					nvg.drawCenteredText("Click to play again!", getX() + (getWidth()/2F), getY() + (getHeight()) - 16, palette.getFontColor(ColorType.DARK), 8, Fonts.MEDIUM);
				}
			}
		}
		nvg.restore();
		// make the game rounded xd
		nvg.drawOutlineRoundedRect(getX(), getY(), getWidth(), getHeight(), 10, 8, palette.getBackgroundColor(ColorType.NORMAL));
	}

	private void gameLogic(NanoVGManager nvg, AccentColor currentColor) {
		currentTimeAverage = (float) (System.currentTimeMillis() - currentTimeStart);
		progressBarProgress = anim(progressBarProgress, ((float) currentRound / (float) rounds) * (getWidth() - 8F), 3F, deltaTime);
		nvg.drawRoundedRect(getX() + 4, getY() + getHeight() - 6, progressBarProgress, 3, 1.5F, currentColor.getColor1());
		drawCat(nvg, catX, catY);
		nvg.drawText(currentTimeAverage  + " MS", getX() + 10, getY() + 10, currentColor.getColor1(), 8, Fonts.MEDIUM);
		if (currentRound >= rounds){ // end of game
			progressBarProgress = 0;
			gameFinished = true;
			gameStarted = false;
			gameFinishedTime = System.currentTimeMillis();
		}
		if (catX < getX() || catX > (getX() + getWidth())){ catX = getNewCatX(); }
		if (catY < getY() || catY > (getY() + getHeight())){ catY = getNewCatY(); }
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if(gameStarted){
			if (MouseUtils.isInside(mouseX,mouseY, catX, catY, 32, 32)){
				// cat clicked
				catX = getNewCatX();
				catY = getNewCatY();
				lastAverage = (lastAverage + currentTimeAverage) / 2;
				currentTimeStart = System.currentTimeMillis();
				currentRound += 1;
			}
		} else {
			if ((System.currentTimeMillis() - gameFinishedTime) > 200 || showTitle) {
				// start the game
				showTitle = false;
				gameStarted = true;
				gameFinished = false;
				gameFinishedTime = 0L;
				startGame();
			}
		}

	}

	private void startGame() {
		catX = getNewCatX();
		catY = getNewCatY();
		rounds = getCatQuantity();
		currentRound = 0;
		lastAverage = 0;
		currentTimeAverage = 0;
		currentTimeStart = System.currentTimeMillis();
	}

	public float getNewCatX() {
		return (getX() - 4) + (random.nextFloat() * ((getWidth()-4) - 32));
	}
	public float getNewCatY() {
		return (getY()-4) + (random.nextFloat() * ((getHeight()-6) - 32));
	}

	public int getCatQuantity(){
		return (int)(5 + (random.nextFloat() * 15));
	}

	private void drawCat(NanoVGManager nvg, float x, float y){
		nvg.drawImage(catImage, x, y, 32,32);
	}

}
