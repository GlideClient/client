package me.eldodebug.soar.management.mods.impl;

import java.util.Calendar;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;

public class CalendarMod extends HUDMod {

	private int height;
	
	public CalendarMod() {
		super(TranslateText.CALENDAR, TranslateText.CALENDAR_DESCRIPTION);
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		
		nvg.setupAndDraw(() -> drawNanoVG());
	}
	
	private void drawNanoVG() {
		
		Calendar calendar = Calendar.getInstance();
		
		AccentColor currentColor = Glide.getInstance().getColorManager().getCurrentColor();
		
		String[] dayOfWeek = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
		float offsetX = 0;
		float offsetY = 0;
		int index = 1;
		int weekIndex = 0;
		
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		Calendar firstDayCalendar = (Calendar) calendar.clone();
		
		firstDayCalendar.set(year, month, 1);
		
		this.drawBackground(100, height);
		this.drawText(getMonthByNumber(month) + " " + year, 6, 6, 11, getHudFont(2));
		
		for(String s : dayOfWeek) {
			
			this.drawText(s, 6 + offsetX, 22, 6.5F, getHudFont(2));
			
			offsetX+=13.4;
		}
		
		offsetX = 0;
		index = firstDayCalendar.get(Calendar.DAY_OF_WEEK);
		offsetX = (index - 1) * 13.4F;
		
		for(int i = 1; i <= maxDay; i++) {
			
			if(i == day) {
				this.drawRoundedRect(4.5F + offsetX, 30F + offsetY, 10.5F, 10.5F, 10.5F / 2);
			}
			
			this.drawCenteredText(String.valueOf(i), 10 + offsetX, 33 + offsetY, 6, getHudFont(1), i == day ? currentColor.getInterpolateColor() : this.getFontColor());
			
			offsetX+=13.4;
			
			if(index % 7 == 0 && i != maxDay) {
				offsetY+=13.4F;
				offsetX = 0;
				weekIndex++;
			}
			
			index++;
		}
		
		height = weekIndex < 5 ? 97 : 110;
		
		this.setWidth(100);
		this.setHeight(weekIndex < 5 ? 97 : 110);
	}
	
	private String getMonthByNumber(int month) {
		
		switch(month) {
			case 0:
				return "January";
			case 1:
				return "February";
			case 2:
				return "March";
			case 3:
				return "April";
			case 4:
				return "May";
			case 5:
				return "June";
			case 6:
				return "July";
			case 7:
				return "August";
			case 8:
				return "September";
			case 9:
				return "October";
			case 10:
				return "November";
			case 11:
				return "December";
		}
		
		return "null";
	}
}
