package com.eejit.explosivechests.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.explosion.ExplosionImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mixin(ExplosionImpl.class)
public abstract class ExplosionImplMixin implements Explosion {

    ExplosionImpl impl = (ExplosionImpl)((Object)this);
    private float power;
    private ServerWorld world;
    private Vec3d pos;
    private ExplosionBehavior behavior;

    @Shadow
    protected abstract ExplosionBehavior makeBehavior(@Nullable Entity entity);


    protected ExplosionImplMixin() {
    }


    @Inject(method = "getBlocksToDestroy", at = @At(value = "HEAD"), cancellable = true)
    private void onGetBlocksToDestroy(CallbackInfoReturnable<List<BlockPos>> cir) {
        if(impl != null) {
            this.power = impl.getPower();
            this.world = impl.getWorld();
            this.pos = impl.getPosition();
            this.behavior = makeBehavior(impl.getEntity());
        }

        cir.cancel();
        Set<BlockPos> set = new HashSet();
        int numRays = Math.min(16000, Math.max(1000, (int)(this.power * 300)));
        System.out.println(numRays);

        for (int i = 0; i < numRays; ++i) {
            double theta = this.world.random.nextDouble() * 2.0 * Math.PI; // azimuth
            double phi = Math.acos(2.0 * this.world.random.nextDouble() - 1.0); // polar angle

            double dx = Math.sin(phi) * Math.cos(theta);
            double dy = Math.cos(phi);
            double dz = Math.sin(phi) * Math.sin(theta);

            float h = this.power * (0.7F + this.world.random.nextFloat() * 0.6F);
            double m = this.pos.x;
            double n = this.pos.y;
            double o = this.pos.z;

            for (float p = 0.3F; h > 0.0F; h -= 0.22500001F) {
                BlockPos blockPos = BlockPos.ofFloored(m, n, o);
                if (!this.world.isInBuildLimit(blockPos)) break;

                BlockState blockState = this.world.getBlockState(blockPos);
                FluidState fluidState = this.world.getFluidState(blockPos);
                Optional<Float> optional = this.behavior.getBlastResistance(this, this.world, blockPos, blockState, fluidState);
                if (optional.isPresent()) {
                    h -= (optional.get() + 0.3F) * 0.3F;
                }

                if (h > 0.0F && this.behavior.canDestroyBlock(this, this.world, blockPos, blockState, h)) {
                    set.add(blockPos);
                }

                m += dx * 0.3;
                n += dy * 0.3;
                o += dz * 0.3;
            }
        }

        cir.setReturnValue(new ObjectArrayList<>(set));
    }
}
