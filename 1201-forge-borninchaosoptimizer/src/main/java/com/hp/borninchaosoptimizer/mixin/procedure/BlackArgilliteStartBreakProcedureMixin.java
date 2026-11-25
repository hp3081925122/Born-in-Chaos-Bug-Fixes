package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.ClientTotemDisplayHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.procedures.BlackArgilliteNKoghdaIghrokNachinaietUnichtozhatProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复玩家开始挖掘黑泥岩过程直接引用客户端 Minecraft 类导致专用服务端左键开挖崩溃的问题。
 */
@Mixin(value = BlackArgilliteNKoghdaIghrokNachinaietUnichtozhatProcedure.class, remap = false)
public abstract class BlackArgilliteStartBreakProcedureMixin {
    @Unique
    private static final ResourceLocation BIC_OPT$CURSE_SOUND_ID =
            ResourceLocation.tryBuild("minecraft", "entity.elder_guardian.curse");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$safeExecute(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixDedicatedServerClientRefs)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || !ClientTotemDisplayHelper.isGameMode(entity, GameType.SURVIVAL)) {
            return;
        }
        ItemStack mainHand = living.getMainHandItem();
        if (!(mainHand.getItem() instanceof PickaxeItem) || living.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            return;
        }
        if (world.isClientSide()) {
            ClientTotemDisplayHelper.displayActivation(new ItemStack(BornInChaosV1ModItems.AI.get()));
        }
        if (!living.level().isClientSide()) {
            living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1000, 2));
        }
        if (world instanceof Level level && BIC_OPT$CURSE_SOUND_ID != null) {
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(BIC_OPT$CURSE_SOUND_ID);
            if (sound != null) {
                if (!level.isClientSide()) {
                    level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 0.8f, 0.9f);
                } else {
                    level.playLocalSound(x, y, z, sound, SoundSource.NEUTRAL, 0.8f, 0.9f, false);
                }
            }
        }
    }
}
