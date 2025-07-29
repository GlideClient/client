package me.eldodebug.soar.injection.mixin.mixins.model;

import me.eldodebug.soar.management.mods.impl.FemaleGenderMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.management.mods.impl.WaveyCapesMod;
import net.minecraft.client.model.ModelPlayer;

@Mixin(ModelPlayer.class)
public class MixinModelPlayer extends ModelBase {

//    private ModelRenderer boobs;

    @Inject(method = "renderCape", at = @At("HEAD"), cancellable = true)
    public void renderCloak(float p_renderCape_1_, CallbackInfo ci) {
    	if(WaveyCapesMod.getInstance().isToggled()) {
    		ci.cancel();
    	}
    }

//    @Inject(method = "<init>", at = @At("RETURN"))
//    private void boobs(float size, boolean z, CallbackInfo c) {
//        boobs = new ModelRenderer(this, 16, 20);
//        boobs.addBox(-4F, -1.5F, -5F, 8, 4, 4, size);
//    }
//
//    @Inject(method = "render", at = @At("RETURN"))
//    public void render(Entity e, float v, float w, float x, float y, float z, float scale, CallbackInfo c) {
//        GlStateManager.pushMatrix();
//        boobs.showModel = FemaleGenderMod.getInstance().isToggled() && e == Minecraft.getMinecraft().thePlayer;
//        boobs.render(scale);
//        // move to pos when sneaking
//        boobs.offsetY = e.isSneaking() ? .25F : 0F;
//        boobs.offsetZ = e.isSneaking() ? .1F : 0F;
//        // rotate so they face up
//        boobs.rotateAngleX = 45;
//        GlStateManager.popMatrix();
//    }
}
