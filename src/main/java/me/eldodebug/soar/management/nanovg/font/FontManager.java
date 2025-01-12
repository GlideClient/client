package me.eldodebug.soar.management.nanovg.font;

import java.nio.ByteBuffer;

import org.lwjgl.nanovg.NanoVG;

import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.utils.IOUtils;

public class FontManager {

	public void init(long nvg) {
		loadFont(nvg, Fonts.FALLBACK);
		loadFont(nvg, Fonts.FLUENT);
		for(Font f : Fonts.getFonts()) {
			loadFont(nvg, f);
		}
	}
	
	private void loadFont(long nvg, Font font) {
		
		if(font.isLoaded()) {
			return;
		}
		
		int loaded = -1;
		
		try {
			ByteBuffer buffer = IOUtils.resourceToByteBuffer(font.getResourceLocation());
			loaded = NanoVG.nvgCreateFontMem(nvg, font.getName(), buffer, false);
			font.setBuffer(buffer);
		} catch (Exception e) {
			GlideLogger.error("Failed to load font", e);
		}
		
		if(loaded == -1) {
			throw new RuntimeException("Failed to init font " + font.getName());
		}else {
			font.setLoaded(true);
			if(Fonts.FLUENT.isLoaded() && Fonts.FALLBACK.isLoaded() && font != Fonts.FLUENT && font != Fonts.FALLBACK){
				NanoVG.nvgAddFallbackFont(nvg, font.getName(), Fonts.FLUENT.getName());
				NanoVG.nvgAddFallbackFont(nvg, font.getName(), Fonts.FALLBACK.getName());
			}

		}
	}
}
