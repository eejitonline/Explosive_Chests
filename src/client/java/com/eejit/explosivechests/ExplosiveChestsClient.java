package com.eejit.explosivechests;

import com.eejit.explosivechests.renderer.ChestBlockPrimedEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.model.ChestBlockModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

import static com.eejit.explosivechests.ExplosiveChests.CHEST_TNT_ENTITY;

public class ExplosiveChestsClient implements ClientModInitializer {

	public static final EntityModelLayer CHEST_LAYER = new EntityModelLayer(
			Identifier.of("explosive-chests", "chest_tnt_entity"),
			"main"
	);

	@Override
	public void onInitializeClient() {
		System.out.println("Initializing Explosive Chests Client");
		EntityRendererRegistry.register(CHEST_TNT_ENTITY, (context) -> {
			return new ChestBlockPrimedEntityRenderer(context, CHEST_LAYER);
		});

		EntityModelLayerRegistry.registerModelLayer(CHEST_LAYER, ChestBlockModel::getSingleTexturedModelData);
	}
}