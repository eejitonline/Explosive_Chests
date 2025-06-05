package com.eejit.explosivechests.mixin;

import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    /*@Shadow
    private World world;  // Make sure you have access to the world

    // Redirect the actual block breaking call:
    @Redirect(
            method = "collectBlocksAndDamageEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;breakBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"
            )
    )
    private boolean redirectBreakBlock(World world, BlockPos pos, boolean drop) {
        // Instead of breaking now, queue it
        MyModClass.destructionQueue.add(pos);
        // Return true to pretend we successfully broke it (or false if you want)
        return true;
    }*/
}
