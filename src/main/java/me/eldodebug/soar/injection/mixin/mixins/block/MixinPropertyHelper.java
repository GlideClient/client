package me.eldodebug.soar.injection.mixin.mixins.block;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.injection.interfaces.ICachedHashcode;
import net.minecraft.block.properties.PropertyHelper;

@Mixin(PropertyHelper.class)
public abstract class MixinPropertyHelper implements ICachedHashcode {

    @Shadow @Final private Class<?> valueClass;
    @Shadow @Final private String name;

    @Unique
    private int soar$cachedHashcode;

    /**
     * @reason Pre-computes and caches the immutable PropertyHelper hash code to avoid re-calculating on every blockstate map lookup.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void cacheHashcode(String name, Class<?> valueClass, CallbackInfo ci) {
        this.soar$cachedHashcode = 31 * this.valueClass.hashCode() + this.name.hashCode();
    }

    /**
     * @reason Returns the pre-computed hashcode.
     */
    @Overwrite
    public int hashCode() {
        return this.soar$cachedHashcode;
    }

    @Override
    public int getCachedHashcode() {
        return this.soar$cachedHashcode;
    }
}