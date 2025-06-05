package com.eejit.explosivechests.renderer;

import com.eejit.explosivechests.ChestBlockPrimedEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.EntityRenderState;

@Environment(EnvType.CLIENT)
public class ChestTntEntityRenderState extends EntityRenderState {
    public ChestBlockPrimedEntity entity;
    public float tickDelta;
}
