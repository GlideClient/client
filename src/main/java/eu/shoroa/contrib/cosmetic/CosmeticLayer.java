package eu.shoroa.contrib.cosmetic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

public class CosmeticLayer implements LayerRenderer<AbstractClientPlayer> {
    @Override
    public void doRenderLayer(AbstractClientPlayer entityPlayer, float handSwing, float handSwingAmount, float ticks, float age, float headYaw, float headPitch, float scale) {
        if (entityPlayer == null || entityPlayer.isInvisible() || entityPlayer != Minecraft.getMinecraft().thePlayer) {
            return;
        }

        CosmeticManager.getInstance().renderLayer(entityPlayer, handSwing, handSwingAmount, ticks, age, headYaw, headPitch, scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
