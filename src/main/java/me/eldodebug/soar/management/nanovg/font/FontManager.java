package me.eldodebug.soar.management.nanovg.font;

import java.nio.ByteBuffer;

import org.lwjgl.nanovg.NanoVG;

import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.utils.IOUtils;

public class FontManager {

	public void init(long nvg) {
		loadFont(nvg, Fonts.GLICONIC);
		loadFont(nvg, Fonts.ICON_FILLED);
		loadFont(nvg, Fonts.ICON_OUTLINE);
		loadFont(nvg, Fonts.UNIFONT);
		loadFont(nvg, Fonts.FALLBACK);
		loadFont(nvg, Fonts.REGULAR);
		loadFont(nvg, Fonts.MEDIUM);
		loadFont(nvg, Fonts.SEMIBOLD);
		loadFont(nvg, Fonts.LEGACYICON);
		loadFont(nvg, Fonts.MOJANGLES);
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
			if(font == Fonts.MOJANGLES && Fonts.UNIFONT.isLoaded()){
				NanoVG.nvgAddFallbackFont(nvg, font.getName(), Fonts.UNIFONT.getName());
				NanoVG.nvgAddFallbackFont(nvg, font.getName(), Fonts.REGULAR.getName());
				NanoVG.nvgAddFallbackFont(nvg, font.getName(), Fonts.FALLBACK.getName());
			} else if(Fonts.FALLBACK.isLoaded()  && font != Fonts.FALLBACK){
				NanoVG.nvgAddFallbackFont(nvg, font.getName(), Fonts.FALLBACK.getName());
				NanoVG.nvgAddFallbackFont(nvg, font.getName(), Fonts.UNIFONT.getName());
			}

			if (font == Fonts.ICON_OUTLINE && Fonts.GLICONIC.isLoaded()){
				NanoVG.nvgAddFallbackFont(nvg, font.getName(), Fonts.GLICONIC.getName());
			}

			if (font == Fonts.ICON_FILLED && Fonts.GLICONIC.isLoaded()){
				NanoVG.nvgAddFallbackFont(nvg, font.getName(), Fonts.GLICONIC.getName());
			}

		}
	}
}
