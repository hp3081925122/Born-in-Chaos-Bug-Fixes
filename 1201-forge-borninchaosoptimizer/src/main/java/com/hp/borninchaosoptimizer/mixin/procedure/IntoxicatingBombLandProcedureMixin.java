package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BossTeleportHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModBlocks;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.IntoxicatindBombKoghdaSnariadPriziemliaietsiaNaBlokProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
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
 * 修复迷醉炸弹落地过程：
 * 1. 仅服务端执行，消除客户端生成的 5 格幽灵药水洼方块与双重碎玻璃声；
 * 2. 修复原模组将炸点粒子写在实体遍历循环内导致空地无粒子、怪堆爆发上千粒子掉帧的严重缺陷；
 * 3. 使用精确 LivingEntity 扫描替代全实体流式排序。
 */
@Mixin(value = IntoxicatindBombKoghdaSnariadPriziemliaietsiaNaBlokProcedure.class, remap = false)
public abstract class IntoxicatingBombLandProcedureMixin {
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

        serverLevel.explode(null, x, y, z, 1.0f, Level.ExplosionInteraction.NONE);
        BossTeleportHelper.playSound(serverLevel, BlockPos.containing(x, y, z), BIC_OPT$GLASS_BREAK_SOUND, SoundSource.NEUTRAL, 1.0f, 1.0f);

        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INTOXICATIND_BOMB_PART.get(), x + 0.5, y + 1.0, z + 0.5, 18, 1.2, 0.2, 1.2, 0.1);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INTOXICATINGSMOKE.get(), x + 0.5, y + 1.0, z + 0.5, 18, 1.2, 0.2, 1.2, 0.1);

        AABB area = new AABB(x - 4.0, y - 4.0, z - 4.0, x + 4.0, y + 4.0, z + 4.0);
        List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class, area);
        MobEffect intoxication = BornInChaosV1ModMobEffects.INTOXICATION.get();
        for (LivingEntity victim : victims) {
            victim.addEffect(new MobEffectInstance(intoxication, 800, 0));
        }

        BlockState puddleState = BornInChaosV1ModBlocks.PUDDLEOFINTOXICATION.get().defaultBlockState();
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
