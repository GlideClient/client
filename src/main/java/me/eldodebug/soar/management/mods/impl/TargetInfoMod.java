package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.TargetUtils;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.easing.EaseBackIn;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.buffer.ScreenAnimation;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;

public class TargetInfoMod extends HUDMod {

	private SimpleAnimation healthAnimation = new SimpleAnimation();
	private SimpleAnimation armorAnimation = new SimpleAnimation();
	private ScreenAnimation screenAnimation = new ScreenAnimation();
	private Animation introAnimation;

	private String name;
	private float health, armor;
	private ResourceLocation head;
	
	public TargetInfoMod() {
		super(TranslateText.TARGET_INFO, TranslateText.TARGET_INFO_DESCRIPTION, "targethud", true);
	}
	
	@Override
	public void setup() {
		introAnimation = new EaseBackIn(320, 1.0F, 2.0F);
		introAnimation.setDirection(Direction.BACKWARDS);
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		
		AbstractClientPlayer target = TargetUtils.getTarget();
		
		if(this.isEditing()) {
			target = mc.thePlayer;
		}
		
		introAnimation.setDirection(target == null ? Direction.BACKWARDS : Direction.FORWARDS);
		
		if(target != null) {
			name = target.getName();
			health = Math.min(target.getHealth(), 20);
			armor = Math.min(target.getTotalArmorValue(), 20);
			head = target.getLocationSkin();
		}
		
		if(name != null && head != null) {
			screenAnimation.wrap(() -> drawNanoVG(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), 2 - introAnimation.getValueFloat(), introAnimation.getValueFloat());
		}
	}
	
	private void drawNanoVG() {
		
		float nameWidth = this.getTextWidth(name, 10.2F, getHudFont(2));
		int width = 140;
		
		if(nameWidth + 48F > width) {
			width = (int) (width + nameWidth - 89);
		}
		
		healthAnimation.setAnimation(health, 16);
		armorAnimation.setAnimation(armor, 16);
		
		this.drawBackground(width, 46);
		this.drawPlayerHead(head, 5, 5, 36, 36, 6);
		this.drawText(name, 45.5F, 8F, 10.2F, getHudFont(2));
		
		this.drawText(LegacyIcon.HEART_FILL, 52, 26.5F, 9, Fonts.LEGACYICON);
		this.drawArc(56.5F, 30.5F, 9F, -90F, -90F + 360, 1.6F, this.getFontColor(120));
		this.drawArc(56.5F, 30.5F, 9F, -90F, -90F + (18 * healthAnimation.getValue()), 1.6F);
		
		this.drawText(LegacyIcon.SHIELD_FILL, 76F, 26.5F, 9, Fonts.LEGACYICON);
		this.drawArc(80.5F, 30.5F, 9F, -90F, -90F + 360, 1.6F, this.getFontColor(120));
		this.drawArc(80.5F, 30.5F, 9F, -90F, -90F + (18 * armorAnimation.getValue()), 1.6F);
		
		this.setWidth(width);
		this.setHeight(46);
	}
}
