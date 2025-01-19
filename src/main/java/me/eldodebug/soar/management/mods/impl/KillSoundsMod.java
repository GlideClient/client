package me.eldodebug.soar.management.mods.impl;

import java.io.File;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventMotionUpdate;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.event.impl.EventUpdate;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.mods.settings.impl.SoundSetting;
import me.eldodebug.soar.utils.Sound;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class KillSoundsMod extends Mod {

	private EntityLivingBase target;
	
	private File prevCustomSound;
	
	private Sound oofSound = new Sound();
	private Sound customSound = new Sound();
	
	private NumberSetting volumeSetting = new NumberSetting(TranslateText.VOLUME, this, 0.5, 0.0, 1.0, false);
	private BooleanSetting customSoundSetting = new BooleanSetting(TranslateText.CUSTOM_SOUND, this, false);
	private SoundSetting soundSetting = new SoundSetting(TranslateText.SOUND, this);
	
	public KillSoundsMod() {
		super(TranslateText.KILL_SOUNDS, TranslateText.KILL_SOUNDS_DESCRIPTION, ModCategory.OTHER);
	}

	@EventTarget
	public void onTick(EventTick event) {
		
		if(customSoundSetting.isToggled()) {
			
			if(soundSetting.getSound() != null) {
				
				if(prevCustomSound != soundSetting.getSound()) {
					
					prevCustomSound = soundSetting.getSound();
					
					try {
						customSound.loadClip(soundSetting.getSound());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				customSound.setVolume(volumeSetting.getValueFloat());
			}
		} else {
			oofSound.setVolume(volumeSetting.getValueFloat());
		}
	}
	
	@EventTarget
	public void onUpdate(EventUpdate event) {
		if(mc.objectMouseOver != null & mc.objectMouseOver.entityHit != null) {
			if(mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
				target = (EntityLivingBase) mc.objectMouseOver.entityHit;
			}
		}
	}
	
	@EventTarget
	public void onPreMotionUpdate(EventMotionUpdate event) {
		
		if (target != null && !mc.theWorld.loadedEntityList.contains(target) && mc.thePlayer.getDistanceSq(target.posX, mc.thePlayer.posY, target.posZ) < 100) {
			
			if (mc.thePlayer.ticksExisted > 3) {
				
				if(customSoundSetting.isToggled()) {
					customSound.play();
				} else {
					oofSound.play();
				}
			}
			
			target = null;
		}
	}
	
	@Override
	public void onEnable() {
		super.onEnable();
		try {
			oofSound.loadClip(new ResourceLocation("soar/audio/oof.wav"));
		} catch (Exception e) {}
	}
}
