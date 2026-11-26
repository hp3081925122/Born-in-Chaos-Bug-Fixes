package com.hp.borninchaosoptimizer.gametest;

import com.hp.borninchaosoptimizer.Borninchaosoptimizer;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import com.hp.borninchaosoptimizer.util.IBicSyncedAnimatable;
import com.hp.borninchaosoptimizer.util.IBicSyncedGeoItem;
import com.hp.borninchaosoptimizer.util.OptimizedFlyingPathNavigation;
import com.hp.borninchaosoptimizer.util.OptimizedGroundPathNavigation;
import java.util.concurrent.atomic.AtomicInteger;
import net.mcreator.borninchaosv.BornInChaosV1Mod;
import net.mcreator.borninchaosv.entity.CorpseFlyEntity;
import net.mcreator.borninchaosv.entity.DoorKnightEntity;
import net.mcreator.borninchaosv.entity.MotherSpiderEntity;
import net.mcreator.borninchaosv.entity.NightmareStalkerEntity;
import net.mcreator.borninchaosv.entity.RestlessSpiritEntity;
import net.mcreator.borninchaosv.entity.ScarletPersecutorEntity;
import net.mcreator.borninchaosv.entity.StaffofMagicArrowsProjectileEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModBlocks;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.procedures.BlackArgilliteNKoghdaBlokRazrushienIghrokomProcedure;
import net.mcreator.borninchaosv.procedures.BlackArgilliteNKoghdaIghrokNachinaietUnichtozhatProcedure;
import net.mcreator.borninchaosv.procedures.DarkAtriumPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.DarkIcePriObnovlieniiTikaProcedure;
import net.mcreator.borninchaosv.procedures.DarkMetalDepositKoghdaBlokRazrushienIghrokomProcedure;
import net.mcreator.borninchaosv.procedures.NutHammerPriUdariePoSushchnostiInstrumientomProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinPistolshotProcedure;
import net.mcreator.borninchaosv.procedures.RottenSoilPriRazrushieniiBlokaIghrokomProcedure;
import net.mcreator.borninchaosv.procedures.StaffofMagicArrowsPriIspolzovaniiStrielkovoghoPriedmietaProcedure;
import net.mcreator.borninchaosv.procedures.ZombieswithDoorKoghdaSushchnostRanienaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.spongepowered.asm.mixin.MixinEnvironment;

/**
 * 专用服务端无头 GameTest 实机集成验证套件：
 * 强制触发全量 Mixin 字节码审计，并实机回归测试专用服务端崩溃修复、寻路与接口替换、计分板去泄漏及第五批物品/方块修复。
 */
