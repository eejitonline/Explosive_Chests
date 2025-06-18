package com.eejit.explosivechests;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.LinkedList;
import java.util.Queue;

public class DestroyBlocks {

    public static Queue<BlockPos> blocks;
    public static Queue<Explosion> blockExplosions;
    public static boolean blockCollectionComplete = false;

    public DestroyBlocks() {
        this.blocks = new LinkedList<>();
        this.blockExplosions = new LinkedList<>();
        destroyBlocksOnTick();
    }

    public void destroyBlocksOnTick() {
        ServerTickEvents.START_SERVER_TICK.register((server) -> {
            if(!this.blocks.isEmpty() && this.blockCollectionComplete) {
                for(int i = 0; i < 200; i++){
                    BlockPos blockPos = this.blocks.poll();
                    Explosion explosion = this.blockExplosions.poll();
                    explosion.getWorld().getBlockState(blockPos).onExploded((ServerWorld) explosion.getWorld(), blockPos, explosion, (item, pos) -> {
                        Block.dropStack(explosion.getWorld(), pos, item);
                    });
                }
            }
            else{
                this.blockCollectionComplete = true;
            }
        });
    }
}
