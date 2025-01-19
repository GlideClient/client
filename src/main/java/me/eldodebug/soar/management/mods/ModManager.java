package me.eldodebug.soar.management.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.LogManager;

import me.eldodebug.soar.management.mods.impl.*;
import me.eldodebug.soar.management.mods.settings.Setting;
import me.eldodebug.soar.utils.Sound;

public class ModManager {

	private ArrayList<Mod> mods = new ArrayList<Mod>();
	private ArrayList<Setting> settings = new ArrayList<Setting>();
	public boolean canPlaySound = false;
	
	public void init() {
		mods.add(new AnimationsMod());
		mods.add(new AppleSkinMod());
		mods.add(new ArmorStatusMod());
		mods.add(new ArrayListMod());
		mods.add(new AsyncScreenshotMod());
		mods.add(new AutoTextMod());
		mods.add(new BlockInfoMod());
		mods.add(new BlockOverlayMod());
		mods.add(new BloodParticlesMod());
		mods.add(new BorderlessFullscreenMod());
		mods.add(new BossHealthMod());
		mods.add(new BowZoomMod());
		mods.add(new BreadcrumbsMod());
		mods.add(new CalendarMod());
		mods.add(new ChatMod());
		mods.add(new ChatTranslateMod());
		mods.add(new ChunkAnimatorMod());
		mods.add(new ChunkBordersMod());
		mods.add(new ClearGlassMod());
		mods.add(new ClearWaterMod());
		mods.add(new ClientSpooferMod());
		mods.add(new ClockMod());
		mods.add(new ColorSaturationMod());
		mods.add(new ComboCounterMod());
		mods.add(new CompassMod());
		mods.add(new CoordsMod());
		mods.add(new CPSDisplayMod());
		mods.add(new CrosshairMod());
		mods.add(new CustomHeldItemsMod());
		mods.add(new DamageParticlesMod());
		mods.add(new DamageTiltMod());
		mods.add(new DamageTintMod());
		mods.add(new DayCounterMod());
		mods.add(new DiscordRPCMod());
		mods.add(new EarsMod());
		mods.add(new EntityCullingMod());
		mods.add(new FarCameraMod());
		mods.add(new FemaleGenderMod());
		mods.add(new FovModifierMod());
		mods.add(new FPSBoostMod());
		mods.add(new FPSDisplayMod());
		mods.add(new FPSLimiterMod());
		mods.add(new FPSSpooferMod());
		mods.add(new FreelookMod());
		mods.add(new FullbrightMod());
		mods.add(new GameModeDisplayMod());
		mods.add(new GlintColorMod());
		mods.add(new InternalSettingsMod());
		mods.add(new GodbridgeAssistMod());
		mods.add(new HealthDisplayMod());
		mods.add(new HitBoxMod());
		mods.add(new HitColorMod());
		mods.add(new HitDelayFixMod());
		mods.add(new HorseStatsMod());
		mods.add(new HypixelMod());
		mods.add(new HypixelQuickPlayMod());
		mods.add(new ImageDisplayMod());
		mods.add(new InventoryDisplayMod());
		mods.add(new InventoryMod());
		mods.add(new ItemInfoMod());
		mods.add(new ItemPhysicsMod());
		mods.add(new Items2DMod());
		mods.add(new JumpCircleMod());
		mods.add(new KeystrokesMod());
		mods.add(new KillEffectsMod());
		mods.add(new KillSoundsMod());
		mods.add(new MechvibesMod());
		mods.add(new MemoryUsageMod());
		mods.add(new MinemenMod());
		mods.add(new MinimalDamageShakeMod());
		mods.add(new MinimalViewBobbingMod());
		mods.add(new MinimapMod());
		mods.add(new MoBendsMod());
		mods.add(new ModernHotbarMod());
		mods.add(new MotionBlurMod());
		mods.add(new MouseStrokesMod());
		mods.add(new MusicInfoMod());
		mods.add(new NameDisplayMod());
		mods.add(new NameProtectMod());
		mods.add(new NametagMod());
		mods.add(new OverlayEditorMod());
		mods.add(new PackDisplayMod());
		mods.add(new ParticleCustomizerMod());
		mods.add(new PingDisplayMod());
		mods.add(new PlayerCounterMod());
		mods.add(new PlayerDisplayMod());
		mods.add(new PlayerListMod());
		mods.add(new PlayerPredicatorMod());
		mods.add(new PlayTimeDisplayMod());
		mods.add(new PotionCounterMod());
		mods.add(new PotionStatusMod());
		mods.add(new ProjectileTrailMod());
		mods.add(new QuickSwitchMod());
		mods.add(new RawInputMod());
		mods.add(new ReachCirclesMod());
		mods.add(new ReachDisplayMod());
		mods.add(new RearviewMod());
		mods.add(new ScoreboardMod());
		mods.add(new ServerIPDisplayMod());
		mods.add(new SessionInfoMod());
		mods.add(new ShinyPotsMod());
		mods.add(new Skin3DMod());
		mods.add(new SkinProtectMod());
		mods.add(new SlowSwingMod());
		mods.add(new SoundModifierMod());
		mods.add(new SoundSubtitlesMod());
		mods.add(new SpeedometerMod());
		mods.add(new StopwatchMod());
		mods.add(new TabEditorMod());
		mods.add(new TaplookMod());
		mods.add(new TargetIndicatorMod());
		mods.add(new TargetInfoMod());
		mods.add(new TimeChangerMod());
		mods.add(new TNTTimerMod());
		mods.add(new ToggleSneakMod());
		mods.add(new ToggleSprintMod());
		mods.add(new UHCOverlayMod());
		mods.add(new WaveyCapesMod());
		mods.add(new WaypointMod());
		mods.add(new WeatherChangerMod());
		mods.add(new WeatherDisplayMod());
		mods.add(new ZoomMod());
	}
	
