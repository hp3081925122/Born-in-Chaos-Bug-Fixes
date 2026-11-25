package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.procedures.LifestealerAtackProcedure;
import net.mcreator.borninchaosv.procedures.LifestealerPriObnovlieniiTikaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadTicProcedure;
import net.mcreator.borninchaosv.procedures.PhantomCreeperKoghdaSushchnostRanienaProcedure;
import net.mcreator.borninchaosv.procedures.SerPumpkinheadGProcedure;
import net.mcreator.borninchaosv.procedures.SupremeBonecallerPriObnovlieniiTaktaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.WithoutHorsePProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 防止窃命者伪装形态、至高唤骨者、南瓜领主、南瓜爵士与幻影苦力怕在同一刻因多段伤害或攻击+刻更新竞争重复触发变身/分裂，导致刷出双倍二阶段首领。
 */
@Mixin(value = {
        LifestealerAtackProcedure.class,
        LifestealerPriObnovlieniiTikaSushchnostiProcedure.class,
        SupremeBonecallerPriObnovlieniiTaktaSushchnostiProcedure.class,
        LordPumpkinheadTicProcedure.class,
        SerPumpkinheadGProcedure.class,
        WithoutHorsePProcedure.class,
        PhantomCreeperKoghdaSushchnostRanienaProcedure.class
}, remap = false)
public abstract class BossPhaseDuplicateSpawnFixMixin {
    @Unique
    private static final String BIC_TRANSFORMED_KEY = "bic_phase_transformed";

    @Inject(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void bic$guardTickTransform(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_BOSS_AND_MINION_DUPLICATE_SPAWN)) {
            return;
        }
        if (world.isClientSide() || entity == null || entity.isRemoved() || entity.getPersistentData().getBoolean(BIC_TRANSFORMED_KEY)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void bic$guardHurtSplit(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_BOSS_AND_MINION_DUPLICATE_SPAWN)) {
            return;
        }
        if (world.isClientSide() || entity == null || entity.isRemoved() || entity.getPersistentData().getBoolean(BIC_TRANSFORMED_KEY)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void bic$guardLifestealerAttackTransform(
            Event event,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            Entity sourceentity,
            CallbackInfo ci
    ) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_BOSS_AND_MINION_DUPLICATE_SPAWN)) {
            return;
        }
        if (world.isClientSide() || sourceentity == null || sourceentity.isRemoved() || sourceentity.getPersistentData().getBoolean(BIC_TRANSFORMED_KEY)) {
            ci.cancel();
        }
    }

    @Redirect(
            method = {
                    "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
                    "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
                    "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;discard()V"),
            require = 0
    )
    private static void bic$markTransformedOnDiscard(Entity entity) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_BOSS_AND_MINION_DUPLICATE_SPAWN)) {
            entity.getPersistentData().putBoolean(BIC_TRANSFORMED_KEY, true);
        }
        entity.discard();
    }
}
