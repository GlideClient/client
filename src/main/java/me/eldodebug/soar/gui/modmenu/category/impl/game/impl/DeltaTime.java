package me.eldodebug.soar.gui.modmenu.category.impl.game.impl;

public class DeltaTime {
    private long lastTime;
    private float deltaTime;

    private static final DeltaTime dt = new DeltaTime();

    public DeltaTime() {
        lastTime = System.nanoTime();
        deltaTime = 0;
    }

    public void update() {
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - lastTime) / 1_000_000_000.0f;
        lastTime = currentTime;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public static DeltaTime getInstance() {
        return dt;
    }

}