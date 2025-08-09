package eu.shoroa.contrib.fake;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class FakePlayer extends EntityPlayerSP {

	public FakePlayer(Minecraft mc, World world) {
		this(mc, world, checkNullGameProfile());
	}
	
	public FakePlayer(Minecraft mc, World world, GameProfile gp) {
		super(mc, world, new NetHandlerPlayClient(mc, mc.currentScreen, new FakeNetworkManager(EnumPacketDirection.CLIENTBOUND), gp) {
			@Override
			public NetworkPlayerInfo getPlayerInfo(String p_175104_1_) {
				return new FakeNetworkPlayerInfo(gp);
			}
			@Override
			public NetworkPlayerInfo getPlayerInfo(UUID p_175102_1_) {
				return new FakeNetworkPlayerInfo(gp);
			}
		}, null);

		this.dimension = 0;
		this.movementInput = new MovementInput();
		this.posX = 0;
		this.posY = 0;
		this.posZ = 0;
	}

	@Override
	public float getEyeHeight() {
		return mc.thePlayer.getEyeHeight();
	}
	
	@Override
	public boolean isWearing(EnumPlayerModelParts playerModelParts) {
		return mc.thePlayer.isWearing(playerModelParts);
	}
	
	@Override
	public boolean hasPlayerInfo() {
		return true;
	}

	@Override
	protected NetworkPlayerInfo getPlayerInfo() {
		return new FakeNetworkPlayerInfo(checkNullGameProfile());
	}

	private static GameProfile checkNullGameProfile() {
		if(Minecraft.getMinecraft().getSession() == null || Minecraft.getMinecraft().getSession().getProfile() == null) {
			return new GameProfile(UUID.randomUUID(), "Player");
		}
		return Minecraft.getMinecraft().getSession().getProfile();
	}
}