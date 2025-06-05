package com.eejit.explosivechests;

import net.fabricmc.api.ModInitializer;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ExplosiveChests implements ModInitializer {
	public static final String MOD_ID = "explosive-chests";
	public static final Queue<BlockPos> destructionQueue = new ConcurrentLinkedQueue<>();

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final RegistryKey<EntityType<?>> CHEST_TNT_ENTITY_KEY = RegistryKey.of(
			RegistryKeys.ENTITY_TYPE,  // This is already RegistryKey<Registry<EntityType<?>>>
			Identifier.of("explosive-chests", "chest_tnt_entity")
	);

	public static final EntityType<ChestBlockPrimedEntity> CHEST_TNT_ENTITY = Registry.register(
			Registries.ENTITY_TYPE,
			Identifier.of("explosive-chests", "chest_tnt_entity"),
			EntityType.Builder
					.create((EntityType<ChestBlockPrimedEntity> type, World world) -> new ChestBlockPrimedEntity(type, world), SpawnGroup.MISC)
					.dimensions(0.98f, 0.98f)
					.build((RegistryKey<EntityType<?>>)(Object)CHEST_TNT_ENTITY_KEY)
	);

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");
	}
}