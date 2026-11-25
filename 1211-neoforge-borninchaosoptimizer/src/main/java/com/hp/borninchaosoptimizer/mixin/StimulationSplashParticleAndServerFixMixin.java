package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import java.util.List;
import net.mcreator.borninchaosv.init.BornInChaosV1ModBlocks;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.PuddleofstimulationproProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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

/**
 * 修复刺激药水瓶落地过程的三大缺陷：
 * 1. 原代码将 37 个炸点气泡/烟雾粒子写在实体遍历循环内，导致空地爆炸 0 粒子、砸中怪群同帧爆发上千粒子掉帧；
 * 2. 未判断服务端直接调用 setBlock 放置 5 格水洼，在客户端产生幽灵方块；
 * 3. 收窄为 LivingEntity.class 并省去无效距离排序。
 */
@Mixin(value = PuddleofstimulationproProcedure.class, remap = false)
public abstract class StimulationSplashParticleAndServerFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixStimulationSplash(LevelAccessor world, double x, double y, double z, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_CLIENT_SIDE_GHOST_MUTATIONS)) {
            return;
        }
        ci.cancel();
        Batch5ProcedureHelper.playSoundBothSides(world, x, y, z, "block.glass.break", 1.0f, 1.0f);
        if (world.isClientSide()) {
            return;
        }
        AABB box = new AABB(x - 4.5, y - 4.5, z - 4.5, x + 4.5, y + 4.5, z + 4.5);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, box);
        for (int i = 0, size = targets.size(); i < size; i++) {
            targets.get(i).addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.STIMULATION, 2400, 0));
        }
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.STIMULATINGBUBBLES.get(), x + 0.5, y + 1.0, z + 0.5, 19, 1.3, 0.2, 1.3, 0.1);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.STIMULATINGSMOKE.get(), x + 0.5, y + 1.0, z + 0.5, 18, 1.3, 0.2, 1.3, 0.1);
        }
        BlockState puddleState = BornInChaosV1ModBlocks.PUDDLEOF_STIMULATION.get().defaultBlockState();
        bic$tryPlacePuddle(world, x, y, z, puddleState);
        bic$tryPlacePuddle(world, x + 1.0, y, z, puddleState);
        bic$tryPlacePuddle(world, x - 1.0, y, z, puddleState);
        bic$tryPlacePuddle(world, x, y, z - 1.0, puddleState);
        bic$tryPlacePuddle(world, x, y, z + 1.0, puddleState);
    }

    @Unique
    private static void bic$tryPlacePuddle(LevelAccessor world, double px, double py, double pz, BlockState puddleState) {
        BlockPos basePos = BlockPos.containing(px, py, pz);
        BlockPos abovePos = BlockPos.containing(px, py + 1.0, pz);
        if (world.getBlockState(basePos).canOcclude() && world.getBlockState(abovePos).is(Blocks.AIR)) {
            world.setBlock(abovePos, puddleState, 3);
        }
    }
}
