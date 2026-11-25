package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import java.util.List;
import java.util.function.Predicate;
import net.mcreator.borninchaosv.procedures.BonescallerStaffKazhdyiTikVInvientarieProcedure;
import net.mcreator.borninchaosv.procedures.MissionaryHatKazhdyiTikDliaShliemaProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinstaffaKazhdyiTikVInvientarieProcedure;
import net.mcreator.borninchaosv.procedures.SpiderBiteKazhdyiTikVInvientarieProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化唤骨法杖、南瓜法杖、蛛咬剑与传教士帽在背包/头盔栏中的每刻自动驯服扫描：
 * 1. 跳过毫无意义的客户端每刻扫描（TamableAnimal.tame 仅在服务端生效）；
 * 2. 服务端按 4 刻（0.2 秒）节流，并将 Entity.class 全实体扫描收敛为仅扫描未驯服的 TamableAnimal.class。
 */
@Mixin(value = {
        BonescallerStaffKazhdyiTikVInvientarieProcedure.class,
        PumpkinstaffaKazhdyiTikVInvientarieProcedure.class,
        SpiderBiteKazhdyiTikVInvientarieProcedure.class,
        MissionaryHatKazhdyiTikDliaShliemaProcedure.class
}, remap = false)
public abstract class SummonerGearInventoryTickOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$skipClientAndThrottleServer(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EQUIPMENT_INVENTORY_TICKS)) {
            return;
        }
        if (world.isClientSide() || !(entity instanceof Player) || (entity.tickCount & 3) != 0) {
            ci.cancel();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelAccessor;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
            )
    )
    private static List bic$queryUntamedAnimalsOnly(
            LevelAccessor world,
            Class entityClass,
            AABB box,
            Predicate predicate
    ) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EQUIPMENT_INVENTORY_TICKS) && entityClass == Entity.class) {
            return world.getEntitiesOfClass(TamableAnimal.class, box, e -> !e.isTame());
        }
        return world.getEntitiesOfClass(entityClass, box, predicate);
    }
}
