package me.eldodebug.soar.utils.buffer;

import java.nio.FloatBuffer;

import org.lwjgl.nanovg.NVGLUFramebuffer;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.opengl.GL11;
import org.lwjgl3.BufferUtils;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import net.minecraft.client.Minecraft;

public class ScreenAlpha {
	
	private Minecraft mc = Minecraft.getMinecraft();
	
	private int fbWidth, fbHeight;
	private NVGLUFramebuffer fb;
	
	public void wrap(Runnable task, float alphaProgress) {
		
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		
		if(fbWidth != mc.displayWidth || fbHeight != mc.displayHeight) {
			close();
		}
		
		if (fb == null) {
			fbWidth = mc.displayWidth;
			fbHeight = mc.displayHeight;
			fb = NanoVGGL2.nvgluCreateFramebuffer(nvg.getContext(), mc.displayWidth, mc.displayHeight, 0);
		}
		
		NanoVGGL2.nvgluBindFramebuffer(nvg.getContext(), fb);

		GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);

		FloatBuffer floaty = BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_COLOR_CLEAR_VALUE, floaty);

		GL11.glClearColor(0, 0, 0, 0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		GL11.glClearColor(floaty.get(0), floaty.get(1), floaty.get(2), floaty.get(3));

		nvg.setupAndDraw(task);
		
		mc.getFramebuffer().bindFramebuffer(true);
		
		nvg.setupAndDraw(() -> {
			
			nvg.setAlpha(Math.min(alphaProgress, 1.0F));

			NVGPaint paint = NVGPaint.create();
			
			NanoVG.nvgBeginPath(nvg.getContext());
			
			NanoVG.nvgRect(nvg.getContext(), 0, 0, mc.displayWidth, mc.displayHeight);
			
			NanoVG.nvgFillPaint(nvg.getContext(), NanoVG.nvgImagePattern(nvg.getContext(), 0, 0, mc.displayWidth,  mc.displayHeight, 0, fb.image(), 1, paint));
			NanoVG.nvgFill(nvg.getContext());
		}, false);
	}
	
	public void close() {
		
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		
		if(fb != null) {
			NanoVGGL2.nvgluDeleteFramebuffer(nvg.getContext(), fb);
			fb = null;
		}
	}
}
