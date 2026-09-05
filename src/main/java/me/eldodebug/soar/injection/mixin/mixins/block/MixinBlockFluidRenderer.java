package me.eldodebug.soar.injection.mixin.mixins.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.renderer.BlockFluidRenderer;

@Mixin(BlockFluidRenderer.class)
public class MixinBlockFluidRenderer {

    /**
     * @reason Removes the 0.001F fluid rendering offset to fix texture stitching/seams on liquid surfaces.
     */
    @ModifyConstant(method = "renderFluid", constant = @Constant(floatValue = 0.001F))
    private float fixFluidStitching(float original) {
        return 0.0F;
    }
}