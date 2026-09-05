package me.eldodebug.soar;

public class GlideMeta {
    public static final String CLIENT_NAME = "Glide";
    public static final String VERSION_NUMBER = "7.3";
    public static final int VERSION_IDENTIFIER = 7000;
    public static final String API = "https://glideclient.github.io";
    public static final String SITE = "https://glideclient.com";
    public static final String DISCORD_SERVER_MEMBER_COUNT_API = "https://discord.com/api/v9/invites/42PXqKvwxq?with_counts=true";
    public static final Type BUILD_TYPE = Type.DEV;


    public enum Type {
        DEV("Development"), ALPHA("Alpha"), BETA("Beta"), RELEASE("Release"), SPECIAL("Special");

        public final String kind;

        Type(String kind) {
            this.kind = kind;
        }
    }
}
