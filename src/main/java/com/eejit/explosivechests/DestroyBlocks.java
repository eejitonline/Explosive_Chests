package com.eejit.explosivechests;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.eejit.explosivechests.ExplosiveChests.LOGGER;
import static com.eejit.explosivechests.ExplosiveChests.config;

public class DestroyBlocks {

    public static Queue<BlockPos> blocks;
    public static Queue<Explosion> blockExplosions;
    public static boolean blockCollectionComplete = false;

    public DestroyBlocks() {
        this.blocks = new ConcurrentLinkedQueue<>();
        this.blockExplosions = new ConcurrentLinkedQueue<>();
        destroyBlocksOnTick();
    }

    public void destroyBlocksOnTick() {
        ServerTickEvents.START_SERVER_TICK.register((server) -> {
            if(!this.blockExplosions.isEmpty() && this.blockCollectionComplete) {
                int size = blockExplosions.size();
                int ticks = 20;
                int chunkSize = size+1/ticks;
                for(int i = 0; i < chunkSize; i++){
                    BlockPos blockPos = this.blocks.poll();
                    Explosion explosion = this.blockExplosions.poll();
                    if (explosion == null) {
                        LOGGER.warn("Explosion was null for block {}", blockPos);
                        continue; // or break, or handle gracefully
                    }
                    explosion.getWorld().getBlockState(blockPos).onExploded((ServerWorld) explosion.getWorld(), blockPos, explosion, (item, pos) -> {
                        if(config.dropMode == "None"){
                            return;
                        }
                        if(config.dropMode == "All") {
                            Block.dropStack(explosion.getWorld(), pos, item);
                        }
                        if(config.dropMode == "ListedItemsOnly"){
                            if(config.dropItems.contains(Registries.ITEM.getId(item.getItem()).toString())) {
                                Block.dropStack(explosion.getWorld(), pos, item);
                            }
                        }
                    });
                }
                blockCollectionComplete = false;
            }
            else{
                this.blockCollectionComplete = false;
            }
        });
    }
}
