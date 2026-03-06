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
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class ScreenAnimation {

	private Minecraft mc = Minecraft.getMinecraft();

	private int fbWidth, fbHeight;
	private NVGLUFramebuffer fb;

	public void wrap(Runnable glRender, Runnable task, float x, float y, float width, float height, float animationProgress, float alphaProgress, boolean stencil) {

		ScaledResolution sr = new ScaledResolution(mc);
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		int factor = sr.getScaleFactor();

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

		GlStateManager.enableTexture2D();;

		GL11.glGetFloat(GL11.GL_COLOR_CLEAR_VALUE, floaty);

		GL11.glClearColor(0, 0, 0, 0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		GL11.glClearColor(floaty.get(0), floaty.get(1), floaty.get(2), floaty.get(3));

		nvg.setupAndDraw(task);

		if(glRender != null) {
			glRender.run();
		}

		mc.getFramebuffer().bindFramebuffer(true);

		nvg.setupAndDraw(() -> {

			nvg.setAlpha(Math.min(alphaProgress, 1.0F));
			nvg.scale(x * factor, y * factor, width * factor, height * factor, animationProgress);

			NVGPaint paint = NVGPaint.create();

			NanoVG.nvgBeginPath(nvg.getContext());

			if(stencil) {
				NanoVG.nvgRect(nvg.getContext(), x * factor, y * factor, width * factor, height * factor);
			}else {
				NanoVG.nvgRect(nvg.getContext(), 0, 0, mc.displayWidth, mc.displayHeight);
			}

			NanoVG.nvgFillPaint(nvg.getContext(), NanoVG.nvgImagePattern(nvg.getContext(), 0, 0, mc.displayWidth,  mc.displayHeight, 0, fb.image(), 1, paint));
			NanoVG.nvgFill(nvg.getContext());
		}, false);
	}

	public void wrap(Runnable task, float x, float y, float width, float height, float animationProgress, float alphaProgress) {
		wrap(null, task, x, y, width, height, animationProgress, alphaProgress, false);
	}

	public void wrap(Runnable glRender, Runnable task, float x, float y, float width, float height, float animationProgress, float alphaProgress) {
		wrap(glRender, task, x, y, width, height, animationProgress, alphaProgress, false);
	}

	public void wrap(Runnable task, float x, float y, float width, float height, float animationProgress, float alphaProgress, boolean stencil) {
		wrap(null, task, x, y, width, height, animationProgress, alphaProgress, stencil);
	}

	public void wrap(Runnable task, float animationProgress, float alphaProgress) {

		ScaledResolution sr = new ScaledResolution(mc);

		wrap(null, task, 0, 0, sr.getScaledWidth(), sr.getScaledHeight(), animationProgress, alphaProgress, false);
	}

	public void wrap(Runnable task, float x, float y, float width, float height, float progress) {
		wrap(null, task, x, y, width, height, progress, progress, false);
	}

	public void wrap(Runnable task, float progress) {

		ScaledResolution sr = new ScaledResolution(mc);

		wrap(null, task, 0, 0, sr.getScaledWidth(), sr.getScaledHeight(), progress, progress, false);
	}

	public void close() {

		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();

		if(fb != null) {
			NanoVGGL2.nvgluDeleteFramebuffer(nvg.getContext(), fb);
			fb = null;
		}
	}
}
