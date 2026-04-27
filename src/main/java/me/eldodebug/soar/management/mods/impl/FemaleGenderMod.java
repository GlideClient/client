package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.discord.DiscordRPC;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;

public class FemaleGenderMod extends Mod {

	private static FemaleGenderMod instance;

	public FemaleGenderMod() {
		super(TranslateText.FEMALE_GENDER, TranslateText.FEMALE_GENDER_DESCRIPTION, ModCategory.PLAYER, "boobs");
		// invalid
		instance = this;
	}

	public static FemaleGenderMod getInstance() {
		return instance;
	}
}
