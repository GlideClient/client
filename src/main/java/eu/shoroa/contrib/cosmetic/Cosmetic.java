package eu.shoroa.contrib.cosmetic;

import net.minecraft.client.model.ModelBase;

public class Cosmetic {
    public String name;
    public ModelBase model;
    public boolean enabled = false;

    public Cosmetic(String name, ModelBase model) {
        this.name = name;
        this.model = model;
    }
}
