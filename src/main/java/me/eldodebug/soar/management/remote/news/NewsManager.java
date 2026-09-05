package me.eldodebug.soar.management.remote.news;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.eldodebug.soar.GlideMeta;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.network.HttpUtils;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class NewsManager {

	private final CopyOnWriteArrayList<News> news = new CopyOnWriteArrayList<News>();
	
	public NewsManager() {
		Multithreading.runAsync(this::loadNews);
	}
	
	private void loadNews() {
		
		JsonObject jsonObject = HttpUtils.readJson(GlideMeta.API + "/data/news/news.json", null);
		
		if(jsonObject != null) {
			
			JsonArray jsonArray = JsonUtils.getArrayProperty(jsonObject, "news");
			
			if(jsonArray != null) {

				Iterator<JsonElement> iterator = jsonArray.iterator();

				while(iterator.hasNext()) {


					JsonElement jsonElement = (JsonElement) iterator.next();
                    Gson gson = new Gson();
                    JsonObject changelogJsonObject = gson.fromJson(jsonElement, JsonObject.class);

                    news.add(new News(JsonUtils.getStringProperty(changelogJsonObject, "title", "null"), JsonUtils.getStringProperty(changelogJsonObject, "subtitle", "null"), JsonUtils.getStringProperty(changelogJsonObject, "body", "null")));
                }
			}
		}
	}

	public CopyOnWriteArrayList<News> getNews() {
		return news;
	}
}
