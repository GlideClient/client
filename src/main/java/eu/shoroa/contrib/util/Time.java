package eu.shoroa.contrib.util;

public strictfp class Time {
    private static float delta;

    public static void setDelta(float delta) {
        Time.delta = delta;
    }

    public static float getDelta() {
        return delta;
    }
}
