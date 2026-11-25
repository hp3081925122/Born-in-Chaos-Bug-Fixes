package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicToolTagHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModBlocks;
import net.mcreator.borninchaosv.procedures.HewnScorchedLogPProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复右键焦木原木、焦木及阴燃变种去皮时硬编码 instanceof AxeItem 的问题，
 * 改为原版标准 ItemTags.AXES 与 NeoForge ItemAbilities.AXE_STRIP 判定，并修复创造模式误扣耐久与客户端幽灵改块问题。
 */
@Mixin(value = HewnScorchedLogPProcedure.class, remap = false)
public abstract class HewnScorchedLogToolTagMixin {
    @Inject(
            method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bic$supportStandardAxeLogStrip(
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci
    ) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_HARDCODED_TOOL_CHECKS)) {
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
        Block oldBlock = oldState.getBlock();
        Block targetBlock = null;
        if (oldBlock == BornInChaosV1ModBlocks.SCORCHED_LOG.get()
                || oldBlock == BornInChaosV1ModBlocks.SMOLDERING_SCORCHED_LOG.get()) {
            targetBlock = BornInChaosV1ModBlocks.STRIPPED_SCORCHED_LOG.get();
        } else if (oldBlock == BornInChaosV1ModBlocks.SCORCHED_WOOD.get()
                || oldBlock == BornInChaosV1ModBlocks.SMOLDERING_SCORCHED_WOOD.get()) {
            targetBlock = BornInChaosV1ModBlocks.STRIPPED_SCORCHED_WOOD.get();
        }
        if (targetBlock == null) {
            return;
        }
        living.swing(InteractionHand.MAIN_HAND, true);
        if (!world.isClientSide()) {
            if (!(living instanceof Player player && player.getAbilities().instabuild)) {
                mainHand.hurtAndBreak(1, living, EquipmentSlot.MAINHAND);
            }
            BlockState newState = BicToolTagHelper.copySharedProperties(oldState, targetBlock.defaultBlockState());
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
