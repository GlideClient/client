package me.eldodebug.soar.management.remote.discord;

import com.google.gson.JsonObject;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.network.HttpUtils;

/**
 * I know what people are like so il explain this class .
 *  This class checks the discord api to see how many members the glide server has
 *  you can see this within the ui from the home screen in the mod menu
 */
public class DiscordStats {
    int membersCount = -1;
    int membersOnline = -1;

    public void setMemberCount(int in){
        this.membersCount = in;
    }
    public int getMemberCount(){return membersCount;}
    public void setMemberOnline(int in){
        this.membersOnline = in;
    }
    public int getMemberOnline(){return membersOnline;}

    public void check(){
        Multithreading.runAsync(this::checkDiscordValues);
    }
    public void checkDiscordValues(){
        DiscordStats discordStats = Glide.getInstance().getDiscordStats();
        JsonObject jsonObject = HttpUtils.readJson("https://discord.com/api/v9/invites/42PXqKvwxq?with_counts=true", null);

        if(jsonObject != null) {
            discordStats.setMemberCount(JsonUtils.getIntProperty(jsonObject, "approximate_member_count", -1));
            discordStats.setMemberOnline(JsonUtils.getIntProperty(jsonObject, "approximate_presence_count", -1));
        }
    }
}
