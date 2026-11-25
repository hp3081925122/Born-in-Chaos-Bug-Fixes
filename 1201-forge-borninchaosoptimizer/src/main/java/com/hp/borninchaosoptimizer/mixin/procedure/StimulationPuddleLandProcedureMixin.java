package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BossTeleportHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModBlocks;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.PuddleofstimulationproProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 修复亢奋药水瓶落地过程：
 * 1. 仅服务端执行，消除客户端 5 格幽灵亢奋药水洼与双重碎玻璃声；
 * 2. 将炸点粒子移出实体循环，修复空地无粒子与怪堆千粒爆发掉帧问题；
 * 3. 使用精确 LivingEntity 扫描替代全实体流式排序。
 */
@Mixin(value = PuddleofstimulationproProcedure.class, remap = false)
public abstract class StimulationPuddleLandProcedureMixin {
    @Unique
    private static final ResourceLocation BIC_OPT$GLASS_BREAK_SOUND =
            ResourceLocation.tryBuild("minecraft", "block.glass.break");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$safeLand(LevelAccessor world, double x, double y, double z, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityTickClientSideMutations)) {
            return;
        }
        ci.cancel();
        if (world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        BossTeleportHelper.playSound(serverLevel, BlockPos.containing(x, y, z), BIC_OPT$GLASS_BREAK_SOUND, SoundSource.NEUTRAL, 1.0f, 1.0f);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.STIMULATINGBUBBLES.get(), x + 0.5, y + 1.0, z + 0.5, 19, 1.3, 0.2, 1.3, 0.1);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.STIMULATINGSMOKE.get(), x + 0.5, y + 1.0, z + 0.5, 18, 1.3, 0.2, 1.3, 0.1);

        AABB area = new AABB(x - 4.5, y - 4.5, z - 4.5, x + 4.5, y + 4.5, z + 4.5);
        List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class, area);
        MobEffect stimulation = BornInChaosV1ModMobEffects.STIMULATION.get();
        for (LivingEntity victim : victims) {
            victim.addEffect(new MobEffectInstance(stimulation, 2400, 0));
        }

        BlockState puddleState = BornInChaosV1ModBlocks.PUDDLEOF_STIMULATION.get().defaultBlockState();
        bicOpt$tryPlacePuddle(serverLevel, x, y, z, puddleState);
        bicOpt$tryPlacePuddle(serverLevel, x + 1.0, y, z, puddleState);
        bicOpt$tryPlacePuddle(serverLevel, x - 1.0, y, z, puddleState);
        bicOpt$tryPlacePuddle(serverLevel, x, y, z - 1.0, puddleState);
        bicOpt$tryPlacePuddle(serverLevel, x, y, z + 1.0, puddleState);
    }

    @Unique
    private static void bicOpt$tryPlacePuddle(ServerLevel level, double px, double py, double pz, BlockState puddle) {
        if (level.getBlockState(BlockPos.containing(px, py, pz)).canOcclude()) {
            BlockPos above = BlockPos.containing(px, py + 1.0, pz);
            if (level.getBlockState(above).getBlock() == Blocks.AIR) {
                level.setBlock(above, puddle, 3);
            }
        }
    }
}
