package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventNVG;
import me.eldodebug.soar.management.event.impl.EventPlayerHeadRotation;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import net.minecraft.util.MathHelper;

public class MouseStrokesMod extends HUDMod {

    private float mouseX, mouseY, lastMouseX, lastMouseY;

    public MouseStrokesMod() {
        super(TranslateText.MOUSE_STROKES, TranslateText.MOUSE_STROKES_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventNVG event) {
        float calculatedMouseX = (lastMouseX + ((mouseX - lastMouseX) * event.getPartialTicks()));
        float calculatedMouseY = (lastMouseY + ((mouseY - lastMouseY) * event.getPartialTicks()));

        this.drawBackground(58, 58);
        this.drawRoundedRect(calculatedMouseX + 28 - 3.5F, calculatedMouseY + 28 - 3.5F, 9, 9, 9 / 2);

        this.setWidth(58);
        this.setHeight(58);
    }

    @EventTarget
    public void onPlayerHeadRotation(EventPlayerHeadRotation event) {
        mouseX += event.getYaw() / 40F;
        mouseY -= event.getPitch() / 40F;
        mouseX = MathHelper.clamp_float(mouseX, -18, 18);
        mouseY = MathHelper.clamp_float(mouseY, -18, 18);
    }

    @EventTarget
    public void onTick(EventTick event) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        mouseX *= 0.75F;
        mouseY *= 0.75F;
    }
}
