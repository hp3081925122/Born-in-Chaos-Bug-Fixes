package com.hp.borninchaosoptimizer.mixin.entity;

import net.mcreator.borninchaosv.entity.DarkVortexEntity;
import net.mcreator.borninchaosv.entity.DiamondThermiteEntity;
import net.mcreator.borninchaosv.entity.FelsteedEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadHeadEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadWithoutaHorseEntity;
import net.mcreator.borninchaosv.entity.LordTheHeadlessEntity;
import net.mcreator.borninchaosv.entity.LordsFelsteedEntity;
import net.mcreator.borninchaosv.entity.PhantomCreeperCopyEntity;
import net.mcreator.borninchaosv.entity.PumpkinBombEntity;
import net.mcreator.borninchaosv.entity.PumpkinDunceEntity;
import net.mcreator.borninchaosv.entity.PumpkinheadEntity;
import net.mcreator.borninchaosv.entity.RidingFelsteedEntity;
import net.mcreator.borninchaosv.entity.RidingLordsFelsteedEntity;
import net.mcreator.borninchaosv.entity.SirPumpkinheadEntity;
import net.mcreator.borninchaosv.entity.SirPumpkinheadWithoutHorseEntity;
import net.mcreator.borninchaosv.entity.SirTheHeadlessEntity;
import net.mcreator.borninchaosv.entity.SpiritofChaosEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 MCR-60：1.20.1 MCreator 生成的实体 hurt 方法仅判断 DamageTypes.EXPLOSION，
 * 漏判玩家/生物实体引发的 DamageTypes.PLAYER_EXPLOSION（如苦力怕、TNT、末影水晶爆炸）。
 * 将爆炸免疫判定统一扩展为原版标签 DamageTypeTags.IS_EXPLOSION，并排除 BYPASSES_INVULNERABILITY，
 * 覆盖全部 18 个遗漏实体（PhantomCreeperEntity 已在 BossAndCreeperHurtOrderMixin 头部统一拦截）。
 */
@Mixin({
        DarkVortexEntity.class,
        DiamondThermiteEntity.class,
        FelsteedEntity.class,
        LordPumpkinheadEntity.class,
        LordPumpkinheadHeadEntity.class,
        LordPumpkinheadWithoutaHorseEntity.class,
        LordsFelsteedEntity.class,
        LordTheHeadlessEntity.class,
        PhantomCreeperCopyEntity.class,
        PumpkinBombEntity.class,
        PumpkinDunceEntity.class,
        PumpkinheadEntity.class,
        RidingFelsteedEntity.class,
        RidingLordsFelsteedEntity.class,
        SirPumpkinheadEntity.class,
        SirPumpkinheadWithoutHorseEntity.class,
        SirTheHeadlessEntity.class,
        SpiritofChaosEntity.class
})
public abstract class ExplosionImmunityTagFixMixin {
    @Redirect(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/resources/ResourceKey;)Z"
            )
    )
    private boolean bic$matchExplosionDamageTag(DamageSource source, ResourceKey<DamageType> key) {
        if (key == DamageTypes.EXPLOSION) {
            return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                    && source.is(DamageTypeTags.IS_EXPLOSION);
        }
        return source.is(key);
    }
}
