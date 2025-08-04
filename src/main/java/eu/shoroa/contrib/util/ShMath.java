package eu.shoroa.contrib.util;

public class ShMath {
    public static float interpolate(float a, float b, float delta) {
        return a + (b - a) * delta;
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(min, value));
    }
}
