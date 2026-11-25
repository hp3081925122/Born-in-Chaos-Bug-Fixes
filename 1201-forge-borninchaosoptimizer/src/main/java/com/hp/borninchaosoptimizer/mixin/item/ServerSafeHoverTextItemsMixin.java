package com.hp.borninchaosoptimizer.mixin.item;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.item.BoneHeartItem;
import net.mcreator.borninchaosv.item.BroadswordDestructionItem;
import net.mcreator.borninchaosv.item.CarrotSwordItem;
import net.mcreator.borninchaosv.item.DamnedDemomansHatItem;
import net.mcreator.borninchaosv.item.DarkAtriumItem;
import net.mcreator.borninchaosv.item.DarkMetalArmorItem;
import net.mcreator.borninchaosv.item.DarkRitualDaggerItem;
import net.mcreator.borninchaosv.item.DeathTotemItem;
import net.mcreator.borninchaosv.item.FrostbittenBladeItem;
import net.mcreator.borninchaosv.item.GreatAxoftheReaperItem;
import net.mcreator.borninchaosv.item.IcySweetnessItem;
import net.mcreator.borninchaosv.item.IntoxicatingDaggerItem;
import net.mcreator.borninchaosv.item.LordPumpkinheadsHatItem;
import net.mcreator.borninchaosv.item.MissionaryHatItem;
import net.mcreator.borninchaosv.item.MonstrousEasterEggItem;
import net.mcreator.borninchaosv.item.NightmareMantleoftheNightItem;
import net.mcreator.borninchaosv.item.NightmareScytheItem;
import net.mcreator.borninchaosv.item.NutHammerItem;
import net.mcreator.borninchaosv.item.RottenEasterEggItem;
import net.mcreator.borninchaosv.item.ShellMaceItem;
import net.mcreator.borninchaosv.item.SkullCrusherItem;
import net.mcreator.borninchaosv.item.SoulSaberItem;
import net.mcreator.borninchaosv.item.SoulbaneItem;
import net.mcreator.borninchaosv.item.SpiderBiteItem;
import net.mcreator.borninchaosv.item.SpinyShellArmorItem;
import net.mcreator.borninchaosv.item.SpiritualEasterEggItem;
import net.mcreator.borninchaosv.item.SpiritualGuideSombreroItem;
import net.mcreator.borninchaosv.item.StormcallersHornItem;
import net.mcreator.borninchaosv.item.SweetAxeItem;
import net.mcreator.borninchaosv.item.SweetSwordItem;
import net.mcreator.borninchaosv.item.TransmutingElixirItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 保护全部 38 个调用 Screen.hasShiftDown() 提示信息过程的物品类，
 * 当其他模组在物理专用服务端（Dist.DEDICATED_SERVER）调用 appendHoverText 时直接安全返回，
 * 彻底根除 NoClassDefFoundError: net/minecraft/client/gui/screens/Screen 崩服隐患。
 */
@Mixin({
        BoneHeartItem.class,
        BroadswordDestructionItem.class,
        CarrotSwordItem.class,
        DamnedDemomansHatItem.Helmet.class,
        DarkAtriumItem.class,
        DarkMetalArmorItem.Boots.class,
        DarkMetalArmorItem.Leggings.class,
        DarkMetalArmorItem.Chestplate.class,
        DarkMetalArmorItem.Helmet.class,
        DarkRitualDaggerItem.class,
        DeathTotemItem.class,
        FrostbittenBladeItem.class,
        GreatAxoftheReaperItem.class,
        IcySweetnessItem.class,
        IntoxicatingDaggerItem.class,
        LordPumpkinheadsHatItem.Helmet.class,
        MissionaryHatItem.Helmet.class,
        MonstrousEasterEggItem.class,
        NightmareMantleoftheNightItem.Boots.class,
        NightmareMantleoftheNightItem.Leggings.class,
        NightmareMantleoftheNightItem.Chestplate.class,
        NightmareMantleoftheNightItem.Helmet.class,
        NightmareScytheItem.class,
        NutHammerItem.class,
        RottenEasterEggItem.class,
        ShellMaceItem.class,
        SkullCrusherItem.class,
        SoulSaberItem.class,
        SoulbaneItem.class,
        SpiderBiteItem.class,
        SpinyShellArmorItem.Chestplate.class,
        SpinyShellArmorItem.Helmet.class,
        SpiritualEasterEggItem.class,
        SpiritualGuideSombreroItem.Helmet.class,
        StormcallersHornItem.class,
        SweetAxeItem.class,
        SweetSwordItem.class,
        TransmutingElixirItem.class
})
public abstract class ServerSafeHoverTextItemsMixin {
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void bicOpt$guardDedicatedServerTooltip(ItemStack stack, Level level, List<Component> list, TooltipFlag flag, CallbackInfo ci) {
        if (FMLEnvironment.dist != Dist.CLIENT
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixDedicatedServerItemHoverCrash)) {
            ci.cancel();
        }
    }
}
