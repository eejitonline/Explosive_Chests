package com.eejit.explosivechests.mixin;

import com.eejit.explosivechests.ChestBlockPrimedEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;


import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.minecraft.block.ChestBlock.getFacing;


@Mixin(ChestBlock.class)
public abstract class ChestBlockMixin extends AbstractChestBlock<ChestBlockEntity> implements Waterloggable {

    ChestBlock thisChest;

    protected ChestBlockMixin(Settings settings, Supplier<BlockEntityType<? extends ChestBlockEntity>> entityTypeRetriever) {
        super(settings, entityTypeRetriever);
        this.thisChest = (ChestBlock)((Object)this);
    }

    private int tntInChest(ChestBlock block, BlockState state, World world, BlockPos pos) {
        return tntInChest(block, state, world, pos, false);
    }

    @Unique
    private int tntInChest(ChestBlock block, BlockState state, World world, BlockPos pos, boolean remove) {
        Inventory inv = block.getInventory(block, state, world, pos, false);
        if(inv != null){
            int count = inv.count(Items.TNT);

            if(remove) {
                for (int slot = 0; slot < inv.size(); slot++) {
                    ItemStack stack = inv.getStack(slot);
                    if(!stack.isEmpty() && stack.getItem() == Items.TNT) {
                        inv.setStack(slot, ItemStack.EMPTY);
                    }
                }
            }
            return count;
        }
        else{
            return 0;
        }
    }

    @Unique
    private boolean primeTnt(World world, BlockPos pos) {
        return primeTnt(world, pos, (LivingEntity)null, false);
    }

    @Unique
    private boolean primeTnt(World world, BlockPos pos, LivingEntity igniter){
        return primeTnt(world, pos, igniter, false);
    }

    @Unique
    private boolean primeTnt(World world, BlockPos pos, @Nullable LivingEntity igniter, boolean randomFuse) {
        int count = tntInChest((ChestBlock)((Object)this), world.getBlockState(pos), world, pos, true);
        Direction facing = getFacing(world.getBlockState(pos));
        System.out.println(facing);
        if (world instanceof ServerWorld serverWorld) {
            if (serverWorld.getGameRules().getBoolean(GameRules.TNT_EXPLODES)) {
                ChestBlockPrimedEntity chestPrimed = new ChestBlockPrimedEntity(world, (double)pos.getX() + (double)0.5F, (double)pos.getY(), (double)pos.getZ() + (double)0.5F, igniter, count, facing);
                if(randomFuse){
                    int i = chestPrimed.getFuse();
                    chestPrimed.setFuse((short)(world.random.nextInt(i / 4) + i / 8));
                }
                world.spawnEntity(chestPrimed);
                if(!randomFuse) {
                    world.playSound((Entity)null, chestPrimed.getX(), chestPrimed.getY(), chestPrimed.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.emitGameEvent(igniter, GameEvent.PRIME_FUSE, pos);
                }
                return true;
            }
        }

        return false;
    }

    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        int count = tntInChest((ChestBlock)((Object)this), state, world, pos);
        if ((!stack.isOf(Items.FLINT_AND_STEEL) && !stack.isOf(Items.FIRE_CHARGE)) || count < 1) {
            return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
        } else {
            if (count > 0 && primeTnt(world, pos, player)) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                Item item = stack.getItem();
                if (stack.isOf(Items.FLINT_AND_STEEL)) {
                    stack.damage(1, player, LivingEntity.getSlotForHand(hand));
                } else {
                    stack.decrementUnlessCreative(1, player);
                }

                player.incrementStat(Stats.USED.getOrCreateStat(item));
            } else if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                if (!serverWorld.getGameRules().getBoolean(GameRules.TNT_EXPLODES)) {
                    player.sendMessage(Text.translatable("block.minecraft.tnt.disabled"), true);
                    return ActionResult.PASS;
                }
            }

            return ActionResult.SUCCESS;
        }
    }

    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            int count = tntInChest((ChestBlock)((Object)this), state, world, pos);
            if (count > 0 && world.isReceivingRedstonePower(pos) && primeTnt(world, pos)) {
                world.removeBlock(pos, false);
            }
        }
    }

    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        int count = tntInChest((ChestBlock)((Object)this), state, world, pos);
        if (count > 0 && world.isReceivingRedstonePower(pos) && primeTnt(world, pos)) {
            world.removeBlock(pos, false);
        }

    }

    @Override
    protected void onExploded(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        System.out.println("Chest destroyed!");
        int count = tntInChest((ChestBlock)((Object)this), world.getBlockState(pos), world, pos);
        System.out.println(count);
        if (world.getGameRules().getBoolean(GameRules.TNT_EXPLODES) && count > 0 && primeTnt(world, pos, explosion.getCausingEntity(), true)) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
        }
        else{
            super.onExploded(state, world, pos, explosion, stackMerger);
        }
    }

    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        if (world instanceof ServerWorld serverWorld) {
            BlockPos blockPos = hit.getBlockPos();
            Entity entity = projectile.getOwner();
            if (projectile.isOnFire() && projectile.canModifyAt(serverWorld, blockPos) && primeTnt(world, blockPos, entity instanceof LivingEntity ? (LivingEntity)entity : null)) {
                world.removeBlock(blockPos, false);
            }
        }

    }
}