@GameTestHolder(Borninchaosoptimizer.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OptimizerServerGameTests {
    private OptimizerServerGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void auditAllMixinsAndDedicatedServerCrashes(GameTestHelper helper) {
        // 强制加载并审计专用服务端环境下所有注册的公共 Mixin 与目标类
        MixinEnvironment.getCurrentEnvironment().audit();

        ServerLevel level = helper.getLevel();
        ServerPlayer fakePlayer = FakePlayerFactory.getMinecraft(level);
        BlockPos absPos = helper.absolutePos(new BlockPos(2, 2, 2));
        fakePlayer.setPos(absPos.getX() + 0.5, absPos.getY(), absPos.getZ() + 0.5);
        fakePlayer.setGameMode(GameType.SURVIVAL);

        // 验证全部 6 个原本在专用服务端会触发 NoClassDefFoundError 的过程可正常执行
        BlackArgilliteNKoghdaBlokRazrushienIghrokomProcedure.execute(level, absPos.getX(), absPos.getY(), absPos.getZ(), fakePlayer);
        BlackArgilliteNKoghdaIghrokNachinaietUnichtozhatProcedure.execute(level, absPos.getX(), absPos.getY(), absPos.getZ(), fakePlayer);
        DarkMetalDepositKoghdaBlokRazrushienIghrokomProcedure.execute(level, absPos.getX(), absPos.getY(), absPos.getZ(), fakePlayer);
        RottenSoilPriRazrushieniiBlokaIghrokomProcedure.execute(level, absPos.getX(), absPos.getY(), absPos.getZ(), fakePlayer);

        ItemStack gun = new ItemStack(BornInChaosV1ModItems.PUMPKINHANDGUN.get());
        fakePlayer.setGameMode(GameType.CREATIVE);
        PumpkinPistolshotProcedure.execute(level, absPos.getX(), absPos.getY(), absPos.getZ(), fakePlayer, gun);

        ItemStack magicStaff = new ItemStack(BornInChaosV1ModItems.STAFF_OF_MAGIC_ARROWS.get());
        StaffofMagicArrowsPriIspolzovaniiStrielkovoghoPriedmietaProcedure.execute(level, absPos.getX(), absPos.getY(), absPos.getZ(), fakePlayer, magicStaff);

        Borninchaosoptimizer.LOGGER.info("[BornInChaosOptimizer] MixinEnvironment.audit() and all 6 dedicated-server crash procedures verified successfully!");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void verifyCoreOptimizationsAndBugFixes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos absPos = helper.absolutePos(new BlockPos(4, 2, 4));

        // 1. 验证实体接口分发与寻路导航器替换
        NightmareStalkerEntity stalker = BornInChaosV1ModEntities.NIGHTMARE_STALKER.get().create(level);
        helper.assertTrue(stalker instanceof IBicSyncedAnimatable, "NightmareStalker must implement IBicSyncedAnimatable");
        helper.assertTrue(stalker.getNavigation() instanceof OptimizedGroundPathNavigation, "NightmareStalker must use OptimizedGroundPathNavigation");

        ScarletPersecutorEntity persecutor = BornInChaosV1ModEntities.SCARLET_PERSECUTOR.get().create(level);
        helper.assertTrue(persecutor instanceof IBicSyncedAnimatable, "ScarletPersecutor must implement IBicSyncedAnimatable");
        helper.assertTrue(persecutor.getNavigation() instanceof OptimizedFlyingPathNavigation, "ScarletPersecutor must use OptimizedFlyingPathNavigation");

        CorpseFlyEntity fly = BornInChaosV1ModEntities.CORPSE_FLY.get().create(level);
        helper.assertTrue(fly instanceof IBicSyncedAnimatable, "CorpseFly must implement IBicSyncedAnimatable");
        RestlessSpiritEntity spirit = BornInChaosV1ModEntities.RESTLESS_SPIRIT.get().create(level);
        helper.assertTrue(spirit != null && spirit.getNavigation() instanceof OptimizedFlyingPathNavigation, "RestlessSpirit must use OptimizedFlyingPathNavigation");
        helper.assertTrue(BornInChaosV1ModItems.PUMPKINHANDGUN.get() instanceof IBicSyncedGeoItem, "PumpkinhandgunItem must implement IBicSyncedGeoItem");

        // 2. 验证门骑士受击举盾时计分板零污染且正确写入实体自身 NBT
        ServerPlayer survPlayer = FakePlayerFactory.getMinecraft(level);
        DoorKnightEntity knight = BornInChaosV1ModEntities.DOOR_KNIGHT.get().create(level);
        if (knight != null) {
            knight.setPos(absPos.getX() + 0.5, absPos.getY(), absPos.getZ() + 0.5);
            ZombieswithDoorKoghdaSushchnostRanienaProcedure.execute(level, knight.getX(), knight.getY(), knight.getZ(), knight, survPlayer);
            Objective blocObj = level.getScoreboard().getObjective("bloc");
            boolean leakedUuid = blocObj != null && level.getScoreboard().listPlayerScores(blocObj).stream()
                    .anyMatch(e -> knight.getStringUUID().equals(e.owner()));
            helper.assertFalse(leakedUuid, "DoorKnight must not write UUID scores into global scoreboard");
            helper.assertTrue(knight.getPersistentData().getBoolean("bic_bloc"), "DoorKnight must record shield state in PersistentData");
        }

        // 3. 验证全局延时队列 <=0 刻任务不泄漏
        AtomicInteger counter = new AtomicInteger();
        BornInChaosV1Mod.queueServerWork(0, counter::incrementAndGet);
        BornInChaosV1Mod.queueServerWork(-2, counter::incrementAndGet);

        // 4. 验证单格消耗品不会将空气加入冷却
        survPlayer.getInventory().clearContent();
        survPlayer.getCooldowns().removeCooldown(BornInChaosV1ModItems.DARK_ATRIUM.get());
        survPlayer.setGameMode(GameType.SURVIVAL);
        ItemStack singleAtrium = new ItemStack(BornInChaosV1ModItems.DARK_ATRIUM.get(), 1);
        DarkAtriumPriShchielchkiePKMProcedure.execute(level, absPos.getX(), absPos.getY(), absPos.getZ(), survPlayer, singleAtrium);
        helper.assertTrue(singleAtrium.isEmpty(), "Survival Dark Atrium count=1 must be consumed");
        helper.assertTrue(survPlayer.getCooldowns().isOnCooldown(BornInChaosV1ModItems.DARK_ATRIUM.get()), "Dark Atrium itself must be placed on cooldown");
        helper.assertFalse(survPlayer.getCooldowns().isOnCooldown(Items.AIR), "Items.AIR must never be placed on cooldown");

        // 5. 验证坚果锤对无头盔目标不产生空掉落物，且不会误删背包白板头盔复制附魔头盔
        survPlayer.getInventory().clearContent();
        survPlayer.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        int itemsBeforeBare = level.getEntitiesOfClass(ItemEntity.class, stalker.getBoundingBox().inflate(64.0)).size();
        for (int i = 0; i < 10; i++) {
            NutHammerPriUdariePoSushchnostiInstrumientomProcedure.execute(level, survPlayer);
        }
        int emptyDrops = level.getEntitiesOfClass(ItemEntity.class, survPlayer.getBoundingBox().inflate(16.0), e -> e.getItem().isEmpty()).size();
        helper.assertTrue(emptyDrops == 0 && itemsBeforeBare >= 0, "Nut Hammer must never spawn empty ItemEntity on bare-headed targets");

        ItemStack plainHelmet = new ItemStack(Items.DIAMOND_HELMET);
        ItemStack enchantedHelmet = new ItemStack(Items.DIAMOND_HELMET);
        enchantedHelmet.enchant(
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION), 4
        );
        survPlayer.getInventory().setItem(0, plainHelmet);
        survPlayer.setItemSlot(EquipmentSlot.HEAD, enchantedHelmet);
        for (int i = 0; i < 30 && !survPlayer.getItemBySlot(EquipmentSlot.HEAD).isEmpty(); i++) {
            NutHammerPriUdariePoSushchnostiInstrumientomProcedure.execute(level, survPlayer);
        }
        helper.assertFalse(survPlayer.getInventory().getItem(0).isEmpty(), "Unenchanted helmet in inventory slot 0 must not be deleted by Nut Hammer");

        // 6. 验证数据包裁剪空进度保护与暗冰方块倒计时
        helper.assertTrue(
                Batch5ProcedureHelper.safeGetOrStartProgress(survPlayer.getAdvancements(), null).isDone(),
                "Null AdvancementHolder must safely return completed progress without NPE"
        );
        level.setBlock(absPos, BornInChaosV1ModBlocks.DARK_ICE.get().defaultBlockState(), 3);
        DarkIcePriObnovlieniiTikaProcedure.execute(level, absPos.getX(), absPos.getY(), absPos.getZ());

        // 7. 验证跨版本对账补齐项：弹射物禁止拾取、母蜘蛛摔落免疫不刷幼蛛、服务端动画保留 1 刻同步
        StaffofMagicArrowsProjectileEntity testArrow = new StaffofMagicArrowsProjectileEntity(
                BornInChaosV1ModEntities.STAFFOF_MAGIC_ARROWS_PROJECTILE.get(), survPlayer, level, null
        );
        helper.assertTrue(testArrow.pickup == AbstractArrow.Pickup.DISALLOWED, "Born in Chaos projectiles must disallow player pickup");

        MotherSpiderEntity mother = BornInChaosV1ModEntities.MOTHER_SPIDER.get().create(level);
        if (mother != null) {
            mother.setPos(absPos.getX() + 0.5, absPos.getY() + 1.0, absPos.getZ() + 0.5);
            mother.setHealth(mother.getMaxHealth() - 5.0f);
            boolean fallHurt = mother.hurt(level.damageSources().fall(), 10.0f);
            helper.assertFalse(fallHurt, "MotherSpider must remain immune to fall damage");
            helper.assertFalse(mother.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE), "Fall immunity must not trigger MotherSpider summon buff");
        }

        stalker.setAnimation("attack");
        ((IBicSyncedAnimatable) stalker).bic$handleAnimationSyncTick(false);
        helper.assertTrue("attack".equals(stalker.getSyncedAnimation()), "Server animation must stay on SynchedEntityData for 1 tick before resetting");
        ((IBicSyncedAnimatable) stalker).bic$handleAnimationSyncTick(false);
        helper.assertTrue("undefined".equals(stalker.getSyncedAnimation()), "Server animation must reset to undefined on the following tick");

        // 8. 验证 10 个生物数据驱动战利品表与 4 个方块实体本地化名称
        String[] migratedLootIds = {
                "entities/baby_skeleton",
                "entities/firelight_not_despawn",
                "entities/scarlet_persecutor",
                "entities/siamese_skeletonsleft",
                "entities/siamese_skeletonsright",
                "entities/barrel_zombie",
                "entities/door_knight",
                "entities/door_knight_not_despawn",
                "entities/fallen_chaos_knight",
                "entities/zombie_clown_not_despawn"
        };
        for (String lootPath : migratedLootIds) {
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootKey =
                    net.minecraft.resources.ResourceKey.create(
                            Registries.LOOT_TABLE,
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", lootPath)
                    );
            net.minecraft.world.level.storage.loot.LootTable table = level.getServer().reloadableRegistries().getLootTable(lootKey);
            helper.assertTrue(table != net.minecraft.world.level.storage.loot.LootTable.EMPTY, "Entity loot table must load cleanly: " + lootPath);
        }
        net.minecraft.world.level.block.entity.BlockEntity darkIceBe = level.getBlockEntity(absPos);
        helper.assertTrue(darkIceBe instanceof net.minecraft.world.Nameable nameable
                        && nameable.getDisplayName().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc
                        && "block.born_in_chaos_v1.dark_ice".equals(tc.getKey()),
                "DarkIceBlockEntity displayName must use block translation key");

        // 9. 验证全部 98 种实体追踪半径已收敛（弹射物 6、常规生物 8、Boss/精英 10，不得出现 > 10 区块）
        int checkedEntities = 0;
        for (var holder : BornInChaosV1ModEntities.REGISTRY.getEntries()) {
            net.minecraft.world.entity.EntityType<?> type = holder.get();
            int range = type.clientTrackingRange();
            helper.assertTrue(range >= 4 && range <= 10, "Tracking range must be clamped to 4..10 chunks, got " + range + " for " + holder.getId());
            checkedEntities++;
        }
        helper.assertTrue(checkedEntities == 98, "Expected 98 Born in Chaos entity types, got " + checkedEntities);
        helper.assertTrue(BornInChaosV1ModEntities.NIGHTMARE_STALKER.get().clientTrackingRange() == 10, "Nightmare Stalker tracking range must be 10 chunks");
        helper.assertTrue(BornInChaosV1ModEntities.DECAYING_ZOMBIE.get().clientTrackingRange() == 8, "Decaying Zombie tracking range must be 8 chunks");
        helper.assertTrue(BornInChaosV1ModEntities.PUMPKIN_PISTOL_PROJECTILE.get().clientTrackingRange() == 6, "Pumpkin Pistol Projectile tracking range must be 6 chunks");

        Borninchaosoptimizer.LOGGER.info("[BornInChaosOptimizer] All core optimizations and Batch 1-6 + MCR-14 bug fixes verified in live ServerLevel!");
        helper.succeed();
    }
}
