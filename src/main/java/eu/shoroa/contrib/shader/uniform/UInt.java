/*
 * Nanovg Blur
 * © Shoroa 2025, All Rights Reserved
 */

package eu.shoroa.contrib.shader.uniform;

public class UInt extends Uniform {
    private int value;

    public UInt(String name, int value) {
        super(name);
        this.value = value;
    }

    public int value() {
        return value;
    }
}
