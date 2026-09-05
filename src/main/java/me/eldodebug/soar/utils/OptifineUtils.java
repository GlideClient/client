package me.eldodebug.soar.utils;

import java.lang.reflect.Field;

import me.eldodebug.soar.injection.mixin.GlideTweaker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

public class OptifineUtils {

    private static Field gameSettings_ofFastRender;
    private static Minecraft mc = Minecraft.getMinecraft();
    
    static {
        try {
            Class.forName("Config");

            gameSettings_ofFastRender = GameSettings.class.getDeclaredField("ofFastRender");
            gameSettings_ofFastRender.setAccessible(true);
        } catch (ClassNotFoundException ignore) {
        } catch (NoSuchFieldException e) {}
    }
    
    public static void disableFastRender() {
    	
		if(GlideTweaker.hasOptifine) {
			try {
				OptifineUtils.gameSettings_ofFastRender.set(mc.gameSettings, false);
			} catch (IllegalArgumentException | IllegalAccessException e) {}
		}
		
		//mc.gameSettings.useVbo = true;
		mc.gameSettings.fboEnable = true;
    }
}
