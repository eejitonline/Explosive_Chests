package com.eejit.explosivechests.mixin;

import net.minecraft.entity.TntEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ExampleMixin {
	@Inject(at = @At("HEAD"), method = "loadWorld")
	private void init(CallbackInfo info) {
		// This code is injected into the start of MinecraftServer.loadWorld()V
		System.out.println("Hello World!");
	}

	@Unique
	public void onDestroyedByExplosion(ServerWorld world, BlockPos pos, Explosion explosion) {
		if (world.getGameRules().getBoolean(GameRules.TNT_EXPLODES)) {
			TntEntity tntEntity = new TntEntity(world, (double)pos.getX() + (double)0.5F, (double)pos.getY(), (double)pos.getZ() + (double)0.5F, explosion.getCausingEntity());
			int i = tntEntity.getFuse();
			tntEntity.setFuse((short)(world.random.nextInt(i / 4) + i / 8));
			world.spawnEntity(tntEntity);
		}
	}
}