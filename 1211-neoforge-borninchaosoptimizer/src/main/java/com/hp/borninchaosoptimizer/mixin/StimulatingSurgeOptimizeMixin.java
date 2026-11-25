package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import java.util.List;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.StimulatingsurgePriNalozhieniiEffiektaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化亢奋涌动（Stimulating Surge）触发过程：
 * 仅在服务端权威执行，并将全实体 Stream 排序扫描直接收窄为 Monster.class 精确查询。
 */
@Mixin(value = StimulatingsurgePriNalozhieniiEffiektaProcedure.class, remap = false)
public abstract class StimulatingSurgeOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeStimulatingSurge(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EFFECT_TICK_SCANS)) {
            return;
        }
        ci.cancel();
        if (entity == null || world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.playSound(null, BlockPos.containing(x, y, z), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0f, 1.0f);
        double cx = entity.getX();
        double cz = entity.getZ();
        AABB box = new AABB(cx - 3.5, y - 3.5, cz - 3.5, cx + 3.5, y + 3.5, cz + 3.5);
        List<Monster> monsters = serverLevel.getEntitiesOfClass(Monster.class, box);
        for (int i = 0, size = monsters.size(); i < size; i++) {
            monsters.get(i).addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.STIMULATION, 400, 0, false, false));
        }
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.STIMULATINGSMOKE.get(), x, y, z, 19, 1.2, 0.2, 1.2, 0.1);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.STIMULATINGBUBBLES.get(), x, y, z, 19, 1.2, 0.2, 1.2, 0.1);
    }
}
