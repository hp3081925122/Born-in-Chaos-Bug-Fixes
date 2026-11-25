package com.hp.borninchaosoptimizer.mixin.combat;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.entity.DoorKnightEntity;
import net.mcreator.borninchaosv.entity.DoorKnightNotDespawnEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherNotDespawnEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.WoodSplitterAxePriUdariePoSushchnostiInstrumientomProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复劈木斧命中过程在创造模式误扣盾牌耐久、打碎盾牌无破碎动画音效且不解除举盾状态，以及不支持 ToolActions.SHIELD_BLOCK 模组盾牌的问题。
 */
@Mixin(value = WoodSplitterAxePriUdariePoSushchnostiInstrumientomProcedure.class, remap = false)
public abstract class WoodSplitterAxeProcedureMixin {
    @Unique
    private static final ResourceLocation BIC_BREAK_WOOD_DOOR =
            ResourceLocation.tryParse("minecraft:entity.zombie.break_wooden_door");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixWoodSplitterShieldBreak(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixDurabilityAndShieldContext)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        SoundEvent breakSound = BIC_BREAK_WOOD_DOOR != null ? ForgeRegistries.SOUND_EVENTS.getValue(BIC_BREAK_WOOD_DOOR) : null;
        if (entity instanceof DoorKnightEntity
                || entity instanceof DoorKnightNotDespawnEntity
                || entity instanceof SkeletonThrasherEntity
                || entity instanceof SkeletonThrasherNotDespawnEntity) {
            if (!living.hasEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK.get())) {
                if (world instanceof Level level && breakSound != null) {
                    if (!level.isClientSide()) {
                        level.playSound(null, BlockPos.containing(x, y, z), breakSound, SoundSource.NEUTRAL, 0.6f, 1.0f);
                    } else {
                        level.playLocalSound(x, y, z, breakSound, SoundSource.NEUTRAL, 0.6f, 1.0f, false);
                    }
                }
                if (world instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, 5, 0.3, 0.5, 0.3, 0.1);
                }
            }
            if (!living.level().isClientSide()) {
                living.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BLOCK_BREAK.get(), 600, 0, false, false));
            }
        }
        if ((entity instanceof Player || entity instanceof Mob) && living.isBlocking() && BicCrossCheckHelper.isShield(living.getUseItem())) {
            if (!living.level().isClientSide()) {
                BicCrossCheckHelper.hurtBlockingShield(living, 500);
            }
            if (world instanceof Level level && breakSound != null) {
                if (!level.isClientSide()) {
                    level.playSound(null, BlockPos.containing(x, y, z), breakSound, SoundSource.NEUTRAL, 0.6f, 1.0f);
                } else {
                    level.playLocalSound(x, y, z, breakSound, SoundSource.NEUTRAL, 0.6f, 1.0f, false);
                }
            }
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, 5, 0.3, 0.5, 0.3, 0.1);
            }
        }
    }
}
