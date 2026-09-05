package me.eldodebug.soar.gui.gamemenus;

import java.awt.*;

public class ViewMenuButton {
    private final String icon;
    private final Color hoverColor;
    private final Runnable onClick;
    private Direction dir;

    public ViewMenuButton(String icon, Color hoverColor, Direction dir, Runnable onClick) {
        this.icon = icon;
        this.hoverColor = hoverColor;
        this.onClick = onClick;
        this.dir = dir;
    }

    public String getIcon() { return icon; }
    public Color getHoverColor() { return hoverColor; }
    public void execute() { 
        if (onClick != null) onClick.run(); 
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

    public Direction getDir() {
        return dir;
    }

    public enum Direction {
        IN(),
        DONE(),
        OUT();
    }
}