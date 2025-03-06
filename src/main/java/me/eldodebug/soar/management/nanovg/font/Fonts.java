package me.eldodebug.soar.management.nanovg.font;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.util.ResourceLocation;

public class Fonts {
	
	private static final String PATH = "soar/fonts/";

	//public static final Font FLUENT = new Font("fluent", new ResourceLocation(PATH + "fluent.ttf"));
	public static final Font FALLBACK = new Font("fallback", new ResourceLocation(PATH + "fallback.ttf"));
	public static final Font REGULAR = new Font("regular", new ResourceLocation(PATH + "inter/Inter-Regular.ttf"));
	public static final Font MEDIUM = new Font("medium", new ResourceLocation(PATH + "inter/Inter-Medium.ttf"));
	public static final Font SEMIBOLD = new Font("semi-bold", new ResourceLocation(PATH + "inter/Inter-SemiBold.ttf"));
	public static final Font LEGACYICON = new Font("icon", new ResourceLocation(PATH + "Icon.ttf"));
	public static final Font MOJANGLES = new Font("mojangles", new ResourceLocation(PATH + "mojangles.ttf"));
	public static final Font UNIFONT = new Font("unifont", new ResourceLocation(PATH + "unifont/unifont.otf"));


	public static ArrayList<Font> getFonts() {
		return new ArrayList<Font>(Arrays.asList(MEDIUM, SEMIBOLD, REGULAR, LEGACYICON, MOJANGLES));
	}
}