	public ArrayList<Mod> getMods() {
		return mods;
	}
	
	public Mod getModByTranslateKey(String key) {
		
		for(Mod m : mods) {
			if(m.getNameKey().equals(key)) {
				return m;
			}
		}
		
		return null;
	}
	
	public ArrayList<HUDMod> getHudMods(){
		
		ArrayList<HUDMod> result = new ArrayList<HUDMod>();
		
		for(Mod m : mods) {
			if(m instanceof HUDMod && ((HUDMod) m).isDraggable()) {
				result.add((HUDMod) m);
			}
		}
		
		return result;
	}

	public ArrayList<Setting> getSettings() {
		return settings;
	}
	
	public ArrayList<Setting> getSettingsByMod(Mod m){
		
		ArrayList<Setting> result = new ArrayList<Setting>();
		
		for(Setting s : settings) {
			if(s.getParent().equals(m)) {
				result.add(s);
			}
		}
		
		if(result.isEmpty()) {
			return null;
		}
		
		return result;
	}
	
	public String getWords(Mod mod) {
		
		String result = "";
		
		for(Mod m : mods) {
			if(m.equals(mod)) {
				result = result + m.getName() + " ";
			}
		}
		
		for(Setting s : settings) {
			if(s.getParent().equals(mod)) {
				result = result + s.getName() + " ";
			}
		}

		for(Mod m : mods) {
			if(m.equals(mod) && !Objects.equals(m.getAlias(), "\u200B")) {
				result = result + m.getAlias() + " ";
			}
		}
		
		return result;
	}
	
	public void addSettings(Setting... settingsList) {
		settings.addAll(Arrays.asList(settingsList));
	}
	
	public void disableAll() {
		canPlaySound = false;
		for(Mod m : mods) {
			m.setToggled(false);
		}
		InternalSettingsMod.getInstance().setToggled(true);
		canPlaySound = true;
	}

	public void playToggleSound(boolean toggled, Mod m){
		if (!canPlaySound) return;
		if(toggled){
			Sound.play("soar/audio/positive.wav", true);
		} else {
			Sound.play("soar/audio/negative.wav", true);
		}

	}

}

