package com.eejit.explosivechests.extensions;

import net.minecraft.entity.LivingEntity;

public interface TntEntityExtension {
    void setExplosionPower(float power);
    void setIgniter(LivingEntity igniter);
}
