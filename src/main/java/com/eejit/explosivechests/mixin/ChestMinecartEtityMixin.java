package com.eejit.explosivechests.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChestMinecartEntity.class)
public abstract class ChestMinecartEtityMixin extends StorageMinecartEntity {
    private static final byte PRIME_TNT_STATUS = 10;
    private static final String EXPLOSION_POWER_NBT_KEY = "explosion_power";
    private static final String EXPLOSION_SPEED_FACTOR_NBT_KEY = "explosion_speed_factor";
    private static final String FUSE_NBT_KEY = "fuse";
    private static final float DEFAULT_EXPLOSION_POWER = 4.0F;
    private static final float DEFAULT_EXPLOSION_SPEED_FACTOR = 1.0F;
    private static final int DEFAULT_FUSE_TICKS = -1;
    @Nullable
    private DamageSource damageSource;
    private int fuseTicks = -1;
    private float explosionPower = 4.0F;
    private float explosionSpeedFactor = 1.0F;

    protected ChestMinecartEtityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    public void tick() {
        super.tick();
        if (this.fuseTicks > 0) {
            --this.fuseTicks;
            this.getWorld().addParticleClient(ParticleTypes.SMOKE, this.getX(), this.getY() + (double)0.5F, this.getZ(), (double)0.0F, (double)0.0F, (double)0.0F);
        } else if (this.fuseTicks == 0) {
            this.explode(this.damageSource, this.getVelocity().horizontalLengthSquared());
        }

        if (this.horizontalCollision) {
            double d = this.getVelocity().horizontalLengthSquared();
            if (d >= (double)0.01F) {
                this.explode(d);
            }
        }

    }

    protected void explode(double power) {
        this.explode((DamageSource)null, power);
    }

    protected void explode(@Nullable DamageSource damageSource, double power) {
        Inventory inv = (Inventory)this.getInventory();
        if(inv != null) {
            int count = inv.count(Items.TNT);
            if(count > 0) {
                this.explosionPower = explosionPower * count;
            }
            else{
                return;
            }
        }

        World var5 = this.getWorld();
        if (var5 instanceof ServerWorld serverWorld) {
            if (serverWorld.getGameRules().getBoolean(GameRules.TNT_EXPLODES)) {
                double d = Math.min(Math.sqrt(power), (double)5.0F);
                serverWorld.createExplosion(this, damageSource, (ExplosionBehavior)null, this.getX(), this.getY(), this.getZ(), (float)((double)this.explosionPower + (double)this.explosionSpeedFactor * this.random.nextDouble() * (double)1.5F * d), false, World.ExplosionSourceType.TNT);
                this.discard();
            } else if (this.isPrimed()) {
                this.discard();
            }
        }

    }

    @Unique
    public boolean isPrimed() {
        return this.fuseTicks > -1;
    }

    public boolean handleFallDamage(double fallDistance, float damagePerDistance, DamageSource damageSource) {
        if (fallDistance >= (double)3.0F) {
            double d = fallDistance / (double)10.0F;
            this.explode(d * d);
        }

        return super.handleFallDamage(fallDistance, damagePerDistance, damageSource);
    }
}
