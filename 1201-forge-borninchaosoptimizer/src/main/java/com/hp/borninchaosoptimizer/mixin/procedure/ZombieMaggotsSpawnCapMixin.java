package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.util.BicSummonCapHelper;
import net.mcreator.borninchaosv.procedures.ZombieMaggotsProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 为僵尸死亡产蛆事件过程（ZombieMaggotsProcedure）增加局部同类实体密度上限，避免堆怪时无限爆发蛆虫。
 */
@Mixin(ZombieMaggotsProcedure.class)
public abstract class ZombieMaggotsSpawnCapMixin {
    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;)Lnet/minecraft/world/entity/Entity;",
                    remap = true
            )
    )
    private static <T extends Entity> T bic$capZombieMaggotSpawn(
            EntityType<T> entityType, ServerLevel level, BlockPos pos, MobSpawnType spawnType
    ) {
        return BicSummonCapHelper.spawnWithCap(entityType, level, pos, spawnType);
    }
}
