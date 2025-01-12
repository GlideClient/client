package me.eldodebug.soar.ui;

import java.util.ArrayList;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.easing.EaseBackIn;

public class ClickEffects {

	private static ClickEffects instance;
	
	private ArrayList<ClickEffect> effects = new ArrayList<ClickEffect>();
	private ArrayList<ClickEffect> removeEffects = new ArrayList<ClickEffect>();
	
	public ClickEffects() {
		instance = this;
	}
	
	public void drawClickEffects() {
		
		for(ClickEffect ce : effects) {
			
			if(ce.isDone()) {
				removeEffects.add(ce);
			}
			
			ce.draw();
		}
		
		effects.removeAll(removeEffects);
	}
	
	public void addClickEffect(int mouseX, int mouseY) {
		effects.add(new ClickEffect(mouseX, mouseY));
	}
	
	public static ClickEffects getInstance() {
		return instance;
	}

	private class ClickEffect {
		
		private Animation animation;
		private int x, y;
		
		private ClickEffect(int x, int y) {
			this.x = x;
			this.y = y;
			this.animation = new EaseBackIn(650, 1, 0.0F);
		}
		
		public void draw() {
			
			Glide instance = Glide.getInstance();
			NanoVGManager nvg = instance.getNanoVGManager();
			AccentColor currentColor = instance.getColorManager().getCurrentColor();
			
			nvg.setupAndDraw(() -> {
				nvg.drawArc(x, y, animation.getValueFloat() * 8, 0, 360, 2, ColorUtils.applyAlpha(currentColor.getInterpolateColor(0), (int) (255 - (animation.getValueFloat() * 255))));
			});
		}
		
		public boolean isDone() {
			return animation.isDone(Direction.FORWARDS);
		}
	}
}
