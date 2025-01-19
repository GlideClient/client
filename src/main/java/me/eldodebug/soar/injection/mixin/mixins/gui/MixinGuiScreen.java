package me.eldodebug.soar.injection.mixin.mixins.gui;

import java.io.IOException;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.utils.Sound;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {

	@Shadow
    protected Minecraft mc;
    
	@Shadow
    public abstract void keyTyped(char typedChar, int keyCode);
    
	@Inject(method = "drawScreen", at = @At("TAIL"))
    public void postDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		if(InternalSettingsMod.getInstance().getClickEffectsSetting().isToggled()) {
			Glide.getInstance().getClickEffects().drawClickEffects();
		}
	}
	
	@Inject(method = "mouseClicked", at = @At("HEAD"))
	public void preMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
		if(InternalSettingsMod.getInstance().getClickEffectsSetting().isToggled()) {
			Glide.getInstance().getClickEffects().addClickEffect(mouseX, mouseY);
		}
		Sound.play("soar/audio/click.wav", true);
	}
	
	@Overwrite
    public void handleKeyboardInput() throws IOException {
        char c = Keyboard.getEventCharacter();
        
        if ((Keyboard.getEventKey() == 0 && c >= ' ') || Keyboard.getEventKeyState()) {
            this.keyTyped(c, Keyboard.getEventKey());
        }
        
        mc.dispatchKeypresses();
    }
}
