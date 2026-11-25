package com.hp.borninchaosoptimizer.mixin.client;

import com.hp.borninchaosoptimizer.client.ClientRenderCache;
import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mcreator.borninchaosv.entity.layer.BabySpiderControlledLayer;
import net.mcreator.borninchaosv.entity.layer.BabySpiderLayer;
import net.mcreator.borninchaosv.entity.layer.BloodyGadflyLayer;
import net.mcreator.borninchaosv.entity.layer.BoneImpLayer;
import net.mcreator.borninchaosv.entity.layer.BoneImpMinionLayer;
import net.mcreator.borninchaosv.entity.layer.ControlledBabySkeletonLayer;
import net.mcreator.borninchaosv.entity.layer.ControlledSpiritualAssistantLayer;
import net.mcreator.borninchaosv.entity.layer.CorpseFishLayer;
import net.mcreator.borninchaosv.entity.layer.CorpseFlyLayer;
import net.mcreator.borninchaosv.entity.layer.DarkVortexLayer;
import net.mcreator.borninchaosv.entity.layer.DiamondThermiteLayer;
import net.mcreator.borninchaosv.entity.layer.DireHoundLeaderLayer;
import net.mcreator.borninchaosv.entity.layer.DreadHoundLayer;
import net.mcreator.borninchaosv.entity.layer.DreadHoundNotDespawnLayer;
import net.mcreator.borninchaosv.entity.layer.FallenChaosKnightLayer;
import net.mcreator.borninchaosv.entity.layer.FelsteedLayer;
import net.mcreator.borninchaosv.entity.layer.FirelightLayer;
import net.mcreator.borninchaosv.entity.layer.FirelightNotDespawnLayer;
import net.mcreator.borninchaosv.entity.layer.GluttonFishLayer;
import net.mcreator.borninchaosv.entity.layer.InfernalSpiritLayer;
import net.mcreator.borninchaosv.entity.layer.KrampusHenchmanLayer;
import net.mcreator.borninchaosv.entity.layer.KrampusLayer;
import net.mcreator.borninchaosv.entity.layer.LifestealerTrueFormLayer;
import net.mcreator.borninchaosv.entity.layer.LordPumpkinheadHeadLayer;
import net.mcreator.borninchaosv.entity.layer.LordPumpkinheadLayer;
import net.mcreator.borninchaosv.entity.layer.LordPumpkinheadWithoutaHorseLayer;
import net.mcreator.borninchaosv.entity.layer.LordsFelsteedLayer;
import net.mcreator.borninchaosv.entity.layer.MotherSpiderLayer;
import net.mcreator.borninchaosv.entity.layer.MrPumpkinLayer;
import net.mcreator.borninchaosv.entity.layer.MrsPumpkinLayer;
import net.mcreator.borninchaosv.entity.layer.NightmareStalkerLayer;
import net.mcreator.borninchaosv.entity.layer.PhantomCreeperCopyLayer;
import net.mcreator.borninchaosv.entity.layer.PhantomCreeperLayer;
import net.mcreator.borninchaosv.entity.layer.PumpkinBombLayer;
import net.mcreator.borninchaosv.entity.layer.PumpkinBruiserLayer;
import net.mcreator.borninchaosv.entity.layer.PumpkinDunceLayer;
import net.mcreator.borninchaosv.entity.layer.PumpkinheadLayer;
import net.mcreator.borninchaosv.entity.layer.RidingFelsteedLayer;
import net.mcreator.borninchaosv.entity.layer.RidingLordsFelsteedLayer;
import net.mcreator.borninchaosv.entity.layer.SearedSpiritLayer;
import net.mcreator.borninchaosv.entity.layer.SearedSpiritNotDespawnLayer;
import net.mcreator.borninchaosv.entity.layer.SenorPumpkinLayer;
import net.mcreator.borninchaosv.entity.layer.SirPumpkinheadLayer;
import net.mcreator.borninchaosv.entity.layer.SirPumpkinheadWithoutHorseLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * 优化全部 44 种 GeckoLib 发光层：
 * 1. 实例级缓存 RenderType.eyes(LAYER)，消除每帧 Pair 包装对象分配；
 * 2. 按资源重载周期缓存 BakedGeoModel，消除每帧重复调用 getModelResource 与模型查表；
 * 3. 配合 ClientRenderCache 固定缓冲合批机制，消除本体与发光层交替冲刷导致的每实体 VBO 提交风暴。
 */
