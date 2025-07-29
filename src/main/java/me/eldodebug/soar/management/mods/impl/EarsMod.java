package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;

public class EarsMod extends Mod {

	private static EarsMod instance;

	public EarsMod() {
		super(TranslateText.EARS, TranslateText.EARS_DESCRIPTION, ModCategory.PLAYER);

		instance = this;
	}

	@Override
	public void onEnable() {
		super.onEnable();
//		Glide.getInstance().getCosmeticManager().getCosmetic(EarsLayer.class).enabled = true;
	}

	@Override
	public void onDisable() {
		super.onDisable();
//		Glide.getInstance().getCosmeticManager().getCosmetic(EarsLayer.class).enabled = false;
	}

	public static void drawLeft(AbstractClientPlayer entitylivingbaseIn, float partialTicks, RenderPlayer playerRenderer) {
		playerRenderer.bindTexture(new ResourceLocation("soar/cosmetics/ears/blackcatleft.png"));
		int i = 0;
		float f = entitylivingbaseIn.prevRotationYaw + (entitylivingbaseIn.rotationYaw - entitylivingbaseIn.prevRotationYaw) * partialTicks - (entitylivingbaseIn.prevRenderYawOffset + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * partialTicks);
		float f1 = entitylivingbaseIn.prevRotationPitch + (entitylivingbaseIn.rotationPitch - entitylivingbaseIn.prevRotationPitch) * partialTicks;
		GlStateManager.pushMatrix();
		setup(f, f1, i);
		float f2 = 1.3333334F;
		GlStateManager.scale(f2, f2, f2);
		if(entitylivingbaseIn.isSneaking()){
			GlStateManager.translate(0, .2F, 0.0F);
		}

		playerRenderer.getMainModel().renderDeadmau5Head(0.0625F);
		GlStateManager.popMatrix();
	}

	public static void drawRight(AbstractClientPlayer entitylivingbaseIn, float pt, RenderPlayer playerRenderer) {
		playerRenderer.bindTexture(new ResourceLocation("soar/cosmetics/ears/blackcatright.png"));
		int i = 1;
		float f = entitylivingbaseIn.prevRotationYaw + (entitylivingbaseIn.rotationYaw - entitylivingbaseIn.prevRotationYaw) * pt - (entitylivingbaseIn.prevRenderYawOffset + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * pt);
		float f1 = entitylivingbaseIn.prevRotationPitch + (entitylivingbaseIn.rotationPitch - entitylivingbaseIn.prevRotationPitch) * pt;
		GlStateManager.pushMatrix();
		setup(f, f1, i);
		float f2 = 1.3333334F;
		GlStateManager.scale(f2, f2, f2);
		if(entitylivingbaseIn.isSneaking()){
			GlStateManager.translate(0, .2F, 0.0F);
		}
		playerRenderer.getMainModel().renderDeadmau5Head(0.0625F);
		GlStateManager.popMatrix();
	}

	public static void setup(float f, float f1, float i){
		GlStateManager.rotate(f, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(f1, 1.0F, 0.0F, 0.0F);
		GlStateManager.translate(0.375F * (i * 2F - 1F), 0.0F, 0.0F);
		GlStateManager.translate(0.0F, -0.375F, 0.0F);
		GlStateManager.rotate(-f1, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(-f, 0.0F, 1.0F, 0.0F);
	}

	public static EarsMod getInstance() {
		return instance;
	}
}
