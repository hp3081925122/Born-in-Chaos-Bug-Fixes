package com.hp.borninchaosoptimizer.mixin;

import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修正生于混沌全部 98 种实体注册时误以方块单位填入的超大追踪半径（40~170 区块，最高 2720 格）。
 * 按原版标准收敛为：弹射物 6 区块（96 格）、常规生物 8 区块（128 格）、Boss 及超远索敌精英 10 区块（160 格），
 * 同步覆写 NeoForge 的 setTrackingRange 与原版 clientTrackingRange，消除跨数十区块的无效实体同步开销。
 * 注意：register 在目标类静态初始化阶段执行，不依赖尚未加载的配置文件。
 */
@Mixin(value = BornInChaosV1ModEntities.class, remap = false)
public abstract class BornInChaosEntitiesTrackingMixin {

    @Inject(method = "register", at = @At("HEAD"), remap = false)
    private static <T extends Entity> void bic$clampEntityTrackingRange(
            String registryname,
            EntityType.Builder<T> entityTypeBuilder,
            CallbackInfoReturnable<DeferredHolder<EntityType<?>, EntityType<T>>> cir
    ) {
        if (registryname == null || entityTypeBuilder == null) {
            return;
        }
        int clampedRange = bic$resolveTrackingRange(registryname);
        entityTypeBuilder.clientTrackingRange(clampedRange);
        entityTypeBuilder.setTrackingRange(clampedRange);
    }

    @Unique
    private static int bic$resolveTrackingRange(String registryname) {
        if (registryname.endsWith("_projectile")
                || registryname.equals("missionary_charge")
                || registryname.equals("stimulating_bombprojectile")) {
            return 6;
        }
        return switch (registryname) {
            case "sir_pumpkinhead",
                 "sir_pumpkinhead_without_horse",
                 "sir_the_headless",
                 "pumpkinhead",
                 "lord_pumpkinhead",
                 "lords_felsteed",
                 "lord_pumpkinhead_withouta_horse",
                 "lord_the_headless",
                 "lord_pumpkinhead_head",
                 "supreme_bonescaller",
                 "supreme_bonescaller_not_despawn",
                 "supreme_bonescaller_stage_2",
                 "krampus",
                 "krampus_henchman",
                 "lifestealer",
                 "lifestealer_true_form",
                 "scarlet_persecutor",
                 "nightmare_stalker",
                 "missioner",
                 "missionary_raider",
                 "glutton_fish",
                 "pumpkin_spirit",
                 "seared_spirit",
                 "seared_spirit_not_despawn",
                 "infernal_spirit" -> 10;
            default -> 8;
        };
    }
}
