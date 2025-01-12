package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.SimpleHUDMod;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

public class DayCounterMod extends SimpleHUDMod {

	private BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);
	
	public DayCounterMod() {
		super(TranslateText.DAY_COUNTER, TranslateText.DAY_COUNTER_DESCRIPTION);
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		this.draw();
	}
	
	@Override
	public String getText() {
		
		long time = mc.theWorld.getWorldInfo().getWorldTotalTime() / 24000L;
		
		return time + " Day" + (time != 1L ? "s" :"");
	}
	
	@Override
	public String getIcon() {
		return iconSetting.isToggled() ? LegacyIcon.SUNRISE : null;
	}
}
