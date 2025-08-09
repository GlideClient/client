package eu.shoroa.contrib.cosmetic;

import eu.shoroa.contrib.impl.BoobsCosmetic;
import eu.shoroa.contrib.impl.DoubleHaloCosmetic;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.event.impl.EventRender3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class CosmeticManager {
    private static CosmeticManager instance;

    private CosmeticManager() {
    }

    private final List<Cosmetic> cosmetics = new ArrayList<>();

    public static CosmeticManager getInstance() {
        if (instance == null) {
            instance = new CosmeticManager();
        }
        return instance;
    }

    public void init() {
        cosmetics.add(new BoobsCosmetic());
        cosmetics.add(new DoubleHaloCosmetic("Serika Halo", "soar/cosmetics/misc/serika_halo.png"));

        Glide.getInstance().getEventManager().register(this);
    }

    public void renderLayer(AbstractClientPlayer entityPlayer, float handSwing, float handSwingAmount, float ticks, float age, float headYaw, float headPitch, float scale) {
        cosmetics.stream().filter(cosmetic -> (cosmetic.isEnabled() || entityPlayer instanceof CosmeticPreviewEntity) && cosmetic.getPositionType() == PositionType.LOCAL).forEach(cosmetic -> {
            cosmetic.render(entityPlayer, handSwing, handSwingAmount, ticks, age, headYaw, headPitch, scale);
        });
    }

    @EventTarget
    public void renderWorld(EventRender3D event) {
        cosmetics.stream().filter(cosmetic -> cosmetic.isEnabled() && cosmetic.getPositionType() == PositionType.WORLD).forEach(cosmetic -> {
            cosmetic.render(Minecraft.getMinecraft().thePlayer);
        });
    }

//    @EventTarget
    public void renderFBO() {
        cosmetics.forEach(Cosmetic::renderPreview);
    }

    public List<Cosmetic> getCosmetics() {
        return cosmetics;
    }

    @Nonnull
    public Cosmetic getCosmeticByClass(Class<? extends Cosmetic> clazz) {
        for (Cosmetic cosmetic : cosmetics) {
            if (cosmetic.getClass().equals(clazz)) {
                return cosmetic;
            }
        }

        throw new IllegalArgumentException("Cosmetic not found for class " + clazz.getName());
    }

    @Nonnull
    public Cosmetic getCosmeticByName(String name) {
        for (Cosmetic cosmetic : cosmetics) {
            if (cosmetic.getName().equals(name)) {
                return cosmetic;
            }
        }

        throw new IllegalArgumentException("Cosmetic not found for name " + name);
    }

    public void setRenderPlayer(RenderPlayer renderPlayer) {
        cosmetics.forEach(cosmetic -> {
            cosmetic.setRenderPlayer(renderPlayer);
        });
    }
}
