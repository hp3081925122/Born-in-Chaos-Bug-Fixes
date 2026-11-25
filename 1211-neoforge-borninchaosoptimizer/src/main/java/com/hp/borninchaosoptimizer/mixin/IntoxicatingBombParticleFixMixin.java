package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import java.util.List;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.IntoxicatindBombKoghdaSnariadPopadaietVSushchnostProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复迷醉炸弹命中实体时把固定坐标的 36 个爆炸烟雾粒子写在实体遍历循环内部，导致怪群或掉落物堆命中瞬间生成上千粒子造成客户端掉帧的缺陷。
 */
@Mixin(value = IntoxicatindBombKoghdaSnariadPopadaietVSushchnostProcedure.class, remap = false)
public abstract class IntoxicatingBombParticleFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixParticleLoopMultiplication(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_TRAP_AND_AOE_NON_LIVING_DAMAGE)) {
            return;
        }
        ci.cancel();
        if (entity == null) {
            return;
        }
        if (world instanceof Level level && !level.isClientSide()) {
            level.explode(null, x, y, z, 1.0f, Level.ExplosionInteraction.NONE);
        }
        AABB box = new AABB(
                entity.getX() - 4.0, entity.getY() - 4.0, entity.getZ() - 4.0,
                entity.getX() + 4.0, entity.getY() + 4.0, entity.getZ() + 4.0
        );
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, box);
        for (int i = 0, size = targets.size(); i < size; i++) {
            LivingEntity target = targets.get(i);
            if (!target.level().isClientSide()) {
                target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INTOXICATION, 800, 0));
            }
        }
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INTOXICATINGSMOKE.get(), x, y, z, 18, 1.2, 0.2, 1.2, 0.1);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INTOXICATIND_BOMB_PART.get(), x, y, z, 18, 1.2, 0.2, 1.2, 0.1);
        }
        if (!(entity instanceof Player) && entity instanceof LivingEntity directTarget && !directTarget.level().isClientSide()) {
            directTarget.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INTOXICATION, 400, 1));
        }
    }
}
