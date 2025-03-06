/*
 * Nanovg Blur
 * © Shoroa 2025, All Rights Reserved
 */

package eu.shoroa.contrib.shader.uniform;

public class UFloat extends Uniform {
    private float value;
    public UFloat(String name, float value) {
        super(name);
        this.value = value;
    }

    public float value() {
        return value;
    }
}
