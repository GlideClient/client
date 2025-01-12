package me.eldodebug.soar.management.remote.changelog;

import java.awt.Color;

import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

public enum ChangelogType {
	ADDED(0, LegacyIcon.PLUS, new Color(0, 142, 65)),
	FIXED(1, LegacyIcon.REFRESH, new Color(207, 112, 3)),
	REMOVED(2, LegacyIcon.MINUS, new Color(209, 34, 34)),
	ERROR(999, LegacyIcon.PROHIBITED, new Color(143, 0, 0));
	
	private int id;
	private String string;
	private Color color;
	
	private ChangelogType(int id, String string, Color color) {
		this.id = id;
		this.string = string;
		this.color = color;
	}
	
	public int getId() {
		return id;
	}

	public String getText() {
		return string;
	}

	public Color getColor() {
		return color;
	}
	
	public static ChangelogType getTypeById(int id) {
		
		for(ChangelogType type : ChangelogType.values()) {
			if(type.getId() == id) {
				return type;
			}
		}
		
		return ChangelogType.ERROR;
	}
}
