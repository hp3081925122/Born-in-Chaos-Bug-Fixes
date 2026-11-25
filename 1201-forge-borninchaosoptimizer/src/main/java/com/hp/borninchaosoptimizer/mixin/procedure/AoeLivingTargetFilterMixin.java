package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;

/**
 * 将霜噬之刃冰霜新星、暗黑护盾爆发、活体炸弹及南瓜系列弹射物爆炸过程的 Entity.class 查询收窄为 LivingEntity.class，
 * 修复玩家释放技能或引爆时瞬间摧毁周围地面战利品掉落物、经验球、物品展示框和画的缺陷。
 */
@Mixin({
        FrostbittenBladePriShchielchkiePKMProcedure.class,
        DarkWardActivProcedure.class,
        LivingBombKazhdyiTikVoVriemiaEffiektaProcedure.class,
        PumpkinBlockHitProcedure.class,
        PumpkinDunceDeathTimeIsReachedProcedure.class,
        PumpkinLordPistolBlockHitProcedure.class,
        PumpkinLordPistolHitProcedure.class,
        PumpkinPistolHitProcedure.class,
        PumpkinStaffKoghdaSnariadPopadaietVSushchnostProcedure.class,
        PumpkinStaffSirHitProcedure.class
})
public abstract class AoeLivingTargetFilterMixin {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(
            method = "execute*",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelAccessor;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
                    remap = true
            ),
            require = 0
    )
    private static List bic$filterLivingTargetsOnly(
            LevelAccessor world,
            Class entityClass,
            AABB box,
            Predicate predicate
    ) {
        if (entityClass == Entity.class
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixNonLivingAoeAndTrapDamage)) {
            return world.getEntitiesOfClass(LivingEntity.class, box, predicate);
        }
        return world.getEntitiesOfClass(entityClass, box, predicate);
    }
}
