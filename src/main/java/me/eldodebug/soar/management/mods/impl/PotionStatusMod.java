package me.eldodebug.soar.management.mods.impl;

import java.util.Arrays;
import java.util.Collection;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.event.impl.EventUpdate;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.utils.GlUtils;
import me.eldodebug.soar.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class PotionStatusMod extends HUDMod {

	private int maxString, prevPotionCount;
	
	private BooleanSetting compactSetting = new BooleanSetting(TranslateText.COMPACT, this, false);
	
	private Collection<PotionEffect> potions;
	
	public PotionStatusMod() {
		super(TranslateText.POTION_STATUS, TranslateText.POTION_STATUS_DESCRIPTION);
	}

	@EventTarget
	public void onUpdate(EventUpdate event) {
		
		if(this.isEditing() || mc.thePlayer == null) {
			potions = Arrays.asList(new PotionEffect(1, 0), new PotionEffect(10, 0));
		}else {
			potions = mc.thePlayer.getActivePotionEffects();
		}
	}
	
	@EventTarget
	public void onRender2D(EventRender2D event) {
		
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		
		nvg.setupAndDraw(() -> drawNanoVG(nvg));
		
		if (!potions.isEmpty()) {
			
			int ySize = compactSetting.isToggled() ? 22 : 23;
			int offsetY = 16;
			
            for (PotionEffect potioneffect : potions) {
            	
                Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            	mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                int index = potion.getStatusIconIndex();
                GlStateManager.enableBlend();
                
                GlUtils.startScale(this.getX(), this.getY(), this.getScale());
                
                if(compactSetting.isToggled()) {
                	GlUtils.startScale((this.getX() + 21) - 20, (this.getY() + offsetY) - 11 - offsetY - 2F, 18, 18, 0.72F);
                    RenderUtils.drawTexturedModalRect((this.getX() + 21) - 20, (this.getY() + offsetY) - 11, 0 + index % 8 * 18, 198 + index / 8 * 18, 18, 18);
                    GlUtils.stopScale();
                } else {
                    RenderUtils.drawTexturedModalRect((this.getX() + 21) - 17, (this.getY() + offsetY) - 12, 0 + index % 8 * 18, 198 + index / 8 * 18, 18, 18);
                }
                
                GlUtils.stopScale();
                
                offsetY+=ySize;
            }
		}
	}
	
	private void drawNanoVG(NanoVGManager nvg) {
		
		int ySize = compactSetting.isToggled() ? 16 : 23;
		int offsetY = 16;
		
		if(potions.isEmpty()) {
			maxString = 0;
		}
		
		if(!potions.isEmpty()) {
			
			this.drawBackground(maxString + 29, (ySize * potions.size()) + 2);
			
            for (PotionEffect potioneffect : potions) {
            	
                Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                
    			String name = I18n.format(potion.getName(), new Object[0]);
    			
                if (potioneffect.getAmplifier() == 1) {
                	name = name + " " + I18n.format("enchantment.level.2", new Object[0]);
                } else if (potioneffect.getAmplifier() == 2) {
                	name = name + " " + I18n.format("enchantment.level.3", new Object[0]);
                } else if (potioneffect.getAmplifier() == 3) {
                	name = name + " " + I18n.format("enchantment.level.4", new Object[0]);
                }
                
                String time = Potion.getDurationString(potioneffect);
                
                if(compactSetting.isToggled()) {
                	this.drawText(name + " | " + time, 20, offsetY - 10.5F, 9, getHudFont(1));
                } else {
                	this.drawText(name, 25, offsetY - 12, 9, getHudFont(1));
                	this.drawText(time, 25, offsetY - 1, 8, getHudFont(1));
                }
                
                offsetY+=ySize;
                
                if(compactSetting.isToggled()) {
                	
                	float totalWidth = nvg.getTextWidth(name + " | " + time, 9, getHudFont(1));
                	
                	if(maxString < totalWidth || prevPotionCount != potions.size()) {
                		maxString = (int) totalWidth - 4;
                	}
                } else {
                    float levelWidth = nvg.getTextWidth(name, 9, getHudFont(1));
                    float timeWidth = nvg.getTextWidth(time, 9, getHudFont(1));
                    
                    if(maxString < levelWidth || maxString < timeWidth || prevPotionCount != potions.size()) {
                    	
                    	if(levelWidth > timeWidth) {
                    		maxString = (int) (levelWidth);
                    	} else {
                    		maxString = (int) (timeWidth);
                    	}
                    	
                    	prevPotionCount = potions.size();
                    }
                }
            }
		}
		
		this.setWidth(maxString + 29);
		this.setHeight((ySize * 2) + 2);
	}
}
