package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.SeasonalDateCache;
import net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules;
import net.mcreator.borninchaosv.procedures.SerPumpkinheadSpavnProcedure;
import net.mcreator.borninchaosv.procedures.WarningSoundProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 优化南瓜爵士自然生成条件判定与全局预警时间检查：
 * 1. 消除每次自然刷怪判定 6 次 Calendar.getInstance()、4 次重复群系查询与重复 TagKey/ResourceLocation 分配；
 * 2. 跳过客户端侧的 WarningSoundProcedure 无效空跑。
 */
@Mixin(value = {
        SerPumpkinheadSpavnProcedure.class,
        WarningSoundProcedure.class
}, remap = false)
public abstract class SirPumpkinheadSpawnOptimizeMixin {
    @Unique
    private static final ResourceLocation BIC_DEEP_DARK = ResourceLocation.fromNamespaceAndPath("minecraft", "deep_dark");
    @Unique
    private static final ResourceLocation BIC_MUSHROOM_FIELDS = ResourceLocation.fromNamespaceAndPath("minecraft", "mushroom_fields");
    @Unique
    private static final TagKey<Biome> BIC_IS_OVERWORLD =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "is_overworld"));
    @Unique
    private static final TagKey<Biome> BIC_TERRALITH_ALL =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("terralith", "all_terralith_biomes"));

    @Inject(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDD)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void bic$optimizeSirPumpkinheadSpawn(LevelAccessor world, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_SEASONAL_RELOAD_OVERWRITE)
                && !OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_SKY_AND_SUNBURN_CHECKS)) {
            return;
        }
        ResourceKey<Level> dimension;
        if (world instanceof Level lvl) {
            dimension = lvl.dimension();
        } else if (world instanceof WorldGenLevel wgl) {
            dimension = wgl.getLevel().dimension();
        } else {
            dimension = Level.OVERWORLD;
        }
        if (dimension != Level.OVERWORLD) {
            cir.setReturnValue(false);
            return;
        }
        var rules = world.getLevelData().getGameRules();
        if (!rules.getBoolean(BornInChaosV1ModGameRules.SERPUMPKINHEADSPAWN)
                || !rules.getBoolean(BornInChaosV1ModGameRules.SEASONAL_EVENTS)
                || (!SeasonalDateCache.isHalloweenDate() && !rules.getBoolean(BornInChaosV1ModGameRules.HALLOWEEN_EVENT))) {
            cir.setReturnValue(false);
            return;
        }

        BlockPos pos = BlockPos.containing(x, y, z);
        int light = world.getMaxLocalRawBrightness(pos);
        if (light > 4) {
            cir.setReturnValue(false);
            return;
        }
        if (light > 0 && !world.canSeeSkyFromBelowWater(BlockPos.containing(x, y + 1.0, z))) {
            cir.setReturnValue(false);
            return;
        }

        Holder<Biome> biome = world.getBiome(pos);
        if (biome.is(BIC_DEEP_DARK) || biome.is(BIC_MUSHROOM_FIELDS)) {
            cir.setReturnValue(false);
            return;
        }
        cir.setReturnValue(biome.is(BIC_IS_OVERWORLD) || biome.is(BIC_TERRALITH_ALL));
    }

    @Inject(
            method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void bic$skipClientWarningSound(Event event, LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (world == null || world.isClientSide() || entity == null) {
            ci.cancel();
        }
    }
}
