package com.hp.borninchaosoptimizer.mixin.block;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BossTeleportHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.PuddleofStimulationticProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化亢奋药水洼倒计时过程：移除每 tick sendBlockUpdated 网络包风暴，修复 <= 0.0 消散判定。
 */
@Mixin(value = PuddleofStimulationticProcedure.class, remap = false)
public abstract class StimulationPuddleTickMixin {
    @Unique
    private static final ResourceLocation BIC_OPT$EXTINGUISH_SOUND =
            ResourceLocation.tryBuild("minecraft", "block.fire.extinguish");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$tickPuddle(LevelAccessor world, double x, double y, double z, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeTemporaryTickingBlocks)) {
            return;
        }
        ci.cancel();
        if (world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos pos = BlockPos.containing(x, y, z);
        if (!serverLevel.getBlockState(BlockPos.containing(x, y - 1.0, z)).canOcclude()) {
            serverLevel.destroyBlock(pos, false);
            return;
        }

        int step = Math.max(1, BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.temporaryBlockTickInterval));
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.STIMULATINGBUBBLES.get(), x + 0.5, y, z + 0.5,
                Math.min(3, Math.max(1, step / 2)), 0.2, 0.2, 0.2, 0.2);

        BlockEntity be = serverLevel.getBlockEntity(pos);
        if (be == null) {
            return;
        }
        double next = be.getPersistentData().getDouble("puddle") - step;
        if (next <= 0.0) {
            serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), x + 0.5, y + 0.5, z + 0.5, 5, 0.3, 0.3, 0.3, 0.1);
            BossTeleportHelper.playSound(serverLevel, pos, BIC_OPT$EXTINGUISH_SOUND, SoundSource.NEUTRAL, 0.3f, 1.0f);
        } else {
            be.getPersistentData().putDouble("puddle", next);
        }
    }
}
