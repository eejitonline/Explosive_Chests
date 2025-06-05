package com.eejit.explosivechests;

import com.eejit.explosivechests.extensions.TntEntityExtension;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.eejit.explosivechests.ExplosiveChests.CHEST_TNT_ENTITY;

public class ChestBlockPrimedEntity extends TntEntity {

    public Direction facing = Direction.NORTH;

    public ChestBlockPrimedEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter, int tntCount, Direction facing) {
        this(CHEST_TNT_ENTITY, world);
        if ((Object)this instanceof TntEntityExtension ext) {
            this.setPosition(x, y, z);
            this.setFuse(80);
            double d = world.random.nextDouble() * (double)((float)Math.PI * 2F);
            this.setVelocity(-Math.sin(d) * 0.02, (double)0.2F, -Math.cos(d) * 0.02);
            this.facing = facing;
            float direction = switch (facing){
                case NORTH -> 90f;
                case SOUTH -> 270f;
                case WEST -> 180f;
                case EAST -> 0f;
                default -> 0f;};
            this.setYaw(direction);
            ext.setIgniter(igniter);
            ext.setExplosionPower(4.0F*tntCount);
        }
    }

    public ChestBlockPrimedEntity(EntityType<? extends ChestBlockPrimedEntity> type, World world) {
        super(type, world);
    }
}
