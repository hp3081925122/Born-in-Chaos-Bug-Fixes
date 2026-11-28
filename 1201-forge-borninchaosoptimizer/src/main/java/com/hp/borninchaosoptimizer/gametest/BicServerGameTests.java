package com.hp.borninchaosoptimizer.gametest;

import com.hp.borninchaosoptimizer.Borninchaosoptimizer;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.MixinEnvironment;

/**
 * 1.20.1 Forge 专用服务端无头 GameTest 验收套件：
 * 1. 强制执行全量公共 Mixin 字节码审计，并实机校验全部 98 种生于混沌实体的追踪半径已收敛至原版安全区间；
 * 2. 实机校验 MCR-60 爆炸标签免疫修复对 PLAYER_EXPLOSION 生效且不影响 genericKill 击杀。
 */
@GameTestHolder(Borninchaosoptimizer.MOD_ID)
@PrefixGameTestTemplate(false)
@Mod.EventBusSubscriber(modid = Borninchaosoptimizer.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BicServerGameTests {
    private BicServerGameTests() {
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        event.register(BicServerGameTests.class);
    }

    @GameTest(template = "empty", templateNamespace = Borninchaosoptimizer.MOD_ID, timeoutTicks = 100)
    public static void verifyMixinsAndEntityTrackingRanges(GameTestHelper helper) {
        // 强制审计专用服务端环境下所有注册的公共 Mixin
        MixinEnvironment.getCurrentEnvironment().audit();

        int checked = 0;
        for (RegistryObject<EntityType<?>> entry : BornInChaosV1ModEntities.REGISTRY.getEntries()) {
            EntityType<?> type = entry.get();
            int range = type.clientTrackingRange();
            helper.assertTrue(
                    range >= 4 && range <= 10,
                    "Born in Chaos entity tracking range must be clamped to 4..10 chunks, got " + range + " for " + entry.getId()
            );
            checked++;
        }
        helper.assertTrue(checked == 98, "Expected 98 Born in Chaos entities, got " + checked);
        helper.assertTrue(
                BornInChaosV1ModEntities.NIGHTMARE_STALKER.get().clientTrackingRange() == 10,
                "Nightmare Stalker tracking range must be 10 chunks"
        );
        helper.assertTrue(
                BornInChaosV1ModEntities.DECAYING_ZOMBIE.get().clientTrackingRange() == 8,
                "Decaying Zombie tracking range must be 8 chunks"
        );
        helper.assertTrue(
                BornInChaosV1ModEntities.PUMPKIN_PISTOL_PROJECTILE.get().clientTrackingRange() == 6,
                "Pumpkin Pistol Projectile tracking range must be 6 chunks"
        );

        Borninchaosoptimizer.LOGGER.info("[BornInChaosOptimizer] 1.20.1 Mixin audit and 98 entity tracking ranges verified!");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = Borninchaosoptimizer.MOD_ID, timeoutTicks = 100)
    public static void verifyExplosionImmunityBlocksPlayerExplosion(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Creeper creeper = EntityType.CREEPER.create(level);
        helper.assertTrue(creeper != null, "Failed to create vanilla Creeper for PLAYER_EXPLOSION test");
        Vec3 creeperPos = helper.absoluteVec(new Vec3(1.5, 2.0, 1.5));
        creeper.moveTo(creeperPos.x, creeperPos.y, creeperPos.z, 0.0F, 0.0F);
        level.addFreshEntity(creeper);

        DamageSource playerExplosion = level.damageSources().explosion(creeper, creeper);
        helper.assertTrue(
                playerExplosion.is(DamageTypes.PLAYER_EXPLOSION) && playerExplosion.is(DamageTypeTags.IS_EXPLOSION),
                "Expected Creeper explosion DamageSource to use DamageTypes.PLAYER_EXPLOSION with IS_EXPLOSION tag"
        );
        DamageSource genericKill = level.damageSources().genericKill();

        try {
            // 校验受 MCR-60 修复的代表性实体（涵盖 Boss、敌对生物、可骑乘动物子类、炸弹实体）与已有守卫的幻影苦力怕
            assertExplosionImmuneAndKillable(helper, level, BornInChaosV1ModEntities.LORD_PUMPKINHEAD.get(), playerExplosion, genericKill, "LordPumpkinheadEntity");
            assertExplosionImmuneAndKillable(helper, level, BornInChaosV1ModEntities.FELSTEED.get(), playerExplosion, genericKill, "FelsteedEntity");
            assertExplosionImmuneAndKillable(helper, level, BornInChaosV1ModEntities.RIDING_FELSTEED.get(), playerExplosion, genericKill, "RidingFelsteedEntity");
            assertExplosionImmuneAndKillable(helper, level, BornInChaosV1ModEntities.PUMPKIN_BOMB.get(), playerExplosion, genericKill, "PumpkinBombEntity");
            assertExplosionImmuneAndKillable(helper, level, BornInChaosV1ModEntities.PHANTOM_CREEPER.get(), playerExplosion, genericKill, "PhantomCreeperEntity");
        } finally {
            creeper.discard();
        }

        Borninchaosoptimizer.LOGGER.info("[BornInChaosOptimizer] 1.20.1 MCR-60 PLAYER_EXPLOSION immunity and genericKill verified!");
        helper.succeed();
    }

    private static void assertExplosionImmuneAndKillable(
            GameTestHelper helper,
            ServerLevel level,
            EntityType<? extends LivingEntity> entityType,
            DamageSource playerExplosion,
            DamageSource genericKill,
            String label
    ) {
        LivingEntity entity = entityType.create(level);
        helper.assertTrue(entity != null, "Failed to create " + label);
        Vec3 spawnPos = helper.absoluteVec(new Vec3(4.0, 2.0, 4.0));
        entity.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, 0.0F, 0.0F);
        level.addFreshEntity(entity);

        try {
            float initialHealth = entity.getHealth();
            boolean explosionHurt = entity.hurt(playerExplosion, 20.0F);
            helper.assertTrue(!explosionHurt, label + " must ignore PLAYER_EXPLOSION damage");
            helper.assertTrue(
                    entity.isAlive() && !entity.isRemoved(),
                    label + " must stay alive and unremoved after PLAYER_EXPLOSION"
            );
            helper.assertTrue(
                    entity.getHealth() == initialHealth,
                    label + " health must remain unchanged after PLAYER_EXPLOSION (expected " + initialHealth + ", got " + entity.getHealth() + ")"
            );

            boolean killHurt = entity.hurt(genericKill, Float.MAX_VALUE);
            helper.assertTrue(killHurt, label + " must still take genericKill damage");
            helper.assertTrue(
                    entity.isDeadOrDying() || entity.getHealth() <= 0.0F,
                    label + " must be killed by genericKill"
            );
        } finally {
            entity.discard();
        }
    }
}
