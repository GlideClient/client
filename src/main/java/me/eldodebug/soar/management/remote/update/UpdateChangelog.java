package me.eldodebug.soar.management.remote.update;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.GlideMeta;
import me.eldodebug.soar.management.remote.changelog.Changelog;
import me.eldodebug.soar.management.remote.changelog.ChangelogType;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.network.HttpUtils;

import java.util.concurrent.CopyOnWriteArrayList;

public class UpdateChangelog {

	private CopyOnWriteArrayList<Changelog> changelogs = new CopyOnWriteArrayList<Changelog>();

	public UpdateChangelog(Glide instance) {
		Multithreading.runAsync(() -> loadChangelog(instance));
	}
	
	private void loadChangelog(Glide instance) {

		JsonObject jsonObject = HttpUtils.readJson(GlideMeta.API + "/changelogs/versions/" + instance.getUpdateInstance().updateBuildID + ".json", null);


		if(jsonObject != null) {
			
			JsonArray jsonArray = JsonUtils.getArrayProperty(jsonObject, "changelogs");
			
			if(jsonArray != null) {

                for (JsonElement jsonElement : jsonArray) {

                    Gson gson = new Gson();
                    JsonObject changelogJsonObject = gson.fromJson(jsonElement, JsonObject.class);

                    changelogs.add(new Changelog(JsonUtils.getStringProperty(changelogJsonObject, "text", "null"),
                            ChangelogType.getTypeById(JsonUtils.getIntProperty(changelogJsonObject, "type", 999))));
                }
			}
		}
	}

	public CopyOnWriteArrayList<Changelog> getChangelogs() {
		return changelogs;
	}
}
