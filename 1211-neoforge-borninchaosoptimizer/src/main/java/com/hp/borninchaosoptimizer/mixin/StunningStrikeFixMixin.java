package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import java.util.List;
import net.mcreator.borninchaosv.entity.MissionaryRaiderEntity;
import net.mcreator.borninchaosv.entity.MissionerEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.StunningStrikeKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复震慑打击（Stunning Strike）的范围判定缺陷与性能开销：
 * 1. 原代码只排除了 MissionerEntity 和亡灵标签，漏掉了袭击变体 MissionaryRaiderEntity（属于灾厄村民而非亡灵）及施法者自身，导致袭击传教士放技能把自己和友军灾厄村民震晕扣血；
 * 2. 原代码扫描 Entity.class 并对所有实体调用 hurt(9.0f)，导致地面掉落物（ItemEntity）、经验球、画和展示框被技能直接炸毁；
 * 3. 改为直接查询 LivingEntity.class 并省去无意义的流式距离排序分配。
 */
@Mixin(value = StunningStrikeKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class StunningStrikeFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixStunningStrikeTargets(LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EFFECT_TICK_SCANS)) {
            return;
        }
        ci.cancel();
        if (entity == null) {
            return;
        }
        double timer = entity.getPersistentData().getDouble("stunningstrike");
        timer = (timer == 0.0) ? 20.0 : (timer - 1.0);
        entity.getPersistentData().putDouble("stunningstrike", timer);
        if (timer != 0.0) {
            return;
        }
        if (entity instanceof LivingEntity caster) {
            if (!caster.level().isClientSide()) {
                caster.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BLOCK_BREAK, 500, 0, false, false));
            }
            caster.removeEffect(BornInChaosV1ModMobEffects.STUNNING_STRIKE);
        }
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.CLOUDSOFDUST.get(), entity.getX(), entity.getY() + 0.3, entity.getZ(), 20, 2.0, 0.1, 2.0, 0.1);
        }
        boolean casterIsRaider = entity instanceof Raider;
        AABB area = new AABB(
                entity.getX() - 4.5, entity.getY() - 4.5, entity.getZ() - 4.5,
                entity.getX() + 4.5, entity.getY() + 4.5, entity.getZ() + 4.5
        );
        DamageSource damageSource = new DamageSource(world.holderOrThrow(DamageTypes.GENERIC));
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, target ->
                target != entity
                        && !(target instanceof MissionerEntity)
                        && !(target instanceof MissionaryRaiderEntity)
                        && !(casterIsRaider && target instanceof Raider)
                        && !target.getType().is(EntityTypeTags.UNDEAD)
        );
        for (LivingEntity target : targets) {
            if (!target.level().isClientSide()) {
                target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.STUN, 40, 0));
            }
            target.hurt(damageSource, 9.0f);
        }
    }
}
