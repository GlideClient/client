/*
 * Nanovg Blur
 * © Shoroa 2025, All Rights Reserved
 */

package eu.shoroa.contrib.shader.uniform;

import java.nio.FloatBuffer;

public abstract class Uniform {
    protected String name;

    public Uniform(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public static UInt makeInt(String name, int value) {
        return new UInt(name, value);
    }

    public static UFloat makeFloat(String name, float value) {
        return new UFloat(name, value);
    }

    public static UFloatBuffer makeFloatBuffer(String name, FloatBuffer buffer) {
        return new UFloatBuffer(name, buffer);
    }

    public static USampler2D makeSampler2D(String name, int texture, int slot) {
        return new USampler2D(name, texture, slot);
    }

    public static UVec2 makeVec2(String name, float x, float y) {
        return new UVec2(name, x, y);
    }

    public static UVec3 makeVec3(String name, float x, float y, float z) {
        return new UVec3(name, x, y, z);
    }

    public static UVec4 makeVec4(String name, float x, float y, float z, float w) {
        return new UVec4(name, x, y, z, w);
    }
}

