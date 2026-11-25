package com.hp.borninchaosoptimizer.client;

import net.mcreator.borninchaosv.client.model.Modeldamneddemomanshat;
import net.mcreator.borninchaosv.client.model.Modeldarkbib;
import net.mcreator.borninchaosv.client.model.Modeldarkboots;
import net.mcreator.borninchaosv.client.model.Modeldarkhelmet;
import net.mcreator.borninchaosv.client.model.Modelkillerrabbitears;
import net.mcreator.borninchaosv.client.model.Modellordpumpkinheadhat;
import net.mcreator.borninchaosv.client.model.Modelmissionaryhat;
import net.mcreator.borninchaosv.client.model.Modelnightrobeboot;
import net.mcreator.borninchaosv.client.model.Modelnightrobebreastplate;
import net.mcreator.borninchaosv.client.model.Modelnightrobemasc;
import net.mcreator.borninchaosv.client.model.Modelspinyshellarmorchestplate;
import net.mcreator.borninchaosv.client.model.Modelspinyshellarmorhelmet;
import net.mcreator.borninchaosv.client.model.Modelspiritguidesombrero;
import net.mcreator.borninchaosv.item.DamnedDemomansHatItem;
import net.mcreator.borninchaosv.item.DarkMetalArmorItem;
import net.mcreator.borninchaosv.item.KillerRabbitEarsItem;
import net.mcreator.borninchaosv.item.LordPumpkinheadsHatItem;
import net.mcreator.borninchaosv.item.MissionaryHatItem;
import net.mcreator.borninchaosv.item.NightmareMantleoftheNightItem;
import net.mcreator.borninchaosv.item.SpinyShellArmorItem;
import net.mcreator.borninchaosv.item.SpiritualGuideSombreroItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 缓存生于混沌全部 12 件三维自定义盔甲的烘焙骨骼网格与 HumanoidModel 实例。
 * 原模组在每帧渲染穿戴者时调用 1~3 次 bakeLayer() 与 new HumanoidModel()，造成严重的渲染分配压力；
 * 本缓存绑定当前 EntityModelSet 引用，在 F3+T 重载资源时自动失效重建。
 */
public final class BicArmorModelCache {
    private static final Map<Class<?>, HumanoidModel<?>> MODEL_CACHE = new ConcurrentHashMap<>();
    private static volatile EntityModelSet lastModelSet;

    private BicArmorModelCache() {
    }

