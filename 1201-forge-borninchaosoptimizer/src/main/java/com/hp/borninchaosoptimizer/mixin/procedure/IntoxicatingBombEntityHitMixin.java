package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.IntoxicatindBombKoghdaSnariadPopadaietVSushchnostProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 修复迷醉炸弹直接命中实体过程（IntoxicatindBombKoghdaSnariadPopadaietVSushchnostProcedure）：
 * 将固定爆炸中心粒子移出实体循环，消除命中怪物/掉落物密集区时单刻喷发近千个烟雾粒子导致的严重掉帧。
 */
@Mixin(value = IntoxicatindBombKoghdaSnariadPopadaietVSushchnostProcedure.class, remap = false)
public abstract class IntoxicatingBombEntityHitMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixBombEntityHitParticles(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityTickClientSideMutations)) {
            return;
        }
        ci.cancel();
        if (entity == null || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.explode(null, x, y, z, 1.0f, Level.ExplosionInteraction.NONE);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INTOXICATINGSMOKE.get(), x, y, z, 18, 1.2, 0.2, 1.2, 0.1);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INTOXICATIND_BOMB_PART.get(), x, y, z, 18, 1.2, 0.2, 1.2, 0.1);

        Vec3 center = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        MobEffect intoxication = BornInChaosV1ModMobEffects.INTOXICATION.get();
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(4.0));
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(intoxication, 800, 0));
        }
        if (!(entity instanceof Player) && entity instanceof LivingEntity directLiving) {
            directLiving.addEffect(new MobEffectInstance(intoxication, 400, 1));
        }
    }
}
