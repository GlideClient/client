package me.eldodebug.soar.utils;

import net.minecraft.util.Util;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.InputStream;

public class MacOSUtils {

    public static void setDockIcon(String path){
        if(Util.getOSType() != Util.EnumOS.OSX) return;  // Redundant check
        InputStream icon = MacOSUtils.class.getResourceAsStream(path); // you could probably use resource location but users can then change it with a TexturePack
        if (icon != null) {
            try {
                Class<?> appClass = Class.forName("com.apple.eawt.Application");
                appClass.getMethod("setDockIconImage", Image.class).invoke(appClass.getMethod("getApplication").invoke(null), ImageIO.read(icon));
            } catch (Exception e) { System.err.println("[ MacOS Utils ] Error setting dock icon: " + e.getMessage()); }
        } else { System.err.println("[ MacOS Utils ] Icon file could not be found"); }
    }

}