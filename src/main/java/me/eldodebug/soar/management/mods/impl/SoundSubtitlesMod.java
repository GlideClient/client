package me.eldodebug.soar.management.mods.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.mods.impl.subtitle.Subtitle;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

public class SoundSubtitlesMod extends HUDMod {

	private static SoundSubtitlesMod instance;
	
    private List<Subtitle> subtitles = Lists.newArrayList();
    private HashMap<String, String> soundMap = new HashMap<>();
    
    private NumberSetting maxSetting = new NumberSetting(TranslateText.MAX, this, 3, 1, 10, true);
    
    private SimpleAnimation backgroundAnimation = new SimpleAnimation(0.0F);
    
	public SoundSubtitlesMod() {
		super(TranslateText.SOUND_SUBTITLES, TranslateText.SOUND_SUBTITLES_DESCRIPTION);
		
		instance = this;
		
        ResourceLocation mapped = new ResourceLocation("soar/soundtitles/data.json");
        
        try {
            JsonObject obj = new JsonParser().parse(read(mc.getResourceManager().getResource(mapped).getInputStream())).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                soundMap.put(entry.getKey(), entry.getValue().getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	@EventTarget
	public void onRender2D(EventRender2D event) {
		
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		
		nvg.setupAndDraw(() -> drawNanoVG());
	}
	
	private void drawNanoVG() {
		
        Vec3 Vec3 = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        Vec3 Vec31 = (new Vec3(0.0D, 0.0D, -1.0D)).rotatePitch(-mc.thePlayer.rotationPitch * 0.017453292F).rotateYaw(-mc.thePlayer.rotationYaw * 0.017453292F);
        Vec3 Vec32 = (new Vec3(0.0D, 1.0D, 0.0D)).rotatePitch(-mc.thePlayer.rotationPitch * 0.017453292F).rotateYaw(-mc.thePlayer.rotationYaw * 0.017453292F);
        Vec3 Vec33 = Vec31.crossProduct(Vec32);
        
        int subtitleWidth = 120;
        int subtitleHeight = (this.isEditing() ? 3 : subtitles.size()) * 16;
        
        ArrayList<Subtitle> removeList = new ArrayList<Subtitle>();
        
        for(Subtitle subtitle : subtitles) {
        	
            if (subtitle.getStartTime() + 3000L <= Minecraft.getSystemTime()) {
            	subtitle.setRemove(true);
            }
            
            if(subtitle.isRemove() && subtitle.isDone()) {
            	removeList.add(subtitle);
            }
        }
        
        subtitles.removeAll(removeList);
        
        backgroundAnimation.setAnimation(subtitleHeight, 20);
        
		if(backgroundAnimation.getValue() > 1) {
			
			ArrayList<Subtitle> fakeSubtitle = new ArrayList<Subtitle>();
			
			this.drawBackground(subtitleWidth, backgroundAnimation.getValue());
			
	        int index = 1;
	        
	        if(this.isEditing()) {
	        	
	        	double posX = mc.thePlayer.posX;
	        	double posY = mc.thePlayer.posY;
	        	double posZ = mc.thePlayer.posZ;
	        	
	        	fakeSubtitle.add(new Subtitle("Sound 1", new Vec3(posX, posY, posZ)));
	        	fakeSubtitle.add(new Subtitle("Sound 2", new Vec3(posX, posY, posZ)));
	        	fakeSubtitle.add(new Subtitle("Sound 3", new Vec3(posX, posY, posZ)));
	        }
	        
	        for(Subtitle subtitle : (this.isEditing() ? fakeSubtitle : subtitles)) {
	        	
	            Vec3 Vec34 = subtitle.getLocation().subtract(Vec3).normalize();
	            double d0 = -Vec33.dotProduct(Vec34);
	            double d1 = -Vec31.dotProduct(Vec34);
	            boolean flag = d1 > 0.5D;
	            
	            subtitle.animation.setAnimation(subtitle.isRemove() ? 0 : 1, 17);
	            
	            if(subtitle.animation.getValue() < 0.1 && subtitle.isRemove()) {
	            	subtitle.setDone(true);
	            }
	            
            	int opacity = this.isEditing() ? 255 : (int) (subtitle.animation.getValue() * 255);
            	float animationOffsetY = (((index - 2) * 16) + (this.isEditing() ? 1 : subtitle.animation.getValue()) * 16);
            	
            	if(index == 1) {
            		animationOffsetY = 0;
            	}
            	
            	this.drawCenteredText(subtitle.getString(), subtitleWidth / 2, animationOffsetY + 4, 9, getHudFont(1), this.getFontColor(opacity));

	            if (!flag) {
	                if (d0 > 0.0D) {
	                	this.drawText(">", subtitleWidth - this.getTextWidth("<", 9, getHudFont(1)) - 4.5F, animationOffsetY + 4.5F, 9, getHudFont(1), this.getFontColor(opacity));
	                } else if (d0 < 0.0D) {
	                	this.drawText("<", 4.5F, animationOffsetY + 4.5F, 9, getHudFont(1), this.getFontColor(opacity));
	                }
	            }
	            
	        	index++;
	        }
	        
	        this.setWidth(subtitleWidth);
	        this.setHeight(subtitleHeight);
		}
	}
	
    public void soundPlay(ISound soundIn) {
    	
    	if(subtitles.size() >= maxSetting.getValue()) {
    		return;
    	}
    	
        String s = getSoundName(soundIn.getSoundLocation());
        
        if (s == null) {
        	s = soundIn.getSoundLocation().getResourcePath();
        }
        
        if (s.isEmpty()) {
        	return;
        }
        
        if (!this.subtitles.isEmpty()) {
            for (Subtitle subtitle : subtitles) {
                if (subtitle.getString().equals(s)) {
                	subtitle.refresh(new Vec3(soundIn.getXPosF(), soundIn.getYPosF(), soundIn.getZPosF()));
                    return;
                }
            }
        }
        
        this.subtitles.add(new Subtitle(s, new Vec3(soundIn.getXPosF(), soundIn.getYPosF(), soundIn.getZPosF())));
    }
    
    private String getSoundName(ResourceLocation location) {
        return soundMap.get(location.getResourcePath());
    }
	
    private String read(InputStream stream) throws IOException {
    	
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder stringBuilder = new StringBuilder();
        
        String line;
        
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        
        return stringBuilder.toString();
    }

	public static SoundSubtitlesMod getInstance() {
		return instance;
	}
}
