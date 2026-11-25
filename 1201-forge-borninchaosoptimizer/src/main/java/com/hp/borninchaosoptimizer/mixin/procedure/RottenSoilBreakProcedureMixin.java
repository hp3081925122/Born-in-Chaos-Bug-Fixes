package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.procedures.RottenSoilPriRazrushieniiBlokaIghrokomProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复腐化土壤被玩家破坏过程实例化含客户端 Minecraft 引用的匿名内部类及客户端无效执行问题。
 */
@Mixin(value = RottenSoilPriRazrushieniiBlokaIghrokomProcedure.class, remap = false)
public abstract class RottenSoilBreakProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$safeExecute(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixDedicatedServerClientRefs)) {
            return;
        }
        ci.cancel();
        if (world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(entity instanceof ServerPlayer serverPlayer)
                || serverPlayer.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) {
            return;
        }
        if (Math.random() < 0.25) {
            Entity spawned = BornInChaosV1ModEntities.MAGGOT.get()
                    .spawn(serverLevel, BlockPos.containing(x + 0.5, y, z + 0.5), MobSpawnType.MOB_SUMMONED);
            if (spawned != null) {
                spawned.setYRot(serverLevel.getRandom().nextFloat() * 360.0f);
            }
        } else if (Math.random() < 0.25) {
            ItemEntity drop = new ItemEntity(serverLevel, x, y, z, new ItemStack(Items.ROTTEN_FLESH));
            drop.setPickUpDelay(10);
            serverLevel.addFreshEntity(drop);
        } else if (Math.random() < 0.25) {
            ItemEntity drop = new ItemEntity(serverLevel, x, y, z, new ItemStack(Items.BONE));
            drop.setPickUpDelay(10);
            serverLevel.addFreshEntity(drop);
        }
    }
}
