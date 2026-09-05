package me.eldodebug.soar.utils.render;

public class GridUtils {

    public static float getGridX(int index, int columns, float itemWidth, float padding){
        return (index % columns) * (itemWidth + padding);
    }

    public static float getGridY(int index, int columns, float itemHeight, float padding){
        int row = (index / columns);
        return row * (itemHeight + padding);
    }

    public static int getGridWidth(int columns, int itemWidth, int padding) {
        if (columns <= 0) return 0;
        return columns * itemWidth + (columns - 1) * padding;
    }

    public static int getGridHeight(int totalItems, int columns, int itemHeight, int padding) {
        if (totalItems <= 0 || columns <= 0) return 0;
        int rows = (totalItems + columns - 1) / columns; // ceiling division
        return rows * itemHeight + (rows - 1) * padding;
    }

    public static int getPossibleColumns(float availableWidth, float itemWidth, float padding) {
        return (int) ((availableWidth + padding) / (itemWidth + padding));
    }

    public static int getPossibleRows(float availableHeight, float itemHeight, float padding) {
        return (int) ((availableHeight + padding) / (itemHeight + padding));
    }

}
