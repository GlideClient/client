package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRendererLivingEntity;
import me.eldodebug.soar.management.event.impl.EventUpdate;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.utils.ServerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;

public class FPSBoostMod extends Mod {

	private static FPSBoostMod instance;

	private BooleanSetting chunkDelaySetting = new BooleanSetting(TranslateText.CHUNK_DELAY, this, false);
	private NumberSetting delaySetting = new NumberSetting(TranslateText.DELAY, this, 5, 1, 12, true);
	private BooleanSetting removeBotSetting = new BooleanSetting(TranslateText.REMOVE_BOT, this, false);
	
	public FPSBoostMod() {
		super(TranslateText.FPS_BOOST, TranslateText.FPS_BOOST_DESCRIPTION, ModCategory.OTHER);
		
		instance = this;
	}
	
	@EventTarget
	public void onUpdate(EventUpdate event) {
		
		if(removeBotSetting.isToggled()) {
			for(Entity entity : mc.theWorld.loadedEntityList) {
				if(entity.isInvisible() && !ServerUtils.isInTablist(entity)) {
					mc.theWorld.removeEntity(entity);
				}
			}
		}
	}
    
	public static FPSBoostMod getInstance() {
		return instance;
	}

	public BooleanSetting getChunkDelaySetting() {
		return chunkDelaySetting;
	}

	public NumberSetting getDelaySetting() {
		return delaySetting;
	}
}
