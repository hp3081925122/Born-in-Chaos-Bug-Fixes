package com.hp.borninchaosoptimizer.core;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin 配置插件，用于在类加载前判断可选运行时环境（如 GeckoLib 与通用 MCreator Optimizer）是否存在。
 */
public final class BornInChaosMixinPlugin implements IMixinConfigPlugin {
    private boolean geckoLibPresent;
    private boolean mcrOptimizerServicePresent;

    @Override
    public void onLoad(String mixinPackage) {
        this.geckoLibPresent = isClassPresent("software.bernie.geckolib.animatable.GeoEntity");
        this.mcrOptimizerServicePresent = isClassPresent("com.hp.mcroptimizer.service.McrTransformationService");
    }

    private static boolean isClassPresent(String className) {
        String resourcePath = className.replace('.', '/') + ".class";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null && loader.getResource(resourcePath) != null) {
            return true;
        }
        return BornInChaosMixinPlugin.class.getClassLoader().getResource(resourcePath) != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(".gecko.") && !this.geckoLibPresent) {
            return false;
        }
        if (mixinClassName.endsWith(".BornInChaosV1ModMixin") && this.mcrOptimizerServicePresent) {
            return false;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
