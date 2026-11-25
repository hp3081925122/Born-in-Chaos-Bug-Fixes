package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.procedures.BoneChillingKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.BoneFractureKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.DetonationKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.IceBarrierKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.LifestealKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.LivingBombKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.MinionSummonKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.SoulStratificationKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.SweetMadnessKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.SweetMadnessPriIstiechieniiEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.SweetMadnessPriNalozhieniiEffiektaProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截纯服务端状态修改型药水过程在客户端侧的误执行，避免客户端提前 removeEffect、setHealth、hurt 或产生独立随机 NBT 分支。
 */
@Mixin(value = {
        BoneChillingKazhdyiTikVoVriemiaEffiektaProcedure.class,
        BoneFractureKazhdyiTikVoVriemiaEffiektaProcedure.class,
        IceBarrierKazhdyiTikVoVriemiaEffiektaProcedure.class,
        SweetMadnessPriNalozhieniiEffiektaProcedure.class,
        SweetMadnessKazhdyiTikVoVriemiaEffiektaProcedure.class,
        SweetMadnessPriIstiechieniiEffiektaProcedure.class,
        DetonationKazhdyiTikVoVriemiaEffiektaProcedure.class,
        LifestealKazhdyiTikVoVriemiaEffiektaProcedure.class,
        LivingBombKazhdyiTikVoVriemiaEffiektaProcedure.class,
        MinionSummonKazhdyiTikVoVriemiaEffiektaProcedure.class,
        SoulStratificationKazhdyiTikVoVriemiaEffiektaProcedure.class
}, remap = false)
public abstract class ServerOnlyPotionProceduresMixin {
    @Inject(
            method = "execute(Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void bic$skipEntityPotionOnClient(Entity entity, CallbackInfo ci) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EFFECT_TICK_SCANS)
                && (entity == null || entity.level().isClientSide())) {
            ci.cancel();
        }
    }

    @Inject(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void bic$skipWorldPotionOnClient(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EFFECT_TICK_SCANS)
                && (world == null || world.isClientSide() || entity == null)) {
            ci.cancel();
        }
    }
}
