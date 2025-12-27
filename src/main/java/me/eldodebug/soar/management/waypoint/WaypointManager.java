package me.eldodebug.soar.management.waypoint;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.ServerUtils;
import net.minecraft.client.Minecraft;

public class WaypointManager {

	private Minecraft mc = Minecraft.getMinecraft();
	
	private ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
	
	public WaypointManager() {
		
		FileManager fileManager = Glide.getInstance().getFileManager();
		File waypointFile = new File(fileManager.getGlideDir(), "Waypoint.json");
		
		fileManager.createFile(waypointFile);
		
		load(waypointFile);
	}
	
	public void load(File file) {
		
		try (FileReader reader = new FileReader(file)) {
			
			Gson gson = new Gson();
			JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
			
			if(jsonObject != null && jsonObject.isJsonObject()) {
				
				JsonArray jsonArray = JsonUtils.getArrayProperty(jsonObject, "Waypoints");
				
				if(jsonArray != null) {
					
					Iterator<JsonElement> iterator = jsonArray.iterator();
					
					while(iterator.hasNext()) {
						
						JsonElement jsonElement = (JsonElement) iterator.next();
						JsonObject wJsonObject = gson.fromJson(jsonElement, JsonObject.class);
						
						waypoints.add(new Waypoint(JsonUtils.getStringProperty(wJsonObject, "World", ""), 
								JsonUtils.getStringProperty(wJsonObject, "Name", ""),
								JsonUtils.getDoubleProperty(wJsonObject, "X", 0),
								JsonUtils.getDoubleProperty(wJsonObject, "Y", 0),
								JsonUtils.getDoubleProperty(wJsonObject, "Z", 0),
								ColorUtils.getColorByInt(JsonUtils.getIntProperty(wJsonObject, "Color", 0))));
					}
				}
			}
		} catch(Exception e) {}
	}
	
	public void save() {
		
		FileManager fileManager = Glide.getInstance().getFileManager();
		File waypointFile = new File(fileManager.getGlideDir(), "Waypoint.json");
		
		try (FileWriter writer = new FileWriter(waypointFile)) {
			
			Gson gson = new Gson();
			JsonObject jsonObject = new JsonObject();
			JsonArray jsonArray = new JsonArray();
			
			for(Waypoint waypoint : waypoints) {
				
				JsonObject wJsonObject = new JsonObject();
				
				wJsonObject.addProperty("World", waypoint.getWorld());
				wJsonObject.addProperty("Name", waypoint.getName());
				wJsonObject.addProperty("X", waypoint.getX());
				wJsonObject.addProperty("Y", waypoint.getY());
				wJsonObject.addProperty("Z", waypoint.getZ());
				wJsonObject.addProperty("Color", waypoint.getColor().getRGB());
				
				jsonArray.add(wJsonObject);
			}
			
			jsonObject.add("Waypoints", jsonArray);
			
			gson.toJson(jsonObject, writer);
		} catch(Exception e) {}
	}
	
	public String getWorld() {
		
		if(ServerUtils.isJoinServer()) {
			return "server-" + ServerUtils.getServerIP() + "-" + mc.theWorld.provider.getDimensionId();
		}
		
		return "local-" + mc.getIntegratedServer().method_2028() + "-" + mc.theWorld.provider.getDimensionId();
	}

	public ArrayList<Waypoint> getWaypoints() {
		return waypoints;
	}
	
	public void addWaypoint(String name, double x, double y, double z, Color color) {
		waypoints.add(new Waypoint(getWorld(), name, x, y, z, color));
	}
}
