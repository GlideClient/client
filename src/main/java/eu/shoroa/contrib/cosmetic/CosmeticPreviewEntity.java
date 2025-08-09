package eu.shoroa.contrib.cosmetic;

import eu.shoroa.contrib.fake.FakePlayer;
import eu.shoroa.contrib.fake.FakeWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldInfo;

public class CosmeticPreviewEntity extends FakePlayer {
    public FakeWorld fakeWorld;

    public CosmeticPreviewEntity(Cosmetic cosmetic) {
        super(Minecraft.getMinecraft(), new FakeWorld(new WorldInfo(new NBTTagCompound())));
        fakeWorld = (FakeWorld) worldObj;
    }
}
