package eu.shoroa.contrib.cosmetic;

import eu.shoroa.contrib.cosmetic.models.Boobs;
import eu.shoroa.contrib.debug.Debug3DRenderer;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender3D;
import net.minecraft.client.model.ModelBase;

import java.util.ArrayList;

public class CosmeticManager {
    private final ArrayList<Cosmetic> cosmetics = new ArrayList<>();

    public void init() {
        cosmetics.add(new Cosmetic("Boobs", new Boobs()));

        Glide.getInstance().getEventManager().register(this);
    }

    @EventTarget
    private void on3D(EventRender3D eventRender3D) {
        Debug3DRenderer.renderAll();
    }

    public ArrayList<Cosmetic> getCosmetics() {
        return cosmetics;
    }

    public <T extends ModelBase> Cosmetic getCosmetic(Class<T> clazz) {
        for (Cosmetic cosmetic : cosmetics) {
            ModelBase model = cosmetic.model;
            if (clazz.isInstance(model)) {
                return cosmetic;
            }
        }
        return null;
    }
}

