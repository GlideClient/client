package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventNVG;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemBlock;

public class GodbridgeAssistMod extends HUDMod {

	private int shiftedTicks = 0;
	
	public GodbridgeAssistMod() {
		super(TranslateText.GODBRIDGE_ASSIST, TranslateText.GODBRIDGE_ASSIST_DESCRIPTION);
	}

	@EventTarget
	public void onTick(EventTick event) {
        if (mc.inGameHasFocus) {
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                shiftedTicks++;
            } else {
                shiftedTicks = 0;
            }
        }
	}
	
	@EventTarget
	public void onRender2D(EventNVG event) {
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
        ScaledResolution sr = new ScaledResolution(mc);
        
        if (mc.inGameHasFocus) {
        	
            if (!mc.thePlayer.isCollidedVertically) {
                return;
            }

            if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
                return;
            }

            if (mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem) == null) {
                return;
            }

            if (!(mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem).getItem() instanceof ItemBlock)) {
                return;
            }

            if (this.shiftedTicks < 5) {
                return;
            }

            if (Math.abs(Math.abs(mc.thePlayer.rotationYaw % 90.0F) - 45.0F) >= 5.0F) {
                return;
            }

            if (mc.thePlayer.rotationPitch <= 70.0F || mc.thePlayer.rotationPitch >= 80.0F) {
                return;
            }

            if (Math.round(Math.abs(mc.thePlayer.posX - Math.floor(mc.thePlayer.posX)) * 10.0D) != 3L && 
            		Math.round(Math.abs(mc.thePlayer.posX - Math.floor(mc.thePlayer.posX)) * 10.0D) != 7L && 
            		Math.round(Math.abs(mc.thePlayer.posZ - Math.floor(mc.thePlayer.posZ)) * 10.0D) != 3L && 
            		Math.round(Math.abs(mc.thePlayer.posZ - Math.floor(mc.thePlayer.posZ)) * 10.0D) != 7L) {
                return;
            }
            
            int alpha = (int) ((Math.abs(Math.abs((double) mc.thePlayer.rotationYaw % 90.0D) - 45.0D) / 5) * 255);
            int round = (int) ((Math.abs(Math.abs((double) mc.thePlayer.rotationYaw % 90.0D) - 45.0D) / 5) * 360);
            
            nvg.drawArc(sr.getScaledWidth() / 2, sr.getScaledHeight() / 2, 12, -90, round, 1.6F, this.getFontColor(alpha));
        }
	}
}
