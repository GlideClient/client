/*
 * Nanovg Blur
 * © Shoroa 2025, All Rights Reserved
 */

package eu.shoroa.contrib.shader.uniform;

import java.nio.FloatBuffer;

public class UFloatBuffer extends Uniform {
    private FloatBuffer buffer;
    public UFloatBuffer(String name, FloatBuffer buffer) {
        super(name);
        this.buffer = buffer;
    }

    public FloatBuffer buffer() {
        return buffer;
    }
}
