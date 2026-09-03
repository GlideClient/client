package me.eldodebug.soar.injection.mixin.mixins.audio;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.management.mods.impl.SoundSubtitlesMod;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import paulscode.sound.SoundSystem;

@Mixin(SoundManager.class)
public abstract class MixinSoundManager {

    @Shadow
    public abstract boolean isSoundPlaying(ISound sound);

    @Shadow
    @Final
    private Map<String, ISound> playingSounds;

    @Shadow
    private boolean loaded;

    private final Set<String> pausedSounds = new HashSet<>();

    @Redirect(method = "pauseAllSounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundManager$SoundSystemStarterThread;pause(Ljava/lang/String;)V", remap = false))
    private void onlyPauseSoundIfNecessary(@Coerce SoundSystem soundSystem, String sound) {
        if (this.isSoundPlaying(this.playingSounds.get(sound))) {
            soundSystem.pause(sound);
            this.pausedSounds.add(sound);
        }
    }

    @Redirect(method = "resumeAllSounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundManager$SoundSystemStarterThread;play(Ljava/lang/String;)V", remap = false))
    private void onlyResumePausedSounds(@Coerce SoundSystem soundSystem, String sound) {
        if (this.pausedSounds.contains(sound)) {
            soundSystem.play(sound);
        }
    }

    @Inject(method = "playSound", at = @At("HEAD"))
    public void prePlaySound(ISound p_sound, CallbackInfo ci) {
        if (this.loaded) {
            SoundSubtitlesMod.getInstance().soundPlay(p_sound);
        }
    }

    @Inject(method = "resumeAllSounds", at = @At("TAIL"))
    private void clearPausedSounds(CallbackInfo ci) {
        this.pausedSounds.clear();
    }
}