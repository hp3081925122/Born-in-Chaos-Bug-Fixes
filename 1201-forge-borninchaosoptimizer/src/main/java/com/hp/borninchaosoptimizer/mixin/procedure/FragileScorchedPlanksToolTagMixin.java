package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicToolTagHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModBlocks;
import net.mcreator.borninchaosv.procedures.FragileScorchedPlanksPProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复右键焦木板雕削为脆弱焦木板时硬编码 instanceof AxeItem 的问题，
 * 改为标准斧标签与 ToolActions.AXE_STRIP 判定，并修复创造模式误扣耐久与客户端幽灵改块问题。
 */
@Mixin(value = FragileScorchedPlanksPProcedure.class, remap = false)
public abstract class FragileScorchedPlanksToolTagMixin {
    @Inject(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bic$supportStandardAxePlanks(
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci
    ) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixHardcodedToolChecks)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        ItemStack mainHand = living.getMainHandItem();
        if (!BicToolTagHelper.isStandardAxe(mainHand)) {
            return;
        }
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockState oldState = world.getBlockState(pos);
        if (oldState.getBlock() != BornInChaosV1ModBlocks.SCORCHED_PLANKS.get()) {
            return;
        }
        living.swing(InteractionHand.MAIN_HAND, true);
        if (!world.isClientSide()) {
            if (!(living instanceof Player player && player.getAbilities().instabuild)) {
                mainHand.hurtAndBreak(1, living, holder -> holder.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            }
            BlockState newState = BicToolTagHelper.copySharedProperties(
                    oldState, BornInChaosV1ModBlocks.FRAGILE_SCORCHED_BOARDS.get().defaultBlockState());
            world.setBlock(pos, newState, 3);
        }
        if (world instanceof Level level) {
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.NEUTRAL, 1.0f, 1.0f);
            } else {
                level.playLocalSound(x, y, z, SoundEvents.AXE_STRIP, SoundSource.NEUTRAL, 1.0f, 1.0f, false);
            }
        }
    }
}
