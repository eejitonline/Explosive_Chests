package com.eejit.explosivechests.mixin;

import com.eejit.explosivechests.extensions.TntEntityExtension;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin implements TntEntityExtension {

    @Shadow
    private float explosionPower;
    @Shadow
    private LivingEntity causingEntity;

    @Unique
    public void setExplosionPower(float power){
        this.explosionPower = power;
    }

    @Unique
    public void setIgniter(LivingEntity igniter){
        this.causingEntity = igniter;
    }
}
