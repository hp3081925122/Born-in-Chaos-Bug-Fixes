package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules;
import net.mcreator.borninchaosv.procedures.BadFeelingactivationProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 BadFeelingactivationProcedure 在服务端使用 Component.literal(Component.translatable(...).getString())
 * 提前硬求值导致专用服务端输出原始翻译键且无法按各客户端语言本地化显示的问题。
 */
@Mixin(value = BadFeelingactivationProcedure.class, remap = false)
public abstract class BadFeelingLocalizationFixMixin {
    @Unique
    private static final ResourceLocation BIC$STALKER_ROAR_ID =
            ResourceLocation.tryBuild("born_in_chaos_v1", "stalker_roar_distant");
    @Unique
    private static final ResourceLocation BIC$MISSIONARY_ALERT_ID =
            ResourceLocation.tryBuild("born_in_chaos_v1", "missionary_alert");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$sendTranslatableWarning(
            LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci
    ) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixWarningMessageLocalization)) {
            return;
        }
        ci.cancel();
        if (entity == null || world == null) {
            return;
        }
        long dayTime = world.dayTime();
        if (world.getLevelData().getGameRules().getBoolean(BornInChaosV1ModGameRules.THEAPPEARANCEOFTHENIGHTMARESTALKER)
                && dayTime >= 60500L && dayTime <= 60700L) {
            bic$playSound(world, x, y, z, BIC$STALKER_ROAR_ID, 1.2f);
            if (entity instanceof Player player && !player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable("entity.born_in_chaos_v1.stalkerwarning"), false);
            }
        } else if (world.getLevelData().getGameRules().getBoolean(BornInChaosV1ModGameRules.MISSIONARY_SPAWN)
                && dayTime >= 228400L && dayTime <= 228600L) {
            bic$playSound(world, x, y, z, BIC$MISSIONARY_ALERT_ID, 1.0f);
            if (entity instanceof Player player && !player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable("entity.born_in_chaos_v1.minibosseswarning"), false);
            }
        }
    }

    @Unique
    private static void bic$playSound(LevelAccessor world, double x, double y, double z, ResourceLocation soundId, float volume) {
        if (!(world instanceof Level level) || soundId == null) {
            return;
        }
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(soundId);
        if (sound == null) {
            return;
        }
        if (!level.isClientSide()) {
            level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, volume, 1.0f);
        } else {
            level.playLocalSound(x, y, z, sound, SoundSource.NEUTRAL, volume, 1.0f, false);
        }
    }
}
