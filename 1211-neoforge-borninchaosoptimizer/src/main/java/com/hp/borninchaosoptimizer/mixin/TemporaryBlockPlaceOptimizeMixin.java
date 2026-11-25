package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.procedures.DarkIcePriDobavlieniiBlokaProcedure;
import net.mcreator.borninchaosv.procedures.FelSoilDestructiblePriDobavlieniiBlokaProcedure;
import net.mcreator.borninchaosv.procedures.PuddleofintoxicationPriDobavlieniiBlokaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 消除暗冰、可毁邪土与水洼放置过程中无意义的 sendBlockUpdated 调用，仅标记服务端 BlockEntity 脏数据。
 */
@Mixin(value = {
        DarkIcePriDobavlieniiBlokaProcedure.class,
        FelSoilDestructiblePriDobavlieniiBlokaProcedure.class,
        PuddleofintoxicationPriDobavlieniiBlokaProcedure.class
}, remap = false)
public abstract class TemporaryBlockPlaceOptimizeMixin {
    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;sendBlockUpdated(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;I)V"
            )
    )
    private static void bic$skipPlaceBlockUpdate(Level level, BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_TEMPORARY_BLOCK_ENTITY_SYNC)) {
            level.sendBlockUpdated(pos, oldState, newState, flags);
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            be.setChanged();
        }
    }
}
