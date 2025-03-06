package me.eldodebug.soar.management.remote.update;

import com.google.gson.JsonObject;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.network.HttpUtils;

public class Update {

    String updateLink = "https://glideclient.github.io/";
    String updateVersionString = "something is broken lmao";
    int updateBuildID = 0;
    boolean discontinued = false;
    boolean soar8Released = false;

    public void setUpdateLink(String in){
        this.updateLink = in;
    }
    public String getUpdateLink(){
        return updateLink;
    }

    public void setVersionString(String in){
        this.updateVersionString = in;
    }
    public String getVersionString(){
        return updateVersionString;
    }

    public void setBuildID(int in){this.updateBuildID = in;}
    public int getBuildID(){
        return updateBuildID;
    }

    public void setDiscontinued(boolean in){
        this.discontinued = in;
    }
    public boolean getDiscontinued(){
        return discontinued;
    }

    public void setSoar8Released(boolean in){
        this.soar8Released = in;
    }
    public boolean getSoar8Released() {return soar8Released;}


    public void check(){
        try{
            Multithreading.runAsync(this::checkUpdates);
        } catch (Exception ignored){}
    }

    public void checkForUpdates(){
        Glide g = Glide.getInstance();
        if (g.getVersionIdentifier() < this.updateBuildID){
            g.setUpdateNeeded(true);
        }
        g.setSoar8Released(getSoar8Released());
    }

    private void checkUpdates() {
        JsonObject jsonObject = HttpUtils.readJson("https://glideclient.github.io/data/meta/client.json", null);
        if (jsonObject != null) {
            setUpdateLink(JsonUtils.getStringProperty(jsonObject, "updatelink", "https://glideclient.github.io/"));
            setVersionString(JsonUtils.getStringProperty(jsonObject, "latestversionstring", "something is broken lmao"));
            setBuildID(JsonUtils.getIntProperty(jsonObject, "latestversion", 0));
            setDiscontinued(JsonUtils.getBooleanProperty(jsonObject, "discontinued", false));
            setSoar8Released(JsonUtils.getBooleanProperty(jsonObject, "soar8released", false));
            checkForUpdates();
        }
    }

}
