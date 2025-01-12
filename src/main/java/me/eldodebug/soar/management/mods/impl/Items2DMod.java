package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.notification.Notification;
import me.eldodebug.soar.management.notification.NotificationType;

public class Items2DMod extends Mod {

	private static Items2DMod instance;
	
	public Items2DMod() {
		super(TranslateText.ITEMS_2D, TranslateText.ITEMS_2D_DESCRIPTION, ModCategory.RENDER);
		
		instance = this;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		
		if(ItemPhysicsMod.getInstance().isToggled()) {
			ItemPhysicsMod.getInstance().setToggled(false);
			Glide.getInstance().getNotificationManager().post(TranslateText.ITEM_PHYSICS.getText(),  "Disabled due to incompatibility" , NotificationType.WARNING);
		}
	}
	
	public static Items2DMod getInstance() {
		return instance;
	}
}
