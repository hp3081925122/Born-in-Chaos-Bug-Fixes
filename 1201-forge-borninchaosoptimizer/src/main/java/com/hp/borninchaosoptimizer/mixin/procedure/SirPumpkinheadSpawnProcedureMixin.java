package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.SeasonalDateCache;
import net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules;
import net.mcreator.borninchaosv.procedures.SerPumpkinheadSpavnProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 优化南瓜爵士自然生成条件判定：消除每次刷怪尝试 6 次 Calendar.getInstance()、4 次重复群系查询与重复 TagKey/ResourceLocation 分配。
 */
@Mixin(value = SerPumpkinheadSpavnProcedure.class, remap = false)
public abstract class SirPumpkinheadSpawnProcedureMixin {
    @Unique
    private static final ResourceLocation BIC_OPT$DEEP_DARK = ResourceLocation.tryBuild("minecraft", "deep_dark");
    @Unique
    private static final ResourceLocation BIC_OPT$MUSHROOM_FIELDS = ResourceLocation.tryBuild("minecraft", "mushroom_fields");
    @Unique
    private static final TagKey<Biome> BIC_OPT$IS_OVERWORLD =
            TagKey.create(Registries.BIOME, ResourceLocation.tryBuild("minecraft", "is_overworld"));
    @Unique
    private static final TagKey<Biome> BIC_OPT$TERRALITH_ALL =
            TagKey.create(Registries.BIOME, ResourceLocation.tryBuild("terralith", "all_terralith_biomes"));

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$optimizeSpawnCheck(LevelAccessor world, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeSunAndSkyTickProcedures)) {
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
                || (!SeasonalDateCache.isHalloweenWindow() && !rules.getBoolean(BornInChaosV1ModGameRules.HALLOWEEN_EVENT))) {
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
        if ((BIC_OPT$DEEP_DARK != null && biome.is(BIC_OPT$DEEP_DARK))
                || (BIC_OPT$MUSHROOM_FIELDS != null && biome.is(BIC_OPT$MUSHROOM_FIELDS))) {
            cir.setReturnValue(false);
            return;
        }
        cir.setReturnValue(biome.is(BIC_OPT$IS_OVERWORLD) || biome.is(BIC_OPT$TERRALITH_ALL));
    }
}
