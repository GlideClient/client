package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.nanovg.font.Icons;
import me.eldodebug.soar.utils.GlUtils;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.easing.EaseBackIn;
import me.eldodebug.soar.utils.buffer.ScreenAnimation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.opengl.GL11;

public class BlockInfoMod extends HUDMod {

	private long lastSelection;
	
	private Animation introAnimation;
	private ScreenAnimation screenAnimation = new ScreenAnimation();
	
    private BlockPos pos;
    private IBlockState state;
    private Block block;
    
	public BlockInfoMod() {
		super(Icons.RECEIPT_CUBE_24, TranslateText.BLOCK_INFO, TranslateText.BLOCK_INFO_DESCRIPTION, "waila", false);
	}

	@Override
	public void setup() {
		introAnimation = new EaseBackIn(320, 1.0F, 2.0F);
		introAnimation.setDirection(Direction.BACKWARDS);
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		screenAnimation.wrap(this::drawBlock, this::drawNanoVG, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 2 - introAnimation.getValueFloat(), introAnimation.getValueFloat());
		
		this.setWidth(80);
		this.setHeight(80);
	}

	private void drawBlock() {
		if (block == null || block.equals(Blocks.portal) || block.equals(Blocks.end_portal)) {return;}
		boolean depth = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
		RenderHelper.enableGUIStandardItemLighting();
		GlUtils.startScale(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 2F * this.getScale());
		mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(block), this.getX() + (this.getWidth() / 2) - 8, this.getY() + (this.getHeight() / 2) - 8);
		GlUtils.stopScale();
		RenderHelper.disableStandardItemLighting();
		if (!depth) GlStateManager.disableDepth();
	}
	
	private void drawNanoVG() {
		
		if((mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) || this.isEditing()) {
			
			if(this.isEditing()) {
				block = Blocks.grass;
			}else {
				pos = mc.objectMouseOver.getBlockPos();
				state = mc.theWorld.getBlockState(pos);
				block = state.getBlock();
			}
			
			introAnimation.setDirection(Direction.FORWARDS);
			lastSelection = System.currentTimeMillis();
		}else {
			if(System.currentTimeMillis() - lastSelection > 1000) {
				introAnimation.setDirection(Direction.BACKWARDS);
			}
		}
		
		if(block != null && !block.equals(Blocks.portal) && !block.equals(Blocks.end_portal)) {
			
			this.drawBackground(80, 80);
			
			this.drawCenteredText(block.getLocalizedName(), 40, 10, 9, getHudFont(1));
		}
	}
}