@Mixin(value = {
        BabySpiderControlledLayer.class,
        BabySpiderLayer.class,
        BloodyGadflyLayer.class,
        BoneImpLayer.class,
        BoneImpMinionLayer.class,
        ControlledBabySkeletonLayer.class,
        ControlledSpiritualAssistantLayer.class,
        CorpseFishLayer.class,
        CorpseFlyLayer.class,
        DarkVortexLayer.class,
        DiamondThermiteLayer.class,
        DireHoundLeaderLayer.class,
        DreadHoundLayer.class,
        DreadHoundNotDespawnLayer.class,
        FallenChaosKnightLayer.class,
        FelsteedLayer.class,
        FirelightLayer.class,
        FirelightNotDespawnLayer.class,
        GluttonFishLayer.class,
        InfernalSpiritLayer.class,
        KrampusHenchmanLayer.class,
        KrampusLayer.class,
        LifestealerTrueFormLayer.class,
        LordPumpkinheadHeadLayer.class,
        LordPumpkinheadLayer.class,
        LordPumpkinheadWithoutaHorseLayer.class,
        LordsFelsteedLayer.class,
        MotherSpiderLayer.class,
        MrPumpkinLayer.class,
        MrsPumpkinLayer.class,
        NightmareStalkerLayer.class,
        PhantomCreeperCopyLayer.class,
        PhantomCreeperLayer.class,
        PumpkinBombLayer.class,
        PumpkinBruiserLayer.class,
        PumpkinDunceLayer.class,
        PumpkinheadLayer.class,
        RidingFelsteedLayer.class,
        RidingLordsFelsteedLayer.class,
        SearedSpiritLayer.class,
        SearedSpiritNotDespawnLayer.class,
        SenorPumpkinLayer.class,
        SirPumpkinheadLayer.class,
        SirPumpkinheadWithoutHorseLayer.class
}, remap = false)
public abstract class GeoLayerOptimizeMixin extends GeoRenderLayer<GeoAnimatable> {
    @Unique
    private RenderType bic$cachedGlowRenderType;
    @Unique
    private BakedGeoModel bic$cachedBakedModel;
    @Unique
    private int bic$bakedModelEpoch = -1;

    protected GeoLayerOptimizeMixin(GeoRenderer<GeoAnimatable> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public BakedGeoModel getDefaultBakedModel(GeoAnimatable animatable) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_GLOW_LAYERS_AND_RENDER_TYPES)) {
            return super.getDefaultBakedModel(animatable);
        }
        int epoch = ClientRenderCache.getReloadEpoch();
        BakedGeoModel cached = this.bic$cachedBakedModel;
        if (cached == null || this.bic$bakedModelEpoch != epoch) {
            this.bic$cachedBakedModel = cached = super.getDefaultBakedModel(animatable);
            this.bic$bakedModelEpoch = epoch;
        }
        return cached;
    }

    @Redirect(
            method = "render*",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;eyes(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
            )
    )
    private RenderType bic$cacheLayerEyesType(ResourceLocation texture) {
        RenderType cached = this.bic$cachedGlowRenderType;
        if (cached == null) {
            this.bic$cachedGlowRenderType = cached = ClientRenderCache.eyes(texture);
        }
        if (OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_GLOW_LAYERS_AND_RENDER_TYPES)) {
            ClientRenderCache.markGlowActive(cached);
        }
        return cached;
    }

    @Redirect(
            method = "render*",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lsoftware/bernie/geckolib/renderer/GeoRenderer;reRender(Lsoftware/bernie/geckolib/cache/object/BakedGeoModel;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lsoftware/bernie/geckolib/animatable/GeoAnimatable;Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/VertexConsumer;FIII)V"
            )
    )
    private <T extends GeoAnimatable> void bic$reRenderWithOptionalImmediateFlush(
            GeoRenderer<T> renderer,
            BakedGeoModel model,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            T animatable,
            RenderType renderType,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay,
            int colour
    ) {
        boolean optEnabled = OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_GLOW_LAYERS_AND_RENDER_TYPES);
        if (optEnabled && animatable instanceof Entity entity) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && entity.isInvisibleTo(mc.player) && !mc.shouldEntityAppearGlowing(entity)) {
                return;
            }
        }
        renderer.reRender(model, poseStack, bufferSource, animatable, renderType, buffer, partialTick, packedLight, packedOverlay, colour);
        if (!optEnabled
                && !ClientRenderCache.isShaderModLoaded()
                && renderType != null
                && bufferSource instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(renderType);
        }
    }
}
