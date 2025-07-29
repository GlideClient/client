package eu.shoroa.contrib.cosmetic.util;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderPlayer;

@SuppressWarnings("ALL")
public class PlayerModelBase extends ModelBase {
    protected RenderPlayer renderPlayer;

    public PlayerModelBase(RenderPlayer renderPlayer) {
        this.renderPlayer = renderPlayer;
    }
}
