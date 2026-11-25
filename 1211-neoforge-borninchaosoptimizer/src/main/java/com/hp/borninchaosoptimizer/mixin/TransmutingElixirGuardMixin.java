package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.BabySkeletonEntity;
import net.mcreator.borninchaosv.entity.CorpseFlyEntity;
import net.mcreator.borninchaosv.entity.DecrepitSkeletonEntity;
import net.mcreator.borninchaosv.entity.DreadHoundEntity;
import net.mcreator.borninchaosv.entity.DreadHoundNotDespawnEntity;
import net.mcreator.borninchaosv.entity.SearedSpiritEntity;
import net.mcreator.borninchaosv.entity.SearedSpiritNotDespawnEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.procedures.TransmutingElixirclicProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复嬗变灵药右键实体过程：
 * 1. 拦截客户端双端交互执行，消除客户端提前 shrink(1) 造成的数量闪烁与重叠本地音效；
 * 2. 增加服务端目标存活与一次性转化幂等锁，杜绝同刻连点将同一目标重复转化两次。
 */
@Mixin(value = TransmutingElixirclicProcedure.class, remap = false)
public abstract class TransmutingElixirGuardMixin {
    @Unique
    private static final String BIC_TRANSMUTE_LOCK = "bic_opt_transmuted";

    @Inject(
            method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bic$guardServerAndIdempotentTransmute(
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
        if (world == null || world.isClientSide() || entity == null || sourceentity == null) {
            ci.cancel();
            return;
        }
        if (entity.isRemoved() || !entity.isAlive() || entity.getPersistentData().getBoolean(BIC_TRANSMUTE_LOCK)) {
            ci.cancel();
            return;
        }
        ItemStack mainHand = sourceentity instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
        if (mainHand.getItem() == BornInChaosV1ModItems.TRANSMUTING_ELIXIR.get() && bic$isValidTransmuteTarget(entity)) {
            entity.getPersistentData().putBoolean(BIC_TRANSMUTE_LOCK, true);
        }
    }

    @Unique
    private static boolean bic$isValidTransmuteTarget(Entity entity) {
        return entity instanceof BabySkeletonEntity
                || entity instanceof CorpseFlyEntity
                || entity instanceof Salmon
                || entity instanceof TropicalFish
                || entity instanceof Cod
                || entity instanceof Pig
                || entity instanceof DreadHoundEntity
                || entity instanceof DreadHoundNotDespawnEntity
                || entity instanceof SearedSpiritEntity
                || entity instanceof SearedSpiritNotDespawnEntity
                || entity instanceof Creeper
                || entity instanceof Zombie
                || entity instanceof DecrepitSkeletonEntity;
    }
}
