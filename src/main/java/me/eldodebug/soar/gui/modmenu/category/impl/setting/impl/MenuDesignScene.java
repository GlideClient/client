package me.eldodebug.soar.gui.modmenu.category.impl.setting.impl;

import me.eldodebug.soar.gui.modmenu.category.impl.SettingCategory;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.SettingScene;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.font.Icons;

public class MenuDesignScene extends SettingScene {

    public MenuDesignScene(SettingCategory parent, TranslateText nameTranslate, TranslateText descriptionTranslate, String icon, boolean show) {
        super(parent, TranslateText.NONE, TranslateText.NONE, Icons.GLIDE, false);
    }


    //todo: add main menu and hud customisation

}
