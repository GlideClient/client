package me.eldodebug.soar.management.mods.impl;

import eu.shoroa.contrib.cosmetic.CosmeticManager;
import eu.shoroa.contrib.impl.BoobsCosmetic;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;

public class FemaleGenderMod extends Mod {

    private static FemaleGenderMod instance;
    private final NumberSetting damping = new NumberSetting(TranslateText.DAMPING, this, 0.95, 0.1, 2.0, false);
    private final NumberSetting springStrength = new NumberSetting(TranslateText.SPRING_STRENGTH, this, 0.8, 0.1, 4.0, false);
    private final NumberSetting jiggleMultiplier = new NumberSetting(TranslateText.JIGGLE_MULTIPLY, this, 15, 1, 20, false);
    private final NumberSetting accelerationMultiplier = new NumberSetting(TranslateText.ACCELERATION_MULTIPLIER, this, 1.5, 0.1, 6.0, false);

    public FemaleGenderMod() {
        super(TranslateText.FEMALE_GENDER, TranslateText.FEMALE_GENDER_DESCRIPTION, ModCategory.PLAYER, "boobs");
        instance = this;
    }

    public static FemaleGenderMod getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        CosmeticManager.getInstance().getCosmeticByClass(BoobsCosmetic.class).setEnabled(true);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        CosmeticManager.getInstance().getCosmeticByClass(BoobsCosmetic.class).setEnabled(false);
    }

    public float getDamping() {
        return (float) damping.getValue();
    }

    public float getSpringStrength() {
        return (float) springStrength.getValue();
    }

    public float getJiggleMultiplier() {
        return (float) jiggleMultiplier.getValue();
    }

    public float getAccelerationMultiplier() {
        return (float) accelerationMultiplier.getValue();
    }
}
