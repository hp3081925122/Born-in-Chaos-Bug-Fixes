package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import java.util.List;
import java.util.function.Predicate;
import net.mcreator.borninchaosv.procedures.DarkWardActivProcedure;
import net.mcreator.borninchaosv.procedures.FrostbittenBladePriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.LivingBombKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinBlockHitProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinDunceDeathTimeIsReachedProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinLordPistolBlockHitProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinLordPistolHitProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinPistolHitProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinStaffKoghdaSnariadPopadaietVSushchnostProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinStaffSirHitProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 将霜噬之刃右键范围技能、暗黑护盾爆发、活体炸弹及南瓜系列弹射物爆炸过程中的 Entity.class 查询收窄为 LivingEntity.class：
 * 1. 修复霜噬之刃释放冰霜新星时对周围所有地面掉落物、经验球、物品展示框和画造成冰冻伤害并销毁战利品的严重缺陷；
 * 2. 在掉落物密集的战斗场景中跳过非生物实体，减少无效距离排序开销。
 */
@Mixin(value = {
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
}, remap = false)
public abstract class TrapAndAoeTargetFixMixin {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(
            method = {
                    "execute",
                    "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelAccessor;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
            ),
            require = 0
    )
    private static List bic$filterLivingEntitiesOnly(
            LevelAccessor world,
            Class entityClass,
            AABB box,
            Predicate predicate
    ) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_TRAP_AND_AOE_NON_LIVING_DAMAGE) && entityClass == Entity.class) {
            return world.getEntitiesOfClass(LivingEntity.class, box, predicate);
        }
        return world.getEntitiesOfClass(entityClass, box, predicate);
    }
}
