package me.eldodebug.soar.management.cape;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.management.cape.impl.Cape;
import me.eldodebug.soar.management.cape.impl.CustomCape;
import me.eldodebug.soar.management.cape.impl.NormalCape;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.utils.ImageUtils;
import me.eldodebug.soar.utils.file.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

public class CapeManager {

	private Minecraft mc = Minecraft.getMinecraft();
	
	private ArrayList<Cape> capes = new ArrayList<Cape>();
	private Cape currentCape;
	
	public CapeManager() {
		
		Glide instance = Glide.getInstance();
		FileManager fileManager = instance.getFileManager();
		File customCapeDir = fileManager.getCustomCapeDir();
		File cacheDir = fileManager.getCapeCacheDir();
		
		capes.add(new NormalCape("None", null, null, CapeCategory.ALL));
		
		add("Minecon 2011", "minecon/2011-sample.png", "minecon/2011.png", CapeCategory.MINECON);
		add("Minecon 2012", "minecon/2012-sample.png", "minecon/2012.png", CapeCategory.MINECON);
		add("Minecon 2013", "minecon/2013-sample.png", "minecon/2013.png", CapeCategory.MINECON);
		add("Minecon 2015", "minecon/2015-sample.png", "minecon/2015.png", CapeCategory.MINECON);
		add("Minecon 2016", "minecon/2016-sample.png", "minecon/2016.png", CapeCategory.MINECON);

		add("Canada", "flag/canada-sample.png", "flag/canada.png", CapeCategory.FLAG);
		add("commonwealth", "flag/commonwealth-sample.png", "flag/commonwealth.png", CapeCategory.FLAG);
		add("England", "flag/england-sample.png", "flag/england.png", CapeCategory.FLAG);
		add("Europe", "flag/europe-sample.png", "flag/europe.png", CapeCategory.FLAG);
		add("France", "flag/france-sample.png", "flag/france.png", CapeCategory.FLAG);
		add("Germany", "flag/germany-sample.png", "flag/germany.png", CapeCategory.FLAG);
		add("India", "flag/india-sample.png", "flag/india.png", CapeCategory.FLAG);
		add("Indonesia", "flag/indonesia-sample.png", "flag/indonesia.png", CapeCategory.FLAG);
		add("Italy", "flag/italy-sample.png", "flag/italy.png", CapeCategory.FLAG);
		add("Japan", "flag/japan-sample.png", "flag/japan.png", CapeCategory.FLAG);
		add("Korea", "flag/korean-sample.png", "flag/korean.png", CapeCategory.FLAG);
		add("LGBT", "flag/lgbt-sample.png", "flag/lgbt.png", CapeCategory.FLAG);
		add("NATO", "flag/nato-sample.png", "flag/nato.png", CapeCategory.FLAG);
		add("Scotland", "flag/scotland-sample.png", "flag/scotland.png", CapeCategory.FLAG);
		add("Trans", "flag/trans-sample.png", "flag/trans.png", CapeCategory.FLAG);
		add("Ukraine", "flag/ukraine-sample.png", "flag/ukraine.png", CapeCategory.FLAG);
		add("UN", "flag/un-sample.png", "flag/un.png", CapeCategory.FLAG);
		add("United Kingdom", "flag/united-kingdom-sample.png", "flag/united-kingdom.png", CapeCategory.FLAG);
		add("United States", "flag/united-states-sample.png", "flag/united-states.png", CapeCategory.FLAG);

		add("Blue", "soar/blue-sample.png", "soar/blue.png", CapeCategory.SOAR);
		add("Orange", "soar/orange-sample.png", "soar/orange.png", CapeCategory.SOAR);
		add("Terminal", "soar/terminal-sample.png", "soar/terminal.png", CapeCategory.SOAR);
		add("Candy", "soar/candy-sample.png", "soar/candy.png", CapeCategory.SOAR);
		add("Candy Floss", "soar/candyfloss-sample.png", "soar/candyfloss.png", CapeCategory.SOAR);
		add("Northern Lights", "soar/northenlights-sample.png", "soar/northenlights.png", CapeCategory.SOAR);
		add("Ocean", "soar/ocean-sample.png", "soar/ocean.png", CapeCategory.SOAR);
		add("Parrot", "soar/parrot-sample.png", "soar/parrot.png", CapeCategory.SOAR);
		add("Skylight", "soar/skylight-sample.png", "soar/skylight.png", CapeCategory.SOAR);
		add("Sour Apple", "soar/sourapple-sample.png", "soar/sourapple.png", CapeCategory.SOAR);
		add("Glide", "soar/glide-sample.png", "soar/glide.png", CapeCategory.SOAR);

		add("Aurora", "cartoon/aurora-sample.png", "cartoon/aurora.png", CapeCategory.CARTOON);
		add("Beach Girl", "cartoon/beachgirl-sample.png", "cartoon/beachgirl.png", CapeCategory.CARTOON);
		add("Beach Hut", "cartoon/beachhut-sample.png", "cartoon/beachhut.png", CapeCategory.CARTOON);
		add("Bridgeend", "cartoon/bridgeend-sample.png", "cartoon/bridgeend.png", CapeCategory.CARTOON);
		add("Cat", "cartoon/cat-sample.png", "cartoon/cat.png", CapeCategory.CARTOON);
		add("Cyber Cat", "cartoon/cybercat-sample.png", "cartoon/cybercat.png", CapeCategory.CARTOON);
		add("Decayed", "cartoon/decayed-sample.png", "cartoon/decayed.png", CapeCategory.CARTOON);
		add("Kitty", "cartoon/kitty-sample.png", "cartoon/kitty.png", CapeCategory.CARTOON);
		add("Lost World", "cartoon/lostworld-sample.png", "cartoon/lostworld.png", CapeCategory.CARTOON);
		add("Mountain", "cartoon/mountain-sample.png", "cartoon/mountain.png", CapeCategory.CARTOON);
		add("Stargazing Girl", "cartoon/stargazinggirl-sample.png", "cartoon/stargazinggirl.png", CapeCategory.CARTOON);
		add("Stellagate", "cartoon/stellagate-sample.png", "cartoon/stellagate.png", CapeCategory.CARTOON);
		add("Stray", "cartoon/stray-sample.png", "cartoon/stray.png", CapeCategory.CARTOON);

		add("BreadCat", "misc/breadcat-sample.png", "misc/breadcat.png", CapeCategory.MISC);
		add("Horse", "misc/horse-sample.png", "misc/horse.png", CapeCategory.MISC);
		add("Trans Arch", "misc/transarch-sample.png", "misc/transarch.png", CapeCategory.MISC);

		currentCape = getCapeByName(InternalSettingsMod.getInstance().getCapeConfigName());

		for(File f : customCapeDir.listFiles()) {
			
			if(FileUtils.isImageFile(f)) {
				
				File file = new File(cacheDir, f.getName() + ".png");
				
				if(!file.exists()) {
					
					try {
						BufferedImage image = ImageIO.read(f);
						int width = image.getWidth();
						int height = image.getHeight();
					
						BufferedImage outputImage = ImageUtils.scissor(image, (int) (width * 0.03125), (int) (height * 0.0625), (int) (width * 0.125), (int) (height * 0.46875));
						
						ImageIO.write(ImageUtils.resize(outputImage, 1000, 1700), "png", file);
					} catch (IOException e) {
						GlideLogger.error("Failed to load image", e);
						continue;
					}
				}
				
				if(file.exists()) {
					
					try {
						DynamicTexture cape = new DynamicTexture(ImageIO.read(f));
						
						addCustomCape(f.getName().replace("." + FileUtils.getExtension(f), ""), file,
								mc.getTextureManager().getDynamicTextureLocation(String.valueOf(f.getName().hashCode()), cape), CapeCategory.CUSTOM);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		for(Cape c : capes) {
			
			if(c instanceof NormalCape) {
				
				NormalCape cape = (NormalCape) c;
				
				if(cape.getSample() != null) {
					instance.getNanoVGManager().loadImage(cape.getSample());
				}
			}
			
			if(c instanceof CustomCape) {
				
				CustomCape cape = (CustomCape) c;
				
				if(cape.getSample() != null) {
					instance.getNanoVGManager().loadImage(cape.getSample());
				}
			}
			if(c.getCape() != null) {
				mc.getTextureManager().bindTexture(c.getCape());
			}
		}
	}
	
	private void add(String name, String samplePath, String capePath, CapeCategory category) {
		
		String cosmeticPath = "soar/cosmetics/cape/";
		
		capes.add(new NormalCape(name, new ResourceLocation(cosmeticPath + samplePath), new ResourceLocation(cosmeticPath + capePath), category));
	}
	
	private void addCustomCape(String name, File sample, ResourceLocation cape, CapeCategory category) {
		capes.add(new CustomCape(name, sample, cape, category));
	}
	
	public ArrayList<Cape> getCapes() {
		return capes;
	}
	
	public Cape getCurrentCape() {
		return currentCape;
	}

	public void setCurrentCape(Cape currentCape) {
		this.currentCape = currentCape;
		InternalSettingsMod.getInstance().setCapeConfigName(currentCape.getName());
	}

	public Cape getCapeByName(String name) {
		
		for(Cape c : capes) {
			if(c.getName().equals(name)) {
				return c;
			}
		}
		
		return getCapeByName("None");
	}
	
}
