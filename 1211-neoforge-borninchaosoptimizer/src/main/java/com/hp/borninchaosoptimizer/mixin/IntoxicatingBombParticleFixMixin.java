package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import java.util.List;
import net.mcreator.borninchaosv.init.BornInChaosV1ModBlocks;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.IntoxicatindBombKoghdaSnariadPopadaietVSushchnostProcedure;
import net.mcreator.borninchaosv.procedures.IntoxicatindBombKoghdaSnariadPriziemliaietsiaNaBlokProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

/**
 * 修复迷醉炸弹（命中实体与落地两个过程）把固定坐标的 36 个爆炸烟雾粒子写在实体遍历循环内部的问题：
 * 根除空地爆炸 0 粒子、怪堆爆炸瞬间爆发上千粒子掉帧，以及客户端落地生成 5 格幽灵迷醉水洼方块的缺陷。
 */
@Mixin(value = {
        IntoxicatindBombKoghdaSnariadPopadaietVSushchnostProcedure.class,
        IntoxicatindBombKoghdaSnariadPriziemliaietsiaNaBlokProcedure.class
}, remap = false)
public abstract class IntoxicatingBombParticleFixMixin {
    @Inject(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void bic$fixEntityHitParticleLoop(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_TRAP_AND_AOE_NON_LIVING_DAMAGE)) {
            return;
        }
        ci.cancel();
        if (entity == null || world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.explode(null, x, y, z, 1.0f, Level.ExplosionInteraction.NONE);
        AABB box = new AABB(
                entity.getX() - 4.0, entity.getY() - 4.0, entity.getZ() - 4.0,
                entity.getX() + 4.0, entity.getY() + 4.0, entity.getZ() + 4.0
        );
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, box);
        for (int i = 0, size = targets.size(); i < size; i++) {
            targets.get(i).addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INTOXICATION, 800, 0));
        }
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INTOXICATINGSMOKE.get(), x, y, z, 18, 1.2, 0.2, 1.2, 0.1);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INTOXICATIND_BOMB_PART.get(), x, y, z, 18, 1.2, 0.2, 1.2, 0.1);
        if (!(entity instanceof Player) && entity instanceof LivingEntity directTarget) {
            directTarget.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INTOXICATION, 400, 1));
        }
    }

    @Inject(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDD)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void bic$fixBlockLandParticleLoopAndGhostPuddles(LevelAccessor world, double x, double y, double z, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_TRAP_AND_AOE_NON_LIVING_DAMAGE)
                && !OptimizerConfig.isEnabled(OptimizerConfig.FIX_CLIENT_SIDE_GHOST_MUTATIONS)) {
            return;
        }
        ci.cancel();
        if (world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.explode(null, x, y, z, 1.0f, Level.ExplosionInteraction.NONE);
        serverLevel.playSound(null, BlockPos.containing(x, y, z), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0f, 1.0f);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INTOXICATIND_BOMB_PART.get(), x + 0.5, y + 1.0, z + 0.5, 18, 1.2, 0.2, 1.2, 0.1);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INTOXICATINGSMOKE.get(), x + 0.5, y + 1.0, z + 0.5, 18, 1.2, 0.2, 1.2, 0.1);

        AABB area = new AABB(x - 4.0, y - 4.0, z - 4.0, x + 4.0, y + 4.0, z + 4.0);
        List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class, area);
        for (int i = 0, size = victims.size(); i < size; i++) {
            victims.get(i).addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INTOXICATION, 800, 0));
        }

        BlockState puddleState = BornInChaosV1ModBlocks.PUDDLEOFINTOXICATION.get().defaultBlockState();
        bic$tryPlacePuddle(serverLevel, x, y, z, puddleState);
        bic$tryPlacePuddle(serverLevel, x + 1.0, y, z, puddleState);
        bic$tryPlacePuddle(serverLevel, x - 1.0, y, z, puddleState);
        bic$tryPlacePuddle(serverLevel, x, y, z - 1.0, puddleState);
        bic$tryPlacePuddle(serverLevel, x, y, z + 1.0, puddleState);
    }

    @Unique
    private static void bic$tryPlacePuddle(ServerLevel level, double px, double py, double pz, BlockState puddle) {
        if (level.getBlockState(BlockPos.containing(px, py, pz)).canOcclude()) {
            BlockPos above = BlockPos.containing(px, py + 1.0, pz);
            if (level.getBlockState(above).getBlock() == Blocks.AIR) {
                level.setBlock(above, puddle, 3);
            }
        }
    }
}
