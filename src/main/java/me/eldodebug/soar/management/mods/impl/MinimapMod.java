package me.eldodebug.soar.management.mods.impl;

import java.awt.Color;

import me.eldodebug.soar.management.event.impl.EventNVG;
import org.lwjgl.opengl.GL11;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventLoadWorld;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.mods.impl.minimap.ChunkAtlas;
import me.eldodebug.soar.management.mods.impl.minimap.ChunkTile;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.utils.GlUtils;
import me.eldodebug.soar.utils.buffer.ScreenStencil;
import me.eldodebug.soar.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;

public class MinimapMod extends HUDMod {

	private NumberSetting widthSetting = new NumberSetting(TranslateText.WIDTH, this, 150, 10, 180, true);
	private NumberSetting heightSetting = new NumberSetting(TranslateText.HEIGHT, this, 70, 10, 180, true);
	private NumberSetting alphaSetting = new NumberSetting(TranslateText.ALPHA, this, 1F, 0.0F, 1F, false);
	
	private ScreenStencil stencil = new ScreenStencil();
	private ChunkAtlas chunkAtlas;
	
	public MinimapMod() {
		super(TranslateText.MINIMAP, TranslateText.MINIMAP_DESCRIPTION);
	}

	@Override
	public void setup() {
		chunkAtlas = new ChunkAtlas(10);
	}

	@EventTarget
	public void onRenderNVG(EventNVG event) {
		int width = widthSetting.getValueInt();
		int height = heightSetting.getValueInt();
		event.renderer().drawShadow(this.getX(), this.getY(), width * this.getScale(), height * this.getScale(), 6 * this.getScale());
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		int width = widthSetting.getValueInt();
		int height = heightSetting.getValueInt();
		
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        
		stencil.wrap(() -> drawMap(event.getPartialTicks()), this.getX(), this.getY(), width * this.getScale(), height * this.getScale(), 6 * this.getScale(), alphaSetting.getValueFloat());
		
		this.setWidth(width);
		this.setHeight(height);
	}
	
	private void drawMap(float partialTicks) {
        
		int width = widthSetting.getValueInt();
		int height = heightSetting.getValueInt();
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		EntityPlayer p = mc.thePlayer;
		
        double x = lerp(p.prevPosX, p.posX, partialTicks);
        double z = lerp(p.prevPosZ, p.posZ, partialTicks);
        double yaw = lerp(p.prevRotationYaw, p.rotationYaw, partialTicks);
        
        chunkAtlas.loadChunks((int) x >> 4, (int) z >> 4);
        
        RenderUtils.drawRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), new Color(138, 176, 254));
        
        GlUtils.startTranslate(this.getX() + (width / 2) * this.getScale(), this.getY() + (height / 2) * this.getScale());
        
        GL11.glRotated(180 - yaw, 0, 0, 1);
        
		GlStateManager.color(1F, 1F, 1F);
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();
        GlStateManager.bindTexture(chunkAtlas.getTextureHandle());

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        double chunkWidth = chunkAtlas.getSpriteWidth();
        double chunkHeight = chunkAtlas.getSpriteHeight();
        
        for (ChunkTile sprite : chunkAtlas) {
        	
            double minX = chunkAtlas.getSpriteX(sprite.getOffset());
            double minY = chunkAtlas.getSpriteY(sprite.getOffset());

            double maxX = minX + chunkWidth;
            double maxY = minY + chunkHeight;

            double renderX = (sprite.getChunkX() << 4) - x;
            double renderY = (sprite.getChunkZ() << 4) - z;

            worldRenderer.pos(renderX, renderY, 0).tex(minX, minY).endVertex();
            worldRenderer.pos(renderX, renderY + 16, 0).tex(minX, maxY).endVertex();
            worldRenderer.pos(renderX + 16, renderY + 16, 0).tex(maxX, maxY).endVertex();
            worldRenderer.pos(renderX + 16, renderY + 0, 0).tex(maxX, minY).endVertex();
        }

        tessellator.draw();
        
        GlUtils.stopTranslate();
	}
	
	@EventTarget
	public void onLoadWorld(EventLoadWorld event) {
		chunkAtlas.clear();
	}
	
    private double lerp(double prev, double current, float partialTicks) {
        return prev + (current - prev) * partialTicks;
    }
	
	@Override
	public void onEnable() {
		super.onEnable();
		chunkAtlas.clear();
	}
}
