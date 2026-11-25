package com.hp.borninchaosoptimizer.mixin.combat;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.entity.ZombieLumberjackEntity;
import net.mcreator.borninchaosv.procedures.GluttonfishatackProcedure;
import net.mcreator.borninchaosv.procedures.LifestealerTFAtackProcedure;
import net.mcreator.borninchaosv.procedures.ZombieLumberjackAtackProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复破盾与吸血攻击监听器的三大缺陷：
 * 1. 阻止客户端侧独立扣减盾牌耐久与修改血量造成的双端状态失步；
 * 2. 修复伐木僵尸先扣碎盾牌（变为 AIR）再调用 addCooldown(getUseItem().getItem(), 100) 将空气加入冷却的漏洞；
 * 3. 补全盾牌耐久扣减的创造模式保护、破碎广播与模组自定义盾牌兼容。
 */
@Mixin(value = {
        GluttonfishatackProcedure.class,
        LifestealerTFAtackProcedure.class,
        ZombieLumberjackAtackProcedure.class
}, remap = false)
public abstract class ShieldBreakProceduresMixin {
    @Unique
    private static final ResourceLocation BIC_ZOMBIE_BREAK_DOOR =
            ResourceLocation.tryParse("minecraft:entity.zombie.break_wooden_door");

    @Inject(method = "onEntityAttacked", at = @At("HEAD"), cancellable = true)
    private static void bic$serverSideAndShieldFix(LivingAttackEvent event, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixGlobalCombatEvents)) {
            return;
        }
        if (event == null || event.getEntity() == null || event.getEntity().level().isClientSide()) {
            ci.cancel();
            return;
        }
        Entity source = event.getSource().getEntity();
        LivingEntity target = event.getEntity();
        if (source instanceof ZombieLumberjackEntity
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixDurabilityAndShieldContext)) {
            ci.cancel();
            if ((target instanceof Mob || target instanceof Monster || (target instanceof Player && target.isBlocking()))
                    && target.isBlocking()) {
                Item shieldItem = BicCrossCheckHelper.hurtBlockingShield(target, 500);
                if (shieldItem != null) {
                    if (target instanceof Player player) {
                        player.getCooldowns().addCooldown(shieldItem, 100);
                    }
                    Level level = target.level();
                    SoundEvent sound = BIC_ZOMBIE_BREAK_DOOR != null ? ForgeRegistries.SOUND_EVENTS.getValue(BIC_ZOMBIE_BREAK_DOOR) : null;
                    if (sound != null) {
                        level.playSound(null, BlockPos.containing(target.getX(), target.getY(), target.getZ()), sound, SoundSource.NEUTRAL, 1.0f, 1.0f);
                    }
                }
            }
        }
    }
}
