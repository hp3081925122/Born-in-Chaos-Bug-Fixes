package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.OptimizedFlyingPathNavigation;
import net.mcreator.borninchaosv.entity.MrPumpkinControlledEntity;
import net.mcreator.borninchaosv.entity.MrPumpkinEntity;
import net.mcreator.borninchaosv.entity.RestlessSpiritEntity;
import net.mcreator.borninchaosv.entity.ScarletPersecutorEntity;
import net.mcreator.borninchaosv.entity.SenorPumpkinEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 为全部使用三维 FlyingPathNavigation 的实体（重点包括 150 格索敌范围的猩红追猎者）替换为分段优化飞行导航器，
 * 消除单次寻路分配 400 区块缓存及遍历 2400 个立体节点造成的 100ms+ 服务端尖峰。
 */
@Mixin(value = {
        ScarletPersecutorEntity.class,
        MrPumpkinControlledEntity.class,
        MrPumpkinEntity.class,
        RestlessSpiritEntity.class,
        SenorPumpkinEntity.class
})
public abstract class FlyingEntitiesNavigationMixin extends Mob {
    protected FlyingEntitiesNavigationMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "createNavigation", at = @At("HEAD"), cancellable = true)
    private void bic$useOptimizedFlyingNavigation(Level world, CallbackInfoReturnable<PathNavigation> cir) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_FLYING_PATHFINDING)) {
            cir.setReturnValue(new OptimizedFlyingPathNavigation(this, world));
        }
    }
}
