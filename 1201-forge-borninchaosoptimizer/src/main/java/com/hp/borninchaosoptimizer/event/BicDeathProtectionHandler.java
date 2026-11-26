package com.hp.borninchaosoptimizer.event;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * 以 HIGH 优先级统一接管暗影守护（Dark Ward）与死亡图腾（Death Totem）的免死触发，
 * 避免被默认 NORMAL 优先级的倒地复活类模组抢先拦截而失效；同时修复掉虚空与 /kill 误消耗图腾、
 * 创造模式误耗、客户端越权改状态及主副手力量等级不一致的缺陷。
 */
public final class BicDeathProtectionHandler {
    private static final ResourceLocation LIFESTEALER_SCREAM_ID =
            ResourceLocation.tryBuild("born_in_chaos_v1", "lifestealer_scream_ap");

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingDeath(LivingDeathEvent event) {
        if (event == null
                || event.isCanceled()
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixDeathProtectionEvents)) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        DamageSource source = event.getSource();
        if (source != null && source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        if (tryTriggerDarkWard(event, entity)) {
            return;
        }
        tryTriggerDeathTotem(event, entity);
    }

    private static boolean tryTriggerDarkWard(LivingDeathEvent event, LivingEntity entity) {
        MobEffect darkWard = BornInChaosV1ModMobEffects.DARK_WARD.get();
        if (!entity.hasEffect(darkWard)) {
            return false;
        }
        event.setCanceled(true);
        entity.setHealth(Math.min(10.0f, entity.getMaxHealth()));
        entity.removeEffect(darkWard);
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 3, false, false));

        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        if (LIFESTEALER_SCREAM_ID != null) {
            SoundEvent scream = ForgeRegistries.SOUND_EVENTS.getValue(LIFESTEALER_SCREAM_ID);
            if (scream != null) {
                level.playSound(null, pos, scream, SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
        }
        level.playSound(null, pos, SoundEvents.TRIDENT_THUNDER, SoundSource.NEUTRAL, 1.0f, 0.8f);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    BornInChaosV1ModParticleTypes.DARKMATTER.get(),
                    entity.getX(), entity.getY() + 1.0, entity.getZ(),
                    10, 0.4, 0.4, 0.4, 0.3
            );
            DamageSource witherDamage = new DamageSource(
                    serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.WITHER)
            );
            MobEffect lifesteal = BornInChaosV1ModMobEffects.LIFESTEAL.get();
            AABB box = AABB.ofSize(entity.position(), 10.0, 10.0, 10.0);
            List<Monster> nearbyMonsters = serverLevel.getEntitiesOfClass(Monster.class, box, Monster::isAlive);
            for (Monster monster : nearbyMonsters) {
                if (monster == entity) {
                    continue;
                }
                monster.hurt(witherDamage, 10.0f);
                monster.addEffect(new MobEffectInstance(lifesteal, 260, 0, false, false));
                serverLevel.sendParticles(
                        BornInChaosV1ModParticleTypes.DARKMATTER.get(),
                        monster.getX(), monster.getY() + 1.0, monster.getZ(),
                        4, 0.3, 0.2, 0.3, 0.1
                );
            }
        }
        return true;
    }

    private static boolean tryTriggerDeathTotem(LivingDeathEvent event, LivingEntity entity) {
        MobEffect magicDepletion = BornInChaosV1ModMobEffects.MAGIC_DEPLETION.get();
        if (entity.hasEffect(magicDepletion)) {
            return false;
        }
        Item deathTotemItem = BornInChaosV1ModItems.DEATH_TOTEM.get();
        ItemStack totemStack = ItemStack.EMPTY;
        if (entity.getOffhandItem().is(deathTotemItem)) {
            totemStack = entity.getOffhandItem();
        } else if (entity.getMainHandItem().is(deathTotemItem)) {
            totemStack = entity.getMainHandItem();
        }
        if (totemStack.isEmpty()) {
            return false;
        }
        event.setCanceled(true);
        entity.setHealth(Math.min(3.0f, entity.getMaxHealth()));
        if (!(entity instanceof Player player && player.getAbilities().instabuild)) {
            totemStack.shrink(1);
        }
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        level.playSound(null, pos, SoundEvents.TRIDENT_THUNDER, SoundSource.NEUTRAL, 1.0f, 1.0f);
        level.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 0.1f, 1.0f);

        entity.removeAllEffects();
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 4));
        entity.addEffect(new MobEffectInstance(magicDepletion, 200, 0));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY(), entity.getZ(), 15, 0.8, 0.8, 0.8, 0.1);
        }
        if (entity instanceof Player player && !hasFullNightmareArmor(entity)) {
            player.getFoodData().setFoodLevel(1);
            player.getFoodData().setSaturation(0.0f);
        }
        return true;
    }

    private static boolean hasFullNightmareArmor(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_HELMET.get())
                && entity.getItemBySlot(EquipmentSlot.CHEST).is(BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_CHESTPLATE.get())
                && entity.getItemBySlot(EquipmentSlot.LEGS).is(BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_LEGGINGS.get())
                && entity.getItemBySlot(EquipmentSlot.FEET).is(BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_BOOTS.get());
    }
}
