package com.hp.borninchaosoptimizer.mixin;

import net.mcreator.borninchaosv.entity.BabySkeletonEntity;
import net.mcreator.borninchaosv.entity.BarrelZombieEntity;
import net.mcreator.borninchaosv.entity.DoorKnightEntity;
import net.mcreator.borninchaosv.entity.DoorKnightNotDespawnEntity;
import net.mcreator.borninchaosv.entity.FallenChaosKnightEntity;
import net.mcreator.borninchaosv.entity.FirelightNotDespawnEntity;
import net.mcreator.borninchaosv.entity.ScarletPersecutorEntity;
import net.mcreator.borninchaosv.entity.SiameseSkeletonsleftEntity;
import net.mcreator.borninchaosv.entity.SiameseSkeletonsrightEntity;
import net.mcreator.borninchaosv.entity.ZombieClownNotDespawnEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 取消生于混沌 10 种生物 dropCustomDeathLoot 中的硬编码掉落，
 * 对应掉落已统一迁移/合并至标准 JSON 实体战利品表（data/born_in_chaos_v1/loot_table/entities/*.json），
 * 使 JEI/JER/EMI Loot 能完整展示掉落并支持抢夺附魔与数据包覆盖。
 */
@Mixin({
        BabySkeletonEntity.class,
        BarrelZombieEntity.class,
        DoorKnightEntity.class,
        DoorKnightNotDespawnEntity.class,
        FallenChaosKnightEntity.class,
        FirelightNotDespawnEntity.class,
        ScarletPersecutorEntity.class,
        SiameseSkeletonsleftEntity.class,
        SiameseSkeletonsrightEntity.class,
        ZombieClownNotDespawnEntity.class
})
public abstract class EntityLootDataDrivenMixin extends Mob {
    protected EntityLootDataDrivenMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "dropCustomDeathLoot", at = @At("HEAD"), cancellable = true)
    private void bic$useDataDrivenEntityLoot(ServerLevel serverLevel, DamageSource source, boolean recentlyHitIn, CallbackInfo ci) {
        super.dropCustomDeathLoot(serverLevel, source, recentlyHitIn);
        ci.cancel();
    }
}
