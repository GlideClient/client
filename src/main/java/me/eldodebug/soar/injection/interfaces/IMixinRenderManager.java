package me.eldodebug.soar.injection.interfaces;

import net.minecraft.client.renderer.entity.RenderPlayer;

public interface IMixinRenderManager {
    double getRenderPosX();
    double getRenderPosY();
    double getRenderPosZ();
    RenderPlayer getPlayerRenderer();
}
