package com.eejit.explosivechests;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ExplosiveChests implements ModInitializer {
	DestroyBlocks destroyBlocks;
	public static final String MOD_ID = "explosive-chests";
	public static final Queue<BlockPos> destructionQueue = new ConcurrentLinkedQueue<>();

	public static ModConfig config;

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
		this.destroyBlocks = new DestroyBlocks();
		configSetup();
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve("explosivechests/config.json");
		config = ConfigLoader.loadConfig(configPath);
	}

	public static void configSetup(){
		Path configDir = FabricLoader.getInstance().getConfigDir();
		Path configFile = configDir.resolve("explosivechests/config.json");

		if (Files.notExists(configFile)) {
			try (InputStream in = ExplosiveChests.class.getClassLoader().getResourceAsStream("assets/explosive-chests/config.json");) {
				if (in == null) {
					System.err.println("Default config resource not found!");
					return;
				}
				Files.createDirectories(configFile.getParent());
				Files.copy(in, configFile);
				System.out.println("Default config copied to " + configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
}