package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.StimulatingsurgePriNalozhieniiEffiektaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 优化亢奋迸发（Stimulating Surge）：将全 Entity.class 排序流扫描直接收窄为服务端 Monster.class 扫描。
 */
@Mixin(value = StimulatingsurgePriNalozhieniiEffiektaProcedure.class, remap = false)
public abstract class StimulatingSurgeProcedureMixin {
    private static final ResourceLocation GLASS_BREAK_SOUND_ID = ResourceLocation.tryParse("minecraft:block.glass.break");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$optimizeStimulatingSurge(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizePotionAuraAndStateTicks)) {
            return;
        }
        ci.cancel();
        if (entity == null || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        SoundEvent glassBreak = GLASS_BREAK_SOUND_ID != null ? ForgeRegistries.SOUND_EVENTS.getValue(GLASS_BREAK_SOUND_ID) : null;
        if (glassBreak != null) {
            serverLevel.playSound(null, BlockPos.containing(x, y, z), glassBreak, SoundSource.NEUTRAL, 1.0f, 1.0f);
        }

        Vec3 center = new Vec3(entity.getX(), y, entity.getZ());
        List<Monster> monsters = serverLevel.getEntitiesOfClass(Monster.class, new AABB(center, center).inflate(3.5));
        MobEffect stimulation = BornInChaosV1ModMobEffects.STIMULATION.get();
        for (Monster monster : monsters) {
            monster.addEffect(new MobEffectInstance(stimulation, 400, 0, false, false));
        }

        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.STIMULATINGSMOKE.get(), x, y, z, 19, 1.2, 0.2, 1.2, 0.1);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.STIMULATINGBUBBLES.get(), x, y, z, 19, 1.2, 0.2, 1.2, 0.1);
    }
}
