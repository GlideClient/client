package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.Event;
import me.eldodebug.soar.management.nanovg.NanoVGManager;

public class EventNVG extends Event {
    private final float partialTicks;

    public EventNVG(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public NanoVGManager renderer() {
        return Glide.getInstance().getNanoVGManager();
    }
}
