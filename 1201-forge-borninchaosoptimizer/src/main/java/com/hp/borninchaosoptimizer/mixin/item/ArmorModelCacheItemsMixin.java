package com.hp.borninchaosoptimizer.mixin.item;

import com.hp.borninchaosoptimizer.client.BicArmorModelCache;
import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.item.DamnedDemomansHatItem;
import net.mcreator.borninchaosv.item.DarkMetalArmorItem;
import net.mcreator.borninchaosv.item.KillerRabbitEarsItem;
import net.mcreator.borninchaosv.item.LordPumpkinheadsHatItem;
import net.mcreator.borninchaosv.item.MissionaryHatItem;
import net.mcreator.borninchaosv.item.NightmareMantleoftheNightItem;
import net.mcreator.borninchaosv.item.SpinyShellArmorItem;
import net.mcreator.borninchaosv.item.SpiritualGuideSombreroItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * 为生于混沌全部 12 件三维自定义盔甲部件注入带模型缓存的 IClientItemExtensions，
 * 彻底消除渲染每个穿戴实体时每帧重复调用 bakeLayer() 烘焙骨骼网格与构造 HumanoidModel 的高额分配。
 */
@Mixin(value = {
        DamnedDemomansHatItem.Helmet.class,
        DarkMetalArmorItem.Helmet.class,
        DarkMetalArmorItem.Chestplate.class,
        DarkMetalArmorItem.Boots.class,
        KillerRabbitEarsItem.Helmet.class,
        LordPumpkinheadsHatItem.Helmet.class,
        MissionaryHatItem.Helmet.class,
        NightmareMantleoftheNightItem.Helmet.class,
        NightmareMantleoftheNightItem.Chestplate.class,
        NightmareMantleoftheNightItem.Boots.class,
        SpinyShellArmorItem.Helmet.class,
        SpinyShellArmorItem.Chestplate.class,
        SpiritualGuideSombreroItem.Helmet.class
}, remap = false)
public abstract class ArmorModelCacheItemsMixin {
    @Inject(method = "initializeClient", at = @At("HEAD"), cancellable = true, remap = false)
    private void bicOpt$useCachedArmorModel(Consumer<IClientItemExtensions> consumer, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.enableClientOptimizations)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.cacheHumanoidArmorModels)) {
            return;
        }
        IClientItemExtensions cachedExt = BicArmorModelCache.forArmorItem((Item) (Object) this);
        if (cachedExt != null) {
            consumer.accept(cachedExt);
            ci.cancel();
        }
    }
}
