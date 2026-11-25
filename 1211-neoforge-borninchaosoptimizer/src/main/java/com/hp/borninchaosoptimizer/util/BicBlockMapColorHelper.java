package com.hp.borninchaosoptimizer.util;

import net.minecraft.world.level.material.MapColor;

/**
 * 为生于混沌全部 109 个缺失 mapColor 的可见方块按材质语义映射最贴近的非 NONE 原版地图色，
 * 根治 Xaero 大地图、Xaero 小地图与原版纸质地图将 MapColor.NONE（col == 0）方块穿透跳过的问题。
 * 原模组已显式指定 MapColor.PLANT 的两种花朵返回 null 保持原样。
 */
public final class BicBlockMapColorHelper {
    private BicBlockMapColorHelper() {
    }

    /**
     * 按方块简单类名解析补齐后的 MapColor；若返回 null 则保留原模组自带配置。
     */
    public static MapColor resolveMapColor(String simpleClassName) {
        if (simpleClassName == null || simpleClassName.isEmpty()) {
            return null;
        }
        if ("MarigoldsBlock".equals(simpleClassName) || "RiverMintBlock".equals(simpleClassName)) {
            return null;
        }
        if (simpleClassName.startsWith("Tombstone")) {
            return "TombstonePlugBlock".equals(simpleClassName) ? MapColor.WOOD : MapColor.STONE;
        }
        if (simpleClassName.contains("StainedBlackArgillite")) {
            return MapColor.TERRACOTTA_RED;
        }
        if (simpleClassName.contains("FiredBlackArgillite")) {
            return MapColor.TERRACOTTA_BLACK;
        }
        if (simpleClassName.contains("Argillite")) {
            return MapColor.DEEPSLATE;
        }
        if (simpleClassName.contains("Scorched")) {
            if (simpleClassName.contains("Log") || simpleClassName.contains("Wood")) {
                return MapColor.COLOR_BLACK;
            }
            return MapColor.TERRACOTTA_GRAY;
        }
        if (simpleClassName.contains("DarkStainedGlass")) {
            return MapColor.COLOR_BLACK;
        }
        if (simpleClassName.contains("GreenStainedGlass")) {
            return MapColor.COLOR_GREEN;
        }
        if (simpleClassName.contains("OrangeStainedGlass")) {
            return MapColor.COLOR_ORANGE;
        }
        if (simpleClassName.contains("PurpleStainedGlass")) {
            return MapColor.COLOR_PURPLE;
        }
        if (simpleClassName.contains("ScarletStainedGlass") || "CursedScarletOrnateGlassPaneBlock".equals(simpleClassName)) {
            return MapColor.COLOR_RED;
        }
        return switch (simpleClassName) {
            case "DarkMetalBlockBlock" -> MapColor.COLOR_BLACK;
            case "DarkMetalDepositBlock", "InfectedDeepslateDiamondOreBlock" -> MapColor.DEEPSLATE;
            case "InfectedDiamondOreBlock", "WebbedCobblestoneBlock" -> MapColor.STONE;
            case "DarkGridBlock", "MeshDoorBlock", "HoundTrapBlock", "ClosedHoundTrapBlock" -> MapColor.METAL;
            case "FelSoilBlock", "FelSoilDestructibleBlock" -> MapColor.COLOR_BLACK;
            case "RottenSoilBlock" -> MapColor.DIRT;
            case "DarkIceBlock" -> MapColor.ICE;
            case "CultivatedPumpkinBlock", "EvilCarvedPumpkinBlock", "FlamingEvilPumpkinBlock",
                    "InfernalEvilPumpkinBlock", "InfernalEvilPumpkinSBlock" -> MapColor.COLOR_ORANGE;
            case "RottenInfernalPumpkinBlock" -> MapColor.TERRACOTTA_BROWN;
            case "BundleOfBonesBlock", "GnawedBonesBlock", "PileOfSkullsBlock",
                    "LifestealerSkullBlock", "NightmareStalkerSkullBlock", "SpinyShellTrapBlock" -> MapColor.SAND;
            case "SpookySnowmanHeadBlock" -> MapColor.SNOW;
            case "CreepyNutcrackerBlock" -> MapColor.WOOD;
            case "CobwebCoverBlock" -> MapColor.WOOL;
            case "PuddleofintoxicationBlock" -> MapColor.COLOR_PURPLE;
            case "PuddleofStimulationBlock" -> MapColor.COLOR_RED;
            default -> MapColor.STONE;
        };
    }
}
