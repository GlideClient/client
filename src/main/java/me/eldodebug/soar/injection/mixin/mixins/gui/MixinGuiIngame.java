package me.eldodebug.soar.injection.mixin.mixins.gui;

import eu.shoroa.contrib.render.ShBlur;
import me.eldodebug.soar.Glide;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.gui.GuiEditHUD;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.injection.interfaces.IMixinGuiIngame;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.event.impl.EventRenderCrosshair;
import me.eldodebug.soar.management.event.impl.EventRenderDamageTint;
import me.eldodebug.soar.management.event.impl.EventRenderExpBar;
import me.eldodebug.soar.management.event.impl.EventRenderNotification;
import me.eldodebug.soar.management.event.impl.EventRenderPlayerStats;
import me.eldodebug.soar.management.event.impl.EventRenderPumpkinOverlay;
import me.eldodebug.soar.management.event.impl.EventRenderScoreboard;
import me.eldodebug.soar.management.event.impl.EventRenderSelectedItem;
import me.eldodebug.soar.management.event.impl.EventRenderTooltip;
import me.eldodebug.soar.management.event.impl.EventRenderVisualizer;
import me.eldodebug.soar.management.mods.impl.AnimationsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.ResourceLocation;

@Mixin(GuiIngame.class)
public abstract class MixinGuiIngame implements IMixinGuiIngame {

	@Shadow
	public abstract boolean showCrosshair();
	
	@Final
	@Shadow
	private Minecraft mc;
	
	@Shadow
    private int updateCounter;
	
	@Shadow
    private int remainingHighlightTicks;
	
	@Shadow
    private ItemStack highlightingItemStack;
	
    public int prevAmount;
    
	@Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;showCrosshair()Z"))
	public boolean preRenderCrosshair(GuiIngame guiIngame) {
		EventRenderCrosshair event = new EventRenderCrosshair();
		event.call();
		boolean result = !event.isCancelled() && showCrosshair();
		mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/icons.png"));
		return result;
	}
	
	@Inject(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;enableBlend()V", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
	public void preRenderGameOverlay(float partialTicks, CallbackInfo callback) {
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    	new EventRenderVisualizer(partialTicks).call();
		GlStateManager.enableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Inject(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", shift = At.Shift.BEFORE, ordinal = 2))
    public void postRenderGameOverlay(float partialTicks, CallbackInfo ci) {
		ShBlur.getInstance().render();

		new EventRenderDamageTint(partialTicks).call();
		
		if(!(mc.currentScreen instanceof GuiEditHUD)) {
			new EventRender2D(partialTicks).call();
			
			if(!(mc.currentScreen instanceof GuiModMenu)) {
				new EventRenderNotification().call();
			}
		}
	}
	
    @Redirect(method = "renderPlayerStats", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V"))
    public void drawTexturedModalRect(GuiIngame gui, int x, int y, int textureX, int textureY, int width, int height) {
    	
    	AnimationsMod mod = AnimationsMod.getInstance();
    	
    	if(mod.isToggled() && mod.getHealthSetting().isToggled()) {
        	if(textureX != prevAmount + 54) {
            	gui.drawTexturedModalRect(x, y, textureX, textureY, width, height);
        	}
    	}else {
        	gui.drawTexturedModalRect(x, y, textureX, textureY, width, height);
    	}
    }
    
	@Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
	public void preRenderBossHealth(CallbackInfo ci) {
    	mc.getTextureManager().bindTexture(Gui.icons);
		ci.cancel();
	}
	
    @Redirect(method = "renderPlayerStats", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V"))
    public void endStartSection(Profiler p, String s) {
    	
        p.endStartSection(s);
        
        if(s.equals("health")) {
        	
            EntityPlayer entityplayer = (EntityPlayer) mc.getRenderViewEntity();        
            int i = 16;

            if (entityplayer.isPotionActive(Potion.poison)) {
                i += 36;
            } else if (entityplayer.isPotionActive(Potion.wither)) {
                i += 72;
            }
            
            prevAmount = i;
        }
    }
    
    @Inject(method = "renderPlayerStats", at = @At("TAIL"))
    public void postRenderPlayerStats(ScaledResolution scaledRes, CallbackInfo ci) {
    	
    	if(mc.getRenderViewEntity() instanceof EntityPlayer) {
    		
        	EntityPlayer entityplayer = (EntityPlayer) mc.getRenderViewEntity();
        	Entity entity = entityplayer.ridingEntity;
        	
        	if(entity == null) {
        		new EventRenderPlayerStats().call();
        	}
    	}
    }

    @Inject(method = "renderPumpkinOverlay", at = @At("HEAD"), cancellable = true)
    private void preRenderPumpkinOverlay(ScaledResolution scaledRes, CallbackInfo ci) {
    	
    	EventRenderPumpkinOverlay event = new EventRenderPumpkinOverlay();
    	event.call();
    	
    	if(event.isCancelled()) {
    		ci.cancel();
    	}
    }
    
	@Inject(method = "renderSelectedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;tryBlendFuncSeparate(IIII)V", shift = Shift.AFTER))
	public void renderSelectedItem(ScaledResolution p_181551_1_, CallbackInfo ci) {
		
		if (this.remainingHighlightTicks > 0 && this.highlightingItemStack != null) {
			int k = (int)((float)this.remainingHighlightTicks * 256.0F / 10.0F);
			
	        if (k > 255) {
	            k = 255;
	        }
	        
			EventRenderSelectedItem event = new EventRenderSelectedItem(16777215 + (k << 24));
			event.call();
		}
	}
    
	@Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    public void renderTooltip(ScaledResolution sr, float partialTicks, CallbackInfo ci) {
		
        EventRenderTooltip event = new EventRenderTooltip(partialTicks);
        event.call();
        
        if(event.isCancelled()) {
        	ci.cancel();
        }
    }
	
	@Inject(method = "renderExpBar", at = @At("HEAD"), cancellable = true)
    public void preRenderExpBar(ScaledResolution scaledRes, int x, CallbackInfo ci) {
		
    	EventRenderExpBar event = new EventRenderExpBar();
    	event.call();
    	
    	if(event.isCancelled()) {
    		ci.cancel();
    	}
    }
	
	@Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void preRenderScoreboard(ScoreObjective objective, ScaledResolution scaledRes, CallbackInfo ci) {
		
		EventRenderScoreboard event = new EventRenderScoreboard(objective);
		event.call();
		
		if(event.isCancelled()) {
			ci.cancel();
		}
	}
	
	@Override
	public int getUpdateCounter() {
		return updateCounter;
	}
}
