package me.eldodebug.soar.management.notification;

import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

public enum NotificationType {
	INFO(LegacyIcon.INFO),
	WARNING(LegacyIcon.ALERT_TRIANGLE),
	ERROR(LegacyIcon.X_CIRCLE),
	SUCCESS(LegacyIcon.CHECK),
	MUSIC(LegacyIcon.MUSIC);
	
	private String icon;
	
	private NotificationType(String icon) {
		this.icon = icon;
	}

	public String getIcon() {
		return icon;
	}
}
