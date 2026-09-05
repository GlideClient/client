package me.eldodebug.soar.injection.mixin.mixins.block;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.world.World;

@Mixin(BlockRedstoneTorch.class)
public class MixinBlockRedstoneTorch {

	@Shadow
    private static Map<World, List<?>> toggles;

    /**
     *  prevent world memory leaks.
     */
    @Inject(method = "<clinit>", at = @At(value="TAIL"))
    private static void fixMemory(CallbackInfo ci) {
    	toggles = new WeakHashMap<>();
    }
}