    public static IClientItemExtensions createExtension(Class<? extends Item> itemClass, Function<EntityModelSet, HumanoidModel<?>> factory) {
        return new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> defaultModel) {
                EntityModelSet currentModels = Minecraft.getInstance().getEntityModels();
                if (currentModels != lastModelSet) {
                    MODEL_CACHE.clear();
                    lastModelSet = currentModels;
                }
                HumanoidModel<?> cached = MODEL_CACHE.computeIfAbsent(itemClass, k -> factory.apply(currentModels));
                cached.crouching = living.isShiftKeyDown();
                cached.riding = defaultModel.riding;
                cached.young = living.isBaby();
                return cached;
            }
        };
    }

    private static ModelPart emptyPart() {
        return new ModelPart(Collections.emptyList(), Collections.emptyMap());
    }

    private static HumanoidModel<?> headOnly(ModelPart headPart) {
        return new HumanoidModel<>(new ModelPart(Collections.emptyList(), Map.of(
                "head", headPart,
                "hat", emptyPart(),
                "body", emptyPart(),
                "right_arm", emptyPart(),
                "left_arm", emptyPart(),
                "right_leg", emptyPart(),
                "left_leg", emptyPart()
        )));
    }

    private static HumanoidModel<?> chestAndArms(ModelPart body, ModelPart leftArm, ModelPart rightArm) {
        return new HumanoidModel<>(new ModelPart(Collections.emptyList(), Map.of(
                "body", body,
                "left_arm", leftArm,
                "right_arm", rightArm,
                "head", emptyPart(),
                "hat", emptyPart(),
                "right_leg", emptyPart(),
                "left_leg", emptyPart()
        )));
    }

    private static HumanoidModel<?> bootsOnly(ModelPart leftLeg, ModelPart rightLeg) {
        return new HumanoidModel<>(new ModelPart(Collections.emptyList(), Map.of(
                "left_leg", leftLeg,
                "right_leg", rightLeg,
                "head", emptyPart(),
                "hat", emptyPart(),
                "body", emptyPart(),
                "right_arm", emptyPart(),
                "left_arm", emptyPart()
        )));
    }

    public static IClientItemExtensions forArmorItem(Item item) {
        if (item instanceof DamnedDemomansHatItem.Helmet) {
            return createExtension(DamnedDemomansHatItem.Helmet.class,
                    models -> headOnly(new Modeldamneddemomanshat<>(models.bakeLayer(Modeldamneddemomanshat.LAYER_LOCATION)).Head));
        }
        if (item instanceof DarkMetalArmorItem.Helmet) {
            return createExtension(DarkMetalArmorItem.Helmet.class,
                    models -> headOnly(new Modeldarkhelmet<>(models.bakeLayer(Modeldarkhelmet.LAYER_LOCATION)).Head));
        }
        if (item instanceof DarkMetalArmorItem.Chestplate) {
            return createExtension(DarkMetalArmorItem.Chestplate.class, models -> {
                Modeldarkbib<?> m = new Modeldarkbib<>(models.bakeLayer(Modeldarkbib.LAYER_LOCATION));
                return chestAndArms(m.Body, m.LeftArm, m.RightArm);
            });
        }
        if (item instanceof DarkMetalArmorItem.Boots) {
            return createExtension(DarkMetalArmorItem.Boots.class, models -> {
                Modeldarkboots<?> m = new Modeldarkboots<>(models.bakeLayer(Modeldarkboots.LAYER_LOCATION));
                return bootsOnly(m.LeftBoot, m.RightBoot);
            });
        }
        if (item instanceof KillerRabbitEarsItem.Helmet) {
            return createExtension(KillerRabbitEarsItem.Helmet.class,
                    models -> headOnly(new Modelkillerrabbitears<>(models.bakeLayer(Modelkillerrabbitears.LAYER_LOCATION)).Head));
        }
        if (item instanceof LordPumpkinheadsHatItem.Helmet) {
            return createExtension(LordPumpkinheadsHatItem.Helmet.class,
                    models -> headOnly(new Modellordpumpkinheadhat<>(models.bakeLayer(Modellordpumpkinheadhat.LAYER_LOCATION)).Head));
        }
        if (item instanceof MissionaryHatItem.Helmet) {
            return createExtension(MissionaryHatItem.Helmet.class,
                    models -> headOnly(new Modelmissionaryhat<>(models.bakeLayer(Modelmissionaryhat.LAYER_LOCATION)).Head));
        }
        if (item instanceof NightmareMantleoftheNightItem.Helmet) {
            return createExtension(NightmareMantleoftheNightItem.Helmet.class,
                    models -> headOnly(new Modelnightrobemasc<>(models.bakeLayer(Modelnightrobemasc.LAYER_LOCATION)).Head));
        }
        if (item instanceof NightmareMantleoftheNightItem.Chestplate) {
            return createExtension(NightmareMantleoftheNightItem.Chestplate.class, models -> {
                Modelnightrobebreastplate<?> m = new Modelnightrobebreastplate<>(models.bakeLayer(Modelnightrobebreastplate.LAYER_LOCATION));
                return chestAndArms(m.Body, m.LeftArm, m.RightArm);
            });
        }
        if (item instanceof NightmareMantleoftheNightItem.Boots) {
            return createExtension(NightmareMantleoftheNightItem.Boots.class, models -> {
                Modelnightrobeboot<?> m = new Modelnightrobeboot<>(models.bakeLayer(Modelnightrobeboot.LAYER_LOCATION));
                return bootsOnly(m.LeftLeg, m.RightLeg);
            });
        }
        if (item instanceof SpinyShellArmorItem.Helmet) {
            return createExtension(SpinyShellArmorItem.Helmet.class,
                    models -> headOnly(new Modelspinyshellarmorhelmet<>(models.bakeLayer(Modelspinyshellarmorhelmet.LAYER_LOCATION)).Head));
        }
        if (item instanceof SpinyShellArmorItem.Chestplate) {
            return createExtension(SpinyShellArmorItem.Chestplate.class, models -> {
                Modelspinyshellarmorchestplate<?> m = new Modelspinyshellarmorchestplate<>(models.bakeLayer(Modelspinyshellarmorchestplate.LAYER_LOCATION));
                return chestAndArms(m.Body, m.LeftArm, m.RightArm);
            });
        }
        if (item instanceof SpiritualGuideSombreroItem.Helmet) {
            return createExtension(SpiritualGuideSombreroItem.Helmet.class,
                    models -> headOnly(new Modelspiritguidesombrero<>(models.bakeLayer(Modelspiritguidesombrero.LAYER_LOCATION)).Head));
        }
        return null;
    }
}
