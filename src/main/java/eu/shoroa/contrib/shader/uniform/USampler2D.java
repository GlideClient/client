/*
 * Nanovg Blur
 * © Shoroa 2025, All Rights Reserved
 */

package eu.shoroa.contrib.shader.uniform;

import org.lwjgl.opengl.GL13;

public class USampler2D extends Uniform {
    private TexSlot slot;
    private int texture;

    public USampler2D(String name, int texture, int slot) {
        super(name);
        this.texture = texture;
        this.slot = new TexSlot(slot, getFunc(slot));
    }

    private int getFunc(int s) {
        int min = GL13.GL_TEXTURE0;
        return min + s;
    }

    public int texture() {
        return texture;
    }

    public TexSlot slot() {
        return slot;
    }

    public static class TexSlot {
        private final int slot;
        private final int glFunc;

        public TexSlot(int slot, int glFunc) {
            this.slot = slot;
            this.glFunc = glFunc;
        }

        public int slot() {
            return slot;
        }

        public int glFunc() {
            return glFunc;
        }
    }
}
