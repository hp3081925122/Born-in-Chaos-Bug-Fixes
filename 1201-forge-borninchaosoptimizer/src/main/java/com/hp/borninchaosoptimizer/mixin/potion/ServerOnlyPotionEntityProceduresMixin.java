package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.BoneChillingKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.BoneFractureKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.IceBarrierKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.SweetMadnessKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.SweetMadnessPriIstiechieniiEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.SweetMadnessPriNalozhieniiEffiektaProcedure;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截纯服务端状态修改型药水过程在客户端侧的误执行，避免客户端提前 removeEffect、setHealth 或产生独立随机 NBT 分支。
 */
@Mixin(value = {
        BoneChillingKazhdyiTikVoVriemiaEffiektaProcedure.class,
        IceBarrierKazhdyiTikVoVriemiaEffiektaProcedure.class,
        BoneFractureKazhdyiTikVoVriemiaEffiektaProcedure.class,
        SweetMadnessPriNalozhieniiEffiektaProcedure.class,
        SweetMadnessKazhdyiTikVoVriemiaEffiektaProcedure.class,
        SweetMadnessPriIstiechieniiEffiektaProcedure.class
}, remap = false)
public abstract class ServerOnlyPotionEntityProceduresMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$skipOnClient(Entity entity, CallbackInfo ci) {
        if (entity == null || !entity.level().isClientSide()) {
            return;
        }
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizePotionAuraAndStateTicks)) {
            return;
        }
        ci.cancel();
    }
}
