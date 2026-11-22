package me.eldodebug.soar.management.mods;

public abstract class HudTheme {

    String themeName;

    /**
     * @param themeName : the theme name for the hud theme
     */
    HudTheme(String themeName){
        this.themeName = themeName;
    }

    public String getThemeName() {
        return themeName;
    }
}
