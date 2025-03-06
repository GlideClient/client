package me.eldodebug.soar.management.mods.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

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

public class ClockMod extends SimpleHUDMod {

	private BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);
	
	private ComboSetting modeSetting = new ComboSetting(TranslateText.DESIGN, this, TranslateText.SIMPLE, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.SIMPLE), new Option(TranslateText.FANCY))));
	
	private DateFormat df = new SimpleDateFormat("HH:mm a", Locale.US);
	
	public ClockMod() {
		super(TranslateText.CLOCK, TranslateText.CLOCK_DESCRIPTION);
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		
		if(modeSetting.getOption().getTranslate().equals(TranslateText.SIMPLE)) {
			this.draw();
		}else {
			Glide.getInstance().getNanoVGManager().setupAndDraw(() -> drawNanoVG());
		}
	}
	
	@Override
	public String getText() {
		return df.format(Calendar.getInstance().getTime());
	}
	
	@Override
	public String getIcon() {
		return iconSetting.isToggled() ? LegacyIcon.CLOCK : null;
	}
	
	private void drawNanoVG() {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		
		float scale = this.getScale();
		int size = 128;
		float center = size / 2;
		float scaledSize = 128 * scale;
		float radius = center * this.getScale();
		String[] numbers = {"3", "6", "9", "12"};
		float lineLength = 4 * scale;
		int index = 0;
		Calendar c = Calendar.getInstance();
		
		this.drawBackground(size, size, radius);
		
		nvg.drawCircle(this.getX() + (scaledSize / 2), this.getY() + (scaledSize / 2), 2 * scale, this.getFontColor());
		
		for(int i = 0; i < 4; i++) {
			
			float angle = (float) Math.toRadians(360.0 / 4.0 * i);
			
		    float textX = center + ((radius / scale) - 6F) * (float) Math.cos(angle);
		    float textY = center + ((radius / scale) - 6F) * (float) Math.sin(angle);
		    
			this.drawCenteredText(numbers[i], textX + 0.5F, textY - 3, 8, getHudFont(2));
		}
		
	    NVGColor nvgColor = nvg.getColor(this.getFontColor());
	    NVGColor nvgColor2 = nvg.getColor(this.getFontColor(180));
	    
		for(int i = 0; i < 12; i++) {
			
			if(i == index * 3) {
				index++;
				continue;
			}
			
			float angle = (float) Math.toRadians(360.0 / 12.0 * i);
			
		    float startX = (center * scale) + (radius - lineLength - 4) * (float) Math.cos(angle);
		    float startY = (center * scale) + (radius - lineLength - 4) * (float) Math.sin(angle);
		    float endX = (center * scale) + (radius - 4) * (float) Math.cos(angle);
		    float endY = (center * scale) + (radius - 4) * (float) Math.sin(angle);
		    
		    NanoVG.nvgBeginPath(nvg.getContext());
		    NanoVG.nvgMoveTo(nvg.getContext(), this.getX() + startX, this.getY() + startY);
		    NanoVG.nvgLineTo(nvg.getContext(), this.getX() + endX, this.getY() + endY);		     
		    NanoVG.nvgStrokeColor(nvg.getContext(), nvgColor);
		    NanoVG.nvgStroke(nvg.getContext());
		}
		
		index = 0;
		lineLength = 2 * scale;
		
		for(int i = 0; i < 60; i++) {
			
			if(i == index * 5) {
				index++;
				continue;
			}
			
			float angle = (float) Math.toRadians(360.0 / 60.0 * i);
			
		    float startX = (center * scale) + (radius - lineLength - 6) * (float) Math.cos(angle);
		    float startY = (center * scale) + (radius - lineLength - 6) * (float) Math.sin(angle);
		    float endX = (center * scale) + (radius - 6) * (float) Math.cos(angle);
		    float endY = (center * scale) + (radius - 6) * (float) Math.sin(angle);
		    
		    NanoVG.nvgBeginPath(nvg.getContext());
		    NanoVG.nvgMoveTo(nvg.getContext(), this.getX() + startX, this.getY() + startY);
		    NanoVG.nvgLineTo(nvg.getContext(), this.getX() + endX, this.getY() + endY);		     
		    NanoVG.nvgStrokeColor(nvg.getContext(), nvgColor2);
		    NanoVG.nvgStrokeWidth(nvg.getContext(), 0.5F);
		    NanoVG.nvgStroke(nvg.getContext());
		}
		
		float secondAngle = (float) Math.toRadians(360.0 / 60.0 * c.get(Calendar.SECOND)) - (float) Math.toRadians(90);
		
	    float secondX = (center * scale) + (radius - (14F * scale)) * (float) Math.cos(secondAngle);
	    float secondY = (center * scale) + (radius - (14F * scale)) * (float) Math.sin(secondAngle);
	    
		float minuteAngle = (float) Math.toRadians(360.0 / 60.0 * c.get(Calendar.MINUTE));
		float hourAngle = (float) Math.toRadians(360.0 / 12.0 * (c.get(Calendar.HOUR) + c.get(Calendar.MINUTE) / 60.0));
		
	    nvg.drawCircle(this.getX() + secondX, this.getY() + secondY, 1.3F * scale, this.getFontColor());
	    
	    nvg.save();
	    nvg.rotate(this.getX(), this.getY(), scaledSize, scaledSize, minuteAngle - (float) Math.toRadians(90));
	    nvg.drawRoundedRect(this.getX() + (scaledSize / 2) - (6 * scale), this.getY() + (scaledSize / 2) - scale, 48 * scale, 2 * scale, scale, this.getFontColor());
	    nvg.restore();
	    
	    nvg.save();
	    nvg.rotate(this.getX(), this.getY(), scaledSize, scaledSize, hourAngle - (float) Math.toRadians(90));
	    nvg.drawRoundedRect(this.getX() + (scaledSize / 2) - (6 * scale), this.getY() + (scaledSize / 2) - scale, 38 * scale, 2 * scale, scale, this.getFontColor());
	    nvg.restore();
	    
		this.setWidth(size);
		this.setHeight(size);
	}
}