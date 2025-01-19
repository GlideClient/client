package me.eldodebug.soar.gui.mainmenu.impl.welcome;

import java.awt.Color;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.mainmenu.GuiGlideMainMenu;
import me.eldodebug.soar.gui.mainmenu.MainMenuScene;
import me.eldodebug.soar.gui.mainmenu.impl.MainScene;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.utils.Sound;
import me.eldodebug.soar.utils.TimerUtils;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.other.DecelerateAnimation;
import me.eldodebug.soar.utils.render.BlurUtils;
import net.minecraft.client.gui.ScaledResolution;

public class LastMessageScene extends MainMenuScene {

	private Animation fadeAnimation, blurAnimation;
	private int step;
	private String message;
	private boolean soundPlayed = false;
	
	private TimerUtils timer = new TimerUtils();
	
	public LastMessageScene(GuiGlideMainMenu parent) {
		super(parent);
		step = 0;
		blurAnimation = new DecelerateAnimation(800, 13);
		blurAnimation.setValue(13);
	}


	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		String compMessage = "Setup is complete!";
		String welcomeMessage = "Thank you for choosing Glide Client!";
		
		BlurUtils.drawBlurScreen(1 + blurAnimation.getValueFloat());
		
		if(fadeAnimation == null && this.getParent().isDoneBackgroundAnimation()) {
			fadeAnimation = new DecelerateAnimation(800, 1);
			fadeAnimation.setDirection(Direction.FORWARDS);
			fadeAnimation.reset();
			timer.reset();
		}
		
		if(blurAnimation.isDone(Direction.BACKWARDS)) {
			Glide.getInstance().createFirstLoginFile();
			this.setCurrentScene(this.getSceneByClass(MainScene.class));
		}
		
		if(fadeAnimation != null) {
			
			switch(step) {
				case 0:
					message = compMessage;
					break;
				case 1:
					message = welcomeMessage;
					break;
			}
			if(!soundPlayed) {
				Sound.play("soar/audio/success.wav", true);
				soundPlayed = true;
			}
			nvg.setupAndDraw(() -> {
				nvg.drawCenteredText(message, sr.getScaledWidth() / 2, 
						(sr.getScaledHeight() / 2) - (nvg.getTextHeight(message, 26, Fonts.REGULAR) / 2), 
						new Color(255, 255, 255, (int) (fadeAnimation.getValueFloat() * 255)), 26, Fonts.REGULAR);
			});
			
			if(timer.delay(3000) && fadeAnimation.getDirection().equals(Direction.FORWARDS)) {
				fadeAnimation.setDirection(Direction.BACKWARDS);
				timer.reset();
			}
			
			if(fadeAnimation.isDone(Direction.BACKWARDS)) {
				
				if(step == 1) {
					blurAnimation.setDirection(Direction.BACKWARDS);
					return;
				}
				
				step++;
				fadeAnimation.setDirection(Direction.FORWARDS);
			}
		}
	}
}