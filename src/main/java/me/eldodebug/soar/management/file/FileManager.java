package me.eldodebug.soar.management.file;

import java.io.File;
import java.io.IOException;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.logger.GlideLogger;
import net.minecraft.client.Minecraft;

public class FileManager {

	private File soarDir, profileDir, cacheDir, screenshotDir, musicDir;
	
	public FileManager() {
		
		soarDir = new File(Minecraft.getMinecraft().mcDataDir, "glide");
		profileDir = new File(soarDir, "profile");
		cacheDir = new File(soarDir, "cache");
		musicDir = new File(soarDir, "music");
		screenshotDir = new File(soarDir, "screenshots");
		
		createDir(soarDir);
		createDir(profileDir);
		createDir(cacheDir);
		createDir(musicDir);
		createDir(screenshotDir);
		
		createVersionFile();
	}
	
	private void createVersionFile() {
		
		File versionDir = new File(cacheDir, "version");
		
		createDir(versionDir);
		createFile(new File(versionDir, Glide.getInstance().getVersionIdentifier() + ".tmp"));
	}
	
	public void createDir(File file) {
		file.mkdir();
	}
	
	public void createFile(File file) {
		try {
			file.createNewFile();
		} catch (IOException e) {
			GlideLogger.error("Failed to create file " + file.getName(), e);
		}
	}

	public File getScreenshotDir() {
		return screenshotDir;
	}

	public File getGlideDir() {
		return soarDir;
	}

	public File getProfileDir() {
		return profileDir;
	}

	public File getCacheDir() {
		return cacheDir;
	}

	public File getMusicDir() {
		return musicDir;
	}
}
