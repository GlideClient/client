package me.eldodebug.soar.injection.mixin.mixins.render;

import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.injection.interfaces.IMixinRenderManager;
import me.eldodebug.soar.management.event.impl.EventRenderHitbox;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;

@Mixin(RenderManager.class)
public abstract class MixinRenderManager implements IMixinRenderManager {

	@Shadow
	public TextureManager renderEngine;

	@Shadow
	public abstract <T extends Entity> Render<T> getEntityRenderObject(Entity entityIn);
	
	@Shadow
    private double renderPosX;
	
	@Shadow
    private double renderPosY;
	
	@Shadow
    private double renderPosZ;

	@Shadow private RenderPlayer playerRenderer;

	@Inject(method = "renderDebugBoundingBox", at = @At("HEAD"), cancellable = true)
	public void onRenderHitbox(Entity entityIn, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
		
		EventRenderHitbox event = new EventRenderHitbox(entityIn, x, y, z, entityYaw, partialTicks);
		event.call();
		
		if(event.isCancelled()) {
			ci.cancel();
		}
	}
	
	@Override
	public double getRenderPosX() {
		return this.renderPosX;
	}

	@Override
	public double getRenderPosY() {
		return this.renderPosY;
	}

	@Override
	public double getRenderPosZ() {
		return this.renderPosZ;
	}

	@Override
	public RenderPlayer getPlayerRenderer() {
		return this.playerRenderer;
	}
}
