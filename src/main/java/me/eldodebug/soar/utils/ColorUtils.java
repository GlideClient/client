package me.eldodebug.soar.utils;

import java.awt.Color;

import net.minecraft.client.renderer.GlStateManager;

public class ColorUtils {

    public static Color getRainbow(int index, double speed, int alpha) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        int color = Color.HSBtoRGB(hue, 1F, 1);
        Color c =  new Color(color);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
    
    public static Color interpolateColors(int speed, int index, Color start, Color end) {
    	
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        
        return ColorUtils.interpolateColorHue(start, end, angle / 360f);
    }

	public static Color interpolateColor(Color from, Color to, double delta) {
		int red = MathUtils.interpolateInt(from.getRed(), to.getRed(), delta);
		int green = MathUtils.interpolateInt(from.getGreen(), to.getGreen(), delta);
		int blue = MathUtils.interpolateInt(from.getBlue(), to.getBlue(), delta);
		int alpha = MathUtils.interpolateInt(from.getAlpha(), to.getAlpha(), delta);
		return new Color(red, green, blue, alpha);
	}
    
    private static Color interpolateColorHue(Color color1, Color color2, float amount) {
    	
        amount = Math.min(1, Math.max(0, amount));

        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);

        Color resultColor = Color.getHSBColor(MathUtils.interpolateFloat(color1HSB[0], color2HSB[0], amount), MathUtils.interpolateFloat(color1HSB[1], color2HSB[1], amount), MathUtils.interpolateFloat(color1HSB[2], color2HSB[2], amount));

        return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(), MathUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }
    
	public static float getHue(Color color) {
		return rgbToHsb(color)[0];
	}
	
	public static float getSaturation(Color color) {
		return rgbToHsb(color)[1];
	}
	
	public static float getBrightness(Color color) {
		return rgbToHsb(color)[2];
	}
	
	private static float[] rgbToHsb(Color color) {
		
        float[] hsv = new float[3];
        
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);
        
        return hsv;
	}
	
	public static String removeColorCode(String text) {
		return text.replaceAll("\\u00a7" + "1", "").replaceAll("\\u00a7" + "2", "").replaceAll("\\u00a7" + "3", "")
				.replaceAll("\\u00a7" + "4", "").replaceAll("\\u00a7" + "5", "").replaceAll("\\u00a7" + "6", "")
				.replaceAll("\\u00a7" + "7", "").replaceAll("\\u00a7" + "8", "").replaceAll("\\u00a7" + "9", "")
				.replaceAll("\\u00a7" + "a", "").replaceAll("\\u00a7" + "b", "").replaceAll("\\u00a7" + "c", "")
				.replaceAll("\\u00a7" + "d", "").replaceAll("\\u00a7" + "e", "").replaceAll("\\u00a7" + "f", "")
				.replaceAll("\\u00a7" + "g", "").replaceAll("\\u00a7" + "k", "").replaceAll("\\u00a7" + "l", "")
				.replaceAll("\\u00a7" + "m", "").replaceAll("\\u00a7" + "n", "").replaceAll("\\u00a7" + "o", "")
				.replaceAll("\\u00a7" + "r", "");
	}
	
	public static void setColor(int color, float alpha) {
		
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        
        GlStateManager.color(r, g, b, alpha);
	}
	
	public static Color getColorByInt(int color) {
		
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        float a = (float)(color >> 24 & 255) / 255.0F;
        
        return new Color(r, g, b, a);
	}
	
	public static void setColor(int color) {
		setColor(color, (float) (color >> 24 & 255) / 255.0F);
	}
	
	public static void resetColor() {
		GlStateManager.color(1F, 1F, 1F, 1F);
	}
	
	public static Color applyAlpha(Color color, int alpha) {
		
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		
		return new Color(r, g, b, alpha);
	}
	
	public static float getAlphaByInt(int color) {
		
        float a = (float)(color >> 24 & 255) / 255.0F;
        
        return a;
	}
}
