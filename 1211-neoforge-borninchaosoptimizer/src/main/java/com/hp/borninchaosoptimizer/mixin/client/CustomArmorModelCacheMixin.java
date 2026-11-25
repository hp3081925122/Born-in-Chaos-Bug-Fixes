package com.hp.borninchaosoptimizer.mixin.client;

import com.hp.borninchaosoptimizer.client.ClientRenderCache;
import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 为全部 8 套（共 13 个部位扩展实例）生于混沌自定义 3D 盔甲缓存烘焙后的 HumanoidModel 实例，
 * 根除原实现每帧每件盔甲调用 1~3 次 EntityModelSet.bakeLayer() 重建完整骨骼网格的严重帧率损耗，
 * 同时通过 ClientRenderCache.getReloadEpoch() 自动支持 F3+T 资源包热重载。
 */
@Mixin(targets = {
        "net.mcreator.borninchaosv.item.DamnedDemomansHatItem$1",
        "net.mcreator.borninchaosv.item.DarkMetalArmorItem$1",
        "net.mcreator.borninchaosv.item.DarkMetalArmorItem$2",
        "net.mcreator.borninchaosv.item.DarkMetalArmorItem$3",
        "net.mcreator.borninchaosv.item.KillerRabbitEarsItem$1",
        "net.mcreator.borninchaosv.item.LordPumpkinheadsHatItem$1",
        "net.mcreator.borninchaosv.item.MissionaryHatItem$1",
        "net.mcreator.borninchaosv.item.NightmareMantleoftheNightItem$1",
        "net.mcreator.borninchaosv.item.NightmareMantleoftheNightItem$2",
        "net.mcreator.borninchaosv.item.NightmareMantleoftheNightItem$3",
        "net.mcreator.borninchaosv.item.SpinyShellArmorItem$1",
        "net.mcreator.borninchaosv.item.SpinyShellArmorItem$2",
        "net.mcreator.borninchaosv.item.SpiritualGuideSombreroItem$1"
}, remap = false)
public abstract class CustomArmorModelCacheMixin {
    @Unique
    private HumanoidModel<?> bic$cachedArmorModel;
    @Unique
    private int bic$cachedModelEpoch = -1;

    @Inject(
            method = "getHumanoidArmorModel(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/client/model/HumanoidModel;)Lnet/minecraft/client/model/HumanoidModel;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bic$returnCachedArmorModel(
            LivingEntity living,
            ItemStack stack,
            EquipmentSlot slot,
            HumanoidModel<?> defaultModel,
            CallbackInfoReturnable<HumanoidModel<?>> cir
    ) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.CACHE_CUSTOM_ARMOR_MODELS)) {
            return;
        }
        HumanoidModel<?> cached = this.bic$cachedArmorModel;
        if (cached != null && this.bic$cachedModelEpoch == ClientRenderCache.getReloadEpoch()) {
            cached.crouching = living.isShiftKeyDown();
            cached.riding = defaultModel.riding;
            cached.young = living.isBaby();
            cir.setReturnValue(cached);
        }
    }

    @Inject(
            method = "getHumanoidArmorModel(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/client/model/HumanoidModel;)Lnet/minecraft/client/model/HumanoidModel;",
            at = @At("RETURN")
    )
    private void bic$storeBakedArmorModel(
            LivingEntity living,
            ItemStack stack,
            EquipmentSlot slot,
            HumanoidModel<?> defaultModel,
            CallbackInfoReturnable<HumanoidModel<?>> cir
    ) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.CACHE_CUSTOM_ARMOR_MODELS)) {
            return;
        }
        HumanoidModel<?> result = cir.getReturnValue();
        if (result != null) {
            this.bic$cachedArmorModel = result;
            this.bic$cachedModelEpoch = ClientRenderCache.getReloadEpoch();
        }
    }
}
