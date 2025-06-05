package com.eejit.explosivechests.renderer;

import com.eejit.explosivechests.ChestBlockPrimedEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.model.ChestBlockModel;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

@Environment(EnvType.CLIENT)
public class ChestBlockPrimedEntityRenderer extends EntityRenderer<ChestBlockPrimedEntity, ChestTntEntityRenderState>{


    private final Identifier TEXTURE = Identifier.of("minecraft", "textures/entity/chest/normal.png");
    protected final ChestBlockModel model;

    public ChestBlockPrimedEntityRenderer(EntityRendererFactory.Context context, EntityModelLayer layer) {
        super(context);
        this.model = new ChestBlockModel(context.getPart(layer));
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(
                       ChestTntEntityRenderState state,
                       MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers,
                       int light) {

        if (!(state instanceof ChestTntEntityRenderState chestState)) return;
        ChestBlockPrimedEntity entity = chestState.entity;

        matrices.push();

        int j = entity.getFuse();
        if ((float)j -  + 1.0f < 10.0f) {
            float h = 1.0f - ((float)j - state.tickDelta + 1.0f) / 10.0f;
            h = MathHelper.clamp((float)h, (float)0.0f, (float)1.0f);
            h *= h;
            h *= h;
            float k = 1.0f + h * 0.3f;
            matrices.scale(k, k, k);
        }

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getYaw()));

        matrices.translate(-0.5, 0.0, -0.5);


        VertexConsumer consumer = vertexConsumers.getBuffer(this.model.getLayer(this.getTexture(entity)));
        renderBlock(matrices, consumer, light,j / 5 % 2 == 0);

        matrices.pop();
        super.render(state, matrices, vertexConsumers, light);
    }

    public ChestTntEntityRenderState createRenderState() {
        return new ChestTntEntityRenderState();
    }

    public void updateRenderState(ChestBlockPrimedEntity entity, ChestTntEntityRenderState state, float f) {
        super.updateRenderState(entity, state, f);
        state.entity = entity;
        state.tickDelta = f;
    }

    public Identifier getTexture(ChestBlockPrimedEntity entity) {
        return TEXTURE;
    }

    protected void renderBlock(MatrixStack matrices, VertexConsumer vertexConsumers, int light, boolean drawFlash){
        int i = drawFlash ? OverlayTexture.packUv(OverlayTexture.getU(1.0f), 10) : OverlayTexture.DEFAULT_UV;
        this.model.render(matrices, vertexConsumers, light, i);
    }
}
