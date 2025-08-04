package me.eldodebug.soar.injection.mixin.mixins.entity;

import eu.shoroa.contrib.cosmetic.CosmeticLayer;
import eu.shoroa.contrib.cosmetic.CosmeticManager;
import me.eldodebug.soar.injection.interfaces.IMixinEntityPlayer;
import me.eldodebug.soar.injection.interfaces.IMixinRenderPlayer;
import me.eldodebug.soar.management.event.impl.EventRenderPlayer;
import me.eldodebug.soar.management.mods.impl.Skin3DMod;
import me.eldodebug.soar.management.mods.impl.skin3d.layers.BodyLayerFeatureRenderer;
import me.eldodebug.soar.management.mods.impl.skin3d.layers.HeadLayerFeatureRenderer;
import me.eldodebug.soar.management.mods.impl.waveycapes.layers.CustomCapeRenderLayer;
import me.eldodebug.soar.utils.SkinUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer extends RendererLivingEntity<AbstractClientPlayer> implements IMixinRenderPlayer {

    @Shadow
    private boolean smallArms;

    private HeadLayerFeatureRenderer headLayer;
    private BodyLayerFeatureRenderer bodyLayer;
    
    public MixinRenderPlayer(RenderManager p_i46156_1_, ModelBase p_i46156_2_, float p_i46156_3_) {
        super(p_i46156_1_, p_i46156_2_, p_i46156_3_);
    }
    
    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onCreate(CallbackInfo info) {
        headLayer = new HeadLayerFeatureRenderer((RenderPlayer)(Object)this);
        bodyLayer = new BodyLayerFeatureRenderer((RenderPlayer)(Object)this);
        addLayer(new CustomCapeRenderLayer((RenderPlayer)(Object)this, getMainModel()));
        CosmeticManager.getInstance().setRenderPlayer((RenderPlayer)(Object)this);
        addLayer(new CosmeticLayer());
//        Glide.getInstance().getCosmeticManager().addLayersAndInit((RenderPlayer)(Object)this);
//        Glide.getInstance().getCosmeticManager().getCosmetics().forEach((c) -> addLayer(c.layer));
    }
    
    @Inject(method = "setModelVisibilities", at = @At("HEAD"))
    private void setModelProperties(AbstractClientPlayer abstractClientPlayer, CallbackInfo info) {
    	
    	if(Skin3DMod.getInstance().isToggled()) {
    		
            ModelPlayer playerModel = (ModelPlayer) getMainModel();
            
            if(Minecraft.getMinecraft().thePlayer.getPositionVector().squareDistanceTo(abstractClientPlayer.getPositionVector()) < Skin3DMod.getInstance().getRenderDistanceLOD() * Skin3DMod.getInstance().getRenderDistanceLOD()) {
                playerModel.bipedHeadwear.isHidden = playerModel.bipedHeadwear.isHidden;
                playerModel.bipedBodyWear.isHidden = playerModel.bipedBodyWear.isHidden;
                playerModel.bipedLeftArmwear.isHidden = playerModel.bipedLeftArmwear.isHidden;
                playerModel.bipedRightArmwear.isHidden = playerModel.bipedRightArmwear.isHidden;
                playerModel.bipedLeftLegwear.isHidden = playerModel.bipedLeftLegwear.isHidden;
                playerModel.bipedRightLegwear.isHidden = playerModel.bipedRightLegwear.isHidden;
            } else {
                if(!abstractClientPlayer.isSpectator()) {
                    playerModel.bipedHeadwear.isHidden = false;
                    playerModel.bipedBodyWear.isHidden = false;
                    playerModel.bipedLeftArmwear.isHidden = false;
                    playerModel.bipedRightArmwear.isHidden = false;
                    playerModel.bipedLeftLegwear.isHidden = false;
                    playerModel.bipedRightLegwear.isHidden = false;
                }
            }
    	}
    }
    
	@Inject(method = "doRender", at = @At("HEAD"), cancellable = true)
    public void preDoRender(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
		
		EventRenderPlayer event = new EventRenderPlayer(entity, x, y, z, partialTicks);
		event.call();
		
		if(event.isCancelled()) {
			ci.cancel();
		}
	}
	
    @Inject(method = "renderRightArm", at = @At("RETURN"))
    public void renderRightArm(AbstractClientPlayer player, CallbackInfo info) {
    	if(Skin3DMod.getInstance().isToggled()) {
    		renderFirstPersonArm(player, 3, (IMixinEntityPlayer)player);
    	}
    }

    @Inject(method = "renderLeftArm", at = @At("RETURN"))
    public void renderLeftArm(AbstractClientPlayer player, CallbackInfo info) {
    	if(Skin3DMod.getInstance().isToggled()) {
    		renderFirstPersonArm(player, 2, (IMixinEntityPlayer)player);
    	}
    }
    
    public void renderFirstPersonArm(AbstractClientPlayer player, int layerId, IMixinEntityPlayer settings) {
    	
        ModelPlayer modelplayer = (ModelPlayer) getMainModel();
        float pixelScaling = Skin3DMod.getInstance().getBaseVoxelSize();
        
        if(settings.getSkinLayers() == null && !setupModel(player, settings)) {
            return;
        }
        
        GlStateManager.pushMatrix();
        modelplayer.bipedRightArm.postRender(0.0625F);
        GlStateManager.scale(0.0625, 0.0625, 0.0625);
        GlStateManager.scale(pixelScaling, pixelScaling, pixelScaling);
        
        if(!smallArms) {
            settings.getSkinLayers()[layerId].x = -0.998f*16f;
        } else {
            settings.getSkinLayers()[layerId].x = -0.499f*16;
        }
        
        settings.getSkinLayers()[layerId].render(false);
        
        GlStateManager.popMatrix();
    }
    
    private boolean setupModel(AbstractClientPlayer abstractClientPlayerEntity, IMixinEntityPlayer settings) {
        
        if(!SkinUtils.hasCustomSkin(abstractClientPlayerEntity)) {
            return false;
        }
        
        SkinUtils.setup3dLayers(abstractClientPlayerEntity, settings, smallArms, null);
        
        return true;
    }
    
    @Redirect(method = "renderRightArm", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPlayer;isSneak:Z", ordinal = 0))
    private void resetArmState(ModelPlayer modelPlayer, boolean value) {
        modelPlayer.isRiding = modelPlayer.isSneak = false;
    }
    
    @Override
    public HeadLayerFeatureRenderer getHeadLayer() {
        return headLayer;
    }

    @Override
    public BodyLayerFeatureRenderer getBodyLayer() {
        return bodyLayer;
    }

    @Override
    public boolean hasThinArms() {
        return smallArms;
    }
}
