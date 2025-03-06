package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.SimpleHUDMod;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;

public class MemoryUsageMod extends SimpleHUDMod {

	private SimpleAnimation animation = new SimpleAnimation();
	
	private ComboSetting designSetting = new ComboSetting(TranslateText.DESIGN, this, TranslateText.SIMPLE, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.SIMPLE), new Option(TranslateText.FANCY))));
	private BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);
	
	public MemoryUsageMod() {
		super(TranslateText.MEMORY_USAGE, TranslateText.MEMORY_USAGE_DESCRIPTION);
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		
		Option design = designSetting.getOption();
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		
		if(design.getTranslate().equals(TranslateText.SIMPLE)) {
			this.draw();
		}else {
			nvg.setupAndDraw(() -> drawNanoVG(nvg));
		}
	}
	
	private void drawNanoVG(NanoVGManager nvg) {
		
		animation.setAnimation(((this.getUsingMemory() / 100F) * 360), 16);
		
		this.drawBackground(54, 60);
		this.drawCenteredText("Memory", 54 / 2, 6, 9, getHudFont(1));
		this.drawCenteredText(this.getUsingMemory() + "%", 54 / 2, 32, 9, getHudFont(1));

		this.drawArc(27, 35.5F, 16.5F, -90, 360, 1.6F, this.getFontColor(120));
		this.drawArc(27, 35.5F, 16.5F, -90, animation.getValue() - 90, 1.6F, this.getFontColor());
		
		this.setWidth(54);
		this.setHeight(60);
	}
	
	@Override
	public String getText() {
		
		String mem = "Mem: " + getUsingMemory() + "%";
		
		return mem;
	}
	
	@Override
	public String getIcon() {
		return iconSetting.isToggled() ? LegacyIcon.SERVER : null;
	}
	
	private long getUsingMemory() {
		
		Runtime runtime = Runtime.getRuntime();
		
		return (runtime.totalMemory() - runtime.freeMemory()) * 100L / runtime.maxMemory();
	}
}
