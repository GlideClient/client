package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.SimpleHUDMod;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.PlayerUtils;
import net.minecraft.potion.Potion;

public class PotionCounterMod extends SimpleHUDMod {

	private BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);
	
	public PotionCounterMod() {
		super(TranslateText.POTION_COUNTER, TranslateText.POTION_COUNTER_DESCRIPTION);
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		this.draw();
	}
	
	@Override
	public String getText() {
		
		int amount = PlayerUtils.getPotionsFromInventory(Potion.heal);
		
		return amount + " " + (amount <= 1 ? "pot" : "pots");
	}
	
	@Override
	public String getIcon() {
		return iconSetting.isToggled() ? LegacyIcon.ARCHIVE : null;
	}
}
