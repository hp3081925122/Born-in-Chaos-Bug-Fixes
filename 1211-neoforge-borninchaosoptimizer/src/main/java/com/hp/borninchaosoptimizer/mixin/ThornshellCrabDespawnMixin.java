package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.ThornshellCrabEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.procedures.ThornshellCrabpriProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复野生刺壳蟹（ThornshellCrabEntity）自然刷新却在构造器强制 setPersistenceRequired() 且 removeWhenFarAway 恒返回 false，
 * 导致长期探索海岸后占满原版 CREATURE（10 只）动物上限的缺陷。
 * 玩家喂食、繁殖、命名或拴绳后的刺壳蟹仍永久保留，不会被远距离清除。
 */
@Mixin(ThornshellCrabEntity.class)
public abstract class ThornshellCrabDespawnMixin extends Animal {
    @Unique
    private static final String BIC_PLAYER_INTERACTED_KEY = "bic_player_interacted";

    protected ThornshellCrabDespawnMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/borninchaosv/entity/ThornshellCrabEntity;setPersistenceRequired()V"
            )
    )
    private void bic$skipWildConstructorPersistence(ThornshellCrabEntity instance) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.ALLOW_WILD_THORNSHELL_CRAB_DESPAWN)) {
            instance.setPersistenceRequired();
        }
    }

    @Inject(method = "removeWhenFarAway", at = @At("HEAD"), cancellable = true)
    private void bic$allowWildCrabDespawn(double distanceToClosestPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.ALLOW_WILD_THORNSHELL_CRAB_DESPAWN)) {
            return;
        }
        if (this.isPersistenceRequired()
                || this.hasCustomName()
                || this.isLeashed()
                || this.isBaby()
                || this.isInLove()
                || this.getPersistentData().getBoolean(BIC_PLAYER_INTERACTED_KEY)) {
            cir.setReturnValue(false);
        } else {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getBreedOffspring", at = @At("RETURN"))
    private void bic$markBredOffspringPersistent(ServerLevel serverWorld, AgeableMob ageable, CallbackInfoReturnable<AgeableMob> cir) {
        AgeableMob child = cir.getReturnValue();
        if (child instanceof ThornshellCrabEntity crab) {
            crab.getPersistentData().putBoolean(BIC_PLAYER_INTERACTED_KEY, true);
            crab.setPersistenceRequired();
        }
    }

    /**
     * 修复 1.21.1 原模组迁移时丢失 ThornshellCrabpriProcedure 生成条件，导致刺壳蟹在任意草地群系泛滥且无视 THORNSHELL_CRAB_SPAWN 规则开关的问题。
     */
    @Inject(method = "init", at = @At("HEAD"), cancellable = true, remap = false)
    private static void bic$restoreThornshellCrabSpawnPlacement(RegisterSpawnPlacementsEvent event, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_SPAWN_CONDITIONS)) {
            return;
        }
        ci.cancel();
        event.register(
                BornInChaosV1ModEntities.THORNSHELL_CRAB.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, world, reason, pos, random) -> ThornshellCrabpriProcedure.execute(
                        world, pos.getX(), pos.getY(), pos.getZ()),
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }
}
