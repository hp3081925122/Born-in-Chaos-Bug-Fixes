package com.hp.borninchaosoptimizer.gametest;

import com.hp.borninchaosoptimizer.Borninchaosoptimizer;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.MixinEnvironment;

/**
 * 1.20.1 Forge 专用服务端无头 GameTest 验收套件：
 * 强制执行全量公共 Mixin 字节码审计，并实机校验全部 98 种生于混沌实体的追踪半径已收敛至原版安全区间。
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
}
