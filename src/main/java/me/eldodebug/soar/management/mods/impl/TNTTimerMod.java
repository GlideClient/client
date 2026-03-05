package me.eldodebug.soar.management.mods.impl;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import me.eldodebug.soar.management.event.impl.EventNVG;
import org.lwjgl.opengl.GL11;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.event.impl.EventRenderTNT;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.SimpleHUDMod;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.utils.ServerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class TNTTimerMod extends SimpleHUDMod {

    private DecimalFormat timeFormatter = new DecimalFormat("0.00");
	private EntityTNTPrimed currentTNT;
	private float partialTicks;
	
	private ComboSetting typeSetting = new ComboSetting(TranslateText.TYPE, this, TranslateText.TAG, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.TAG), new Option(TranslateText.HUD))));
	
	public TNTTimerMod() {
		super(TranslateText.TNT_TIMER, TranslateText.TNT_TIMER_DESCRIPTION);
	}

	@EventTarget
	public void onRender2D(EventNVG event) {
		
		Option option = typeSetting.getOption();
		
		if(option.getTranslate().equals(TranslateText.HUD)) {
			this.draw();
			this.setCategory(ModCategory.HUD);
			this.setDraggable(true);
		} else {
			this.setDraggable(false);
			this.setCategory(ModCategory.RENDER);
		}
	}
	
	@EventTarget
	public void onRenderTNT(EventRenderTNT event) {
		
		Option option = typeSetting.getOption();
		
		if(option.getTranslate().equals(TranslateText.TAG)) {
			
	        int fuseTimer = ServerUtils.isHypixel() ? event.getEntity().fuse - 28 : event.getEntity().fuse;

	        if (fuseTimer >= 1) {
	            double distance = event.getEntity().getDistanceSqToEntity(event.getTntRenderer().getRenderManager().livingPlayer);

	            if (distance <= 4096.0D) {
	                float number = ((float) fuseTimer - event.getPartialTicks()) / 20.0F;
	                String time = timeFormatter.format((double) number);
	                FontRenderer fontrenderer = event.getTntRenderer().getFontRendererFromRenderManager();

	                GlStateManager.pushMatrix();
	                GlStateManager.translate((float) event.getX() + 0.0F, (float) event.getY() + event.getEntity().height + 0.5F, (float) event.getZ());
	                GL11.glNormal3f(0.0F, 1.0F, 0.0F);
	                GlStateManager.rotate(-event.getTntRenderer().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
	                byte xMultiplier = 1;

	                if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) {
	                    xMultiplier = -1;
	                }

	                float scale = 0.02666667F;

	                GlStateManager.rotate(event.getTntRenderer().getRenderManager().playerViewX * (float) xMultiplier, 1.0F, 0.0F, 0.0F);
	                GlStateManager.scale(-scale, -scale, scale);
	                GlStateManager.disableLighting();
	                GlStateManager.depthMask(false);
	                GlStateManager.disableDepth();
	                GlStateManager.enableBlend();
	                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
	                Tessellator tessellator = Tessellator.getInstance();
	                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
	                int stringWidth = fontrenderer.getStringWidth(time) >> 1;
	                float green = Math.min((float) fuseTimer / (ServerUtils.isHypixel() ? 52.0F : 80.0F), 1.0F);
	                Color color = new Color(1.0F - green, green, 0.0F);

	                GlStateManager.enableDepth();
	                GlStateManager.depthMask(true);
	                GlStateManager.disableTexture2D();
	                worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
	                worldrenderer.pos((double) (-stringWidth - 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
	                worldrenderer.pos((double) (-stringWidth - 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
	                worldrenderer.pos((double) (stringWidth + 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
	                worldrenderer.pos((double) (stringWidth + 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
	                tessellator.draw();
	                GlStateManager.enableTexture2D();
	                fontrenderer.drawString(time, -fontrenderer.getStringWidth(time) >> 1, 0, color.getRGB());
	                GlStateManager.enableLighting();
	                GlStateManager.disableBlend();
	                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	                GlStateManager.popMatrix();
	            }

	        }
		}
	}
	
	@Override
	public String getText() {
		
		if((mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null && mc.objectMouseOver.typeOfHit == MovingObjectType.ENTITY && mc.objectMouseOver.entityHit instanceof EntityTNTPrimed)) {
			currentTNT = (EntityTNTPrimed) mc.objectMouseOver.entityHit;
		}
		
		if(currentTNT != null) {
			
	        int fuseTimer = ServerUtils.isHypixel() ? currentTNT.fuse - 28 : currentTNT.fuse;
	        
	        if (fuseTimer >= 1) {
                float number = ((float) fuseTimer - partialTicks) / 20.0F;
                String time = timeFormatter.format((double) number);
                
                return time + "s";
	        }else {
	        	currentTNT = null;
	        }
		}
		
		return "There is no TNT";
	}
}
