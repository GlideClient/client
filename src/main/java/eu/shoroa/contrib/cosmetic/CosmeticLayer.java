package eu.shoroa.contrib.cosmetic;

import me.eldodebug.soar.Glide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

public class CosmeticLayer implements LayerRenderer<AbstractClientPlayer> {
    @Override
    public void doRenderLayer(AbstractClientPlayer entityPlayer, float handSwing, float handSwingAmount, float ticks, float age, float headYaw, float headPitch, float scale) {
        if (entityPlayer == null || entityPlayer.isInvisible() || entityPlayer != Minecraft.getMinecraft().thePlayer) {
            return;
        }

        Glide.getInstance().getCosmeticManager().getCosmetics().stream().filter((c) -> c.enabled).forEach((c) -> {
            c.model.render(entityPlayer, handSwing, handSwingAmount, ticks, age, headYaw, headPitch);
        });
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
