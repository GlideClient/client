package me.eldodebug.soar.utils.animation.normal.easing;

import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;

public class BackOutAnimation extends Animation {

    private final float overshoot;

    public BackOutAnimation(int ms, double end, float overshoot) {
        super(ms, end);
        this.overshoot = overshoot;
    }

    public BackOutAnimation(int ms, double end, float overshoot, Direction direction) {
        super(ms, end, direction);
        this.overshoot = overshoot;
    }

    @Override
    protected double getEquation(double x) {
        double f = (x / (double) duration) - 1;
        return 1 + (overshoot + 1) * Math.pow(f, 3) + overshoot * Math.pow(f, 2);
    }
}