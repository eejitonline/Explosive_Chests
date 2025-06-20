package com.eejit.explosivechests.mixin;

import com.eejit.explosivechests.DestroyBlocks;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.concurrent.*;

@Mixin(ExplosionImpl.class)
public abstract class ExplosionImplMixin implements Explosion{

    ExplosionImpl impl = (ExplosionImpl)((Object)this);
    @Shadow @Final @Mutable
    private float power;
    @Shadow @Final @Mutable
    private ServerWorld world;
    @Shadow @Final @Mutable
    private Vec3d pos;
    @Shadow @Final @Mutable
    private ExplosionBehavior behavior;

    @Shadow
    protected abstract ExplosionBehavior makeBehavior(@Nullable Entity entity);


    protected ExplosionImplMixin() {
    }


    @Inject(method = "getBlocksToDestroy", at = @At(value = "HEAD"), cancellable = true)
    private void onGetBlocksToDestroy(CallbackInfoReturnable<List<BlockPos>> cir) throws InterruptedException {
        if(impl != null) {
            this.power = impl.getPower();
            this.world = impl.getWorld();
            this.pos = impl.getPosition();
            this.behavior = makeBehavior(impl.getEntity());
        }

        cir.cancel();
        ConcurrentLinkedQueue<List<BlockPos>> allRayPaths = new ConcurrentLinkedQueue<>();
        Set<BlockPos> set = ConcurrentHashMap.newKeySet();
        ThreadLocal<Random> threadRandom = ThreadLocal.withInitial(() -> new Random());
        int numRays = Math.min(16000, Math.max(400, (int)(this.power * 75)));
        int numThreads = 10;
        int start = 0;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        System.out.println(numRays);

        int chunkSize = (numRays-start+1)/numThreads;

        for(int i = 0; i < numThreads; ++i) {
            int chunkStart = start + i*chunkSize;
            int chunkEnd = (i == numThreads - 1) ? numRays : chunkStart + chunkSize - 1;

            executor.submit(() -> {
                Random rand = threadRandom.get();
                
                for (int j = chunkStart; j <= chunkEnd; ++j) {
                    List<BlockPos> path = new ArrayList<>();
                    double theta = rand.nextDouble() * 2.0 * Math.PI; // azimuth
                    double phi = Math.acos(2.0 * rand.nextDouble() - 1.0); // polar angle

                    double dx = Math.sin(phi) * Math.cos(theta);
                    double dy = Math.cos(phi);
                    double dz = Math.sin(phi) * Math.sin(theta);

                    float h = this.power * (0.7F + rand.nextFloat() * 0.6F);
                    double m = this.pos.x;
                    double n = this.pos.y;
                    double o = this.pos.z;

                    for (float p = 0.3F; h > 0.0F; h -= 0.22500001F) {
                        BlockPos blockPos = BlockPos.ofFloored(m, n, o);
                        path.add(blockPos);

                        m += dx * 0.3;
                        n += dy * 0.3;
                        o += dz * 0.3;
                    }

                    allRayPaths.add(path);
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        for (List<BlockPos> path : allRayPaths) {
            float h = this.power * (0.7F + world.random.nextFloat() * 0.6F);; // or recompute if needed
            for (BlockPos blockPos : path) {
                if (!this.world.isInBuildLimit(blockPos)) break;

                BlockState blockState = this.world.getBlockState(blockPos);
                FluidState fluidState = this.world.getFluidState(blockPos);
                Optional<Float> optional = this.behavior.getBlastResistance(this, this.world, blockPos, blockState, fluidState);
                if (optional.isPresent()) {
                    h -= (optional.get() + 0.3F) * 0.3F;
                }

                if (h <= 0.0F) break;

                if (this.behavior.canDestroyBlock(this, this.world, blockPos, blockState, h)) {
                    if (set.add(blockPos)) {
                        DestroyBlocks.blocks.add(blockPos);
                        DestroyBlocks.blockExplosions.add(this);
                    }
                }
            }
        }

        System.out.println("explosion calculated");
        DestroyBlocks.blockCollectionComplete = true;
        cir.setReturnValue(new ObjectArrayList<>(set));
    }

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/ExplosionImpl;destroyBlocks(Ljava/util/List;)V"))
    private void skip(ExplosionImpl instance, List<BlockPos> positions){

    }
}