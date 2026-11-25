package com.hp.borninchaosoptimizer.util;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import java.util.List;
import java.util.function.Consumer;
import net.mcreator.borninchaosv.entity.PumpkinPistolProjectileEntity;
import net.mcreator.borninchaosv.entity.StaffofMagicArrowsProjectileEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.item.PumpkinhandgunItem;
import net.mcreator.borninchaosv.item.StaffOfMagicArrowsAItem;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 第五批方块、物品、弹射物与过程的公共辅助逻辑，避免在 Mixin 包内产生任何额外内部类。
 */
public final class Batch5ProcedureHelper {
    private static final AdvancementProgress COMPLETED_MISSING_PROGRESS = new CompletedMissingProgress();

    private Batch5ProcedureHelper() {
    }

    private static final class CompletedMissingProgress extends AdvancementProgress {
        @Override
        public boolean isDone() {
            return true;
        }
    }

    /**
     * 保留魔矢法杖 5 级穿透属性的专用弹射物子类，替代原过程内的匿名内部类。
     */
    private static final class PiercingMagicArrowEntity extends StaffofMagicArrowsProjectileEntity {
        private final byte pierce;

        private PiercingMagicArrowEntity(EntityType<? extends StaffofMagicArrowsProjectileEntity> type, Level level, byte pierce) {
            super(type, level);
            this.pierce = pierce;
        }

        @Override
        public byte getPierceLevel() {
            return this.pierce;
        }
    }

    /**
     * 安全获取玩家进度状态：当数据包裁剪导致 AdvancementHolder 为空时返回已完成占位实例，避免空指针崩服。
     */
    public static AdvancementProgress safeGetOrStartProgress(PlayerAdvancements advancements, AdvancementHolder holder) {
        if (holder == null && OptimizerConfig.isEnabled(OptimizerConfig.FIX_MISSING_ADVANCEMENT_NULL_CRASH)) {
            return COMPLETED_MISSING_PROGRESS;
        }
        return advancements.getOrStartProgress(holder);
    }

    /**
     * 补全耐久扣减的实体与装备槽上下文，保证创造模式不扣耐久且武器/盾牌碎裂触发正常音效粒子与停止举盾。
     */
    public static void hurtStackWithContext(
            ItemStack stack,
            int amount,
            ServerLevel level,
            ServerPlayer originalPlayer,
            Consumer<Item> originalCallback,
            Entity rawHolder
    ) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_EQUIPMENT_DURABILITY_CONTEXT) || !(rawHolder instanceof LivingEntity living)) {
            stack.hurtAndBreak(amount, level, originalPlayer, originalCallback);
            return;
        }
        if (stack.isEmpty()) {
            return;
        }
        EquipmentSlot slot = resolveEquipmentSlot(living, stack);
        stack.hurtAndBreak(amount, living, slot);
    }

    public static EquipmentSlot resolveEquipmentSlot(LivingEntity living, ItemStack stack) {
        if (living.isUsingItem() && living.getUseItem() == stack) {
            return LivingEntity.getSlotForHand(living.getUsedItemHand());
        }
        if (living.getMainHandItem() == stack) {
            return EquipmentSlot.MAINHAND;
        }
        if (living.getOffhandItem() == stack) {
            return EquipmentSlot.OFFHAND;
        }
        for (EquipmentSlot candidate : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        }) {
            if (living.getItemBySlot(candidate) == stack) {
                return candidate;
            }
        }
        return EquipmentSlot.MAINHAND;
    }

    /**
     * 专用服务端安全版：暗金属矿被挖掘过程。
     */
    public static void executeDarkMetalDepositBroken(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null || !SafeClientBridge.isSurvival(entity)) {
            return;
        }
        ItemStack mainHand = entity instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
        int silkTouch = mainHand.getEnchantmentLevel(
                world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH)
        );
        if (silkTouch == 0 && world instanceof ServerLevel serverLevel) {
            int count = Mth.nextInt(serverLevel.getRandom(), 2, 4);
            for (int i = 0; i < count; i++) {
                serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, x, y, z, 2));
            }
        }
    }

    /**
     * 专用服务端安全版：腐壤被玩家挖掘过程。
     */
    public static void executeRottenSoilBroken(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null || !SafeClientBridge.isSurvival(entity) || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        if (Math.random() < 0.25) {
            Entity spawned = BornInChaosV1ModEntities.MAGGOT.get().spawn(serverLevel, BlockPos.containing(x + 0.5, y, z + 0.5), MobSpawnType.MOB_SUMMONED);
            if (spawned != null) {
                spawned.setYRot(serverLevel.getRandom().nextFloat() * 360.0f);
            }
        } else if (Math.random() < 0.25) {
            ItemEntity drop = new ItemEntity(serverLevel, x, y, z, new ItemStack(Items.ROTTEN_FLESH));
            drop.setPickUpDelay(10);
            serverLevel.addFreshEntity(drop);
        } else if (Math.random() < 0.25) {
            ItemEntity drop = new ItemEntity(serverLevel, x, y, z, new ItemStack(Items.BONE));
            drop.setPickUpDelay(10);
            serverLevel.addFreshEntity(drop);
        }
    }

    /**
     * 专用服务端安全版：南瓜手枪射击过程（同时修复耐久扣减上下文）。
     */
    public static void executePumpkinPistolShot(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        if (entity == null) {
            return;
        }
        boolean creative = SafeClientBridge.isCreative(entity);
        boolean hasBullet = entity instanceof Player p && p.getInventory().contains(new ItemStack(BornInChaosV1ModItems.PUMPKIN_BULLET.get()));
        if (!hasBullet && !creative) {
            return;
        }
        Level projectileLevel = entity.level();
        if (!projectileLevel.isClientSide()) {
            PumpkinPistolProjectileEntity arrow = new PumpkinPistolProjectileEntity(
                    BornInChaosV1ModEntities.PUMPKIN_PISTOL_PROJECTILE.get(), projectileLevel
            );
            arrow.setOwner(entity);
            arrow.setBaseDamage(3.0f);
            arrow.setKnockback(1);
            arrow.setSilent(true);
            if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_PROJECTILE_PICKUP_DUPE)) {
                arrow.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED;
            }
            arrow.setPos(entity.getX(), entity.getEyeY() - 0.1, entity.getZ());
            arrow.shoot(entity.getLookAngle().x, entity.getLookAngle().y, entity.getLookAngle().z, 5.0f, 0.0f);
            projectileLevel.addFreshEntity(arrow);
        }
        playSoundBothSides(world, x, y, z, "born_in_chaos_v1:pumpkinhead_gun_fire", 0.9f, 1.0f);
        if (itemstack.getItem() instanceof PumpkinhandgunItem) {
            CustomData.update(DataComponents.CUSTOM_DATA, itemstack, tag -> tag.putString("geckoAnim", "shot"));
        }
        if (!creative) {
            if (entity instanceof Player player) {
                Item bulletItem = BornInChaosV1ModItems.PUMPKIN_BULLET.get();
                player.getInventory().clearOrCountMatchingItems(p -> p.getItem() == bulletItem, 1, player.inventoryMenu.getCraftSlots());
            }
            if (world instanceof ServerLevel serverLevel && entity instanceof LivingEntity living) {
                Item gunItem = BornInChaosV1ModItems.PUMPKINHANDGUN.get();
                if (living.getMainHandItem().getItem() == gunItem) {
                    hurtWeapon(living.getMainHandItem(), 1, serverLevel, living, EquipmentSlot.MAINHAND);
                } else if (living.getOffhandItem().getItem() == gunItem) {
                    hurtWeapon(living.getOffhandItem(), 1, serverLevel, living, EquipmentSlot.OFFHAND);
                }
            }
        }
        if (entity instanceof Player player) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            int cooldown = helmet.getItem() == BornInChaosV1ModItems.LORD_PUMPKINHEADS_HAT_HELMET.get() ? 20 : 40;
            player.getCooldowns().addCooldown(BornInChaosV1ModItems.PUMPKINHANDGUN.get(), cooldown);
        }
    }

    /**
     * 专用服务端安全版：魔矢法杖射击过程（消除匿名客户端检查类，同时补全耐久上下文）。
     */
    public static void executeStaffOfMagicArrowsShot(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        if (entity == null) {
            return;
        }
        Item dustItem = BornInChaosV1ModItems.SPIRITUAL_DUST.get();
        boolean hasDust = entity instanceof Player p && p.getInventory().contains(new ItemStack(dustItem));
        float damage = hasDust ? 3.0f : 1.8f;
        if (hasDust && !SafeClientBridge.isCreative(entity) && entity instanceof Player player) {
            player.getInventory().clearOrCountMatchingItems(p -> p.getItem() == dustItem, 1, player.inventoryMenu.getCraftSlots());
        }
        Level projectileLevel = entity.level();
        if (!projectileLevel.isClientSide()) {
            PiercingMagicArrowEntity arrow = new PiercingMagicArrowEntity(
                    BornInChaosV1ModEntities.STAFFOF_MAGIC_ARROWS_PROJECTILE.get(), projectileLevel, (byte) 5
            );
            arrow.setOwner(entity);
            arrow.setBaseDamage(damage);
            arrow.setKnockback(1);
            arrow.setSilent(true);
            if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_PROJECTILE_PICKUP_DUPE)) {
                arrow.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED;
            }
            arrow.setPos(entity.getX(), entity.getEyeY() - 0.1, entity.getZ());
            arrow.shoot(entity.getLookAngle().x, entity.getLookAngle().y, entity.getLookAngle().z, 5.0f, 0.0f);
            projectileLevel.addFreshEntity(arrow);
        }
        playSoundBothSides(world, x, y, z, "born_in_chaos_v1:magic_staff_shoot", 0.6f, 1.0f);
        if (hasDust && world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.RITUAL.get(), x, y + 1.0, z, 5, 0.3, 0.3, 0.3, 0.1);
        }
        if (entity instanceof LivingEntity living) {
            Item staffItem = BornInChaosV1ModItems.STAFF_OF_MAGIC_ARROWS.get();
            if (living.getMainHandItem().getItem() == staffItem) {
                if (itemstack.getItem() instanceof StaffOfMagicArrowsAItem) {
                    CustomData.update(DataComponents.CUSTOM_DATA, itemstack, tag -> tag.putString("geckoAnim", "shot_right"));
                }
                living.swing(InteractionHand.MAIN_HAND, true);
                if (world instanceof ServerLevel serverLevel) {
                    hurtWeapon(living.getMainHandItem(), 1, serverLevel, living, EquipmentSlot.MAINHAND);
                }
            } else if (living.getOffhandItem().getItem() == staffItem) {
                if (itemstack.getItem() instanceof StaffOfMagicArrowsAItem) {
                    CustomData.update(DataComponents.CUSTOM_DATA, itemstack, tag -> tag.putString("geckoAnim", "shot_left"));
                }
                living.swing(InteractionHand.OFF_HAND, true);
                if (world instanceof ServerLevel serverLevel) {
                    hurtWeapon(living.getOffhandItem(), 1, serverLevel, living, EquipmentSlot.OFFHAND);
                }
            }
            if (living instanceof Player player) {
                boolean fullNightmareSet = living.getItemBySlot(EquipmentSlot.HEAD).getItem() == BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_HELMET.get()
                        && living.getItemBySlot(EquipmentSlot.CHEST).getItem() == BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_CHESTPLATE.get()
                        && living.getItemBySlot(EquipmentSlot.LEGS).getItem() == BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_LEGGINGS.get()
                        && living.getItemBySlot(EquipmentSlot.FEET).getItem() == BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_BOOTS.get();
                player.getCooldowns().addCooldown(staffItem, fullNightmareSet ? 25 : 40);
            }
        }
    }

    private static void hurtWeapon(ItemStack stack, int amount, ServerLevel level, LivingEntity living, EquipmentSlot slot) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_EQUIPMENT_DURABILITY_CONTEXT)) {
            stack.hurtAndBreak(amount, living, slot);
        } else {
            stack.hurtAndBreak(amount, level, null, item -> {});
        }
    }

    /**
     * 修复坚果锤击落头盔逻辑：跳过无头盔目标，直接清空 HEAD 槽位，根除附魔头盔复制、无容器能力生物无限掉头盔及空掉落物。
     */
    public static void executeNutHammerDisarm(LevelAccessor world, Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        ItemStack headStack = living.getItemBySlot(EquipmentSlot.HEAD);
        if (headStack.isEmpty()) {
            return;
        }
        boolean isPlayer = living instanceof Player;
        double chance = isPlayer ? 0.35 : 0.6;
        if (Math.random() >= chance) {
            return;
        }
        if (world instanceof ServerLevel serverLevel) {
            ItemStack droppedCopy = headStack.copy();
            living.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            if (living instanceof Player player) {
                player.getInventory().setChanged();
            }
            ItemEntity drop = new ItemEntity(serverLevel, living.getX(), living.getY() + 1.5, living.getZ(), droppedCopy);
            drop.setPickUpDelay(15);
            serverLevel.addFreshEntity(drop);
            serverLevel.sendParticles(
                    BornInChaosV1ModParticleTypes.STUNSTARS.get(),
                    living.getX(), living.getY() + 1.5, living.getZ(),
                    5, 0.25, 0.2, 0.25, 0.1
            );
        }
        playSoundBothSides(world, living.getX(), living.getY() + 1.5, living.getZ(), "born_in_chaos_v1:bonk_hit", isPlayer ? 0.8f : 0.6f, 0.9f);
    }

    /**
     * 处理魔灯/空魔灯转换：保护创造模式不损耗原物品，并在原槽位堆叠大于 1 时将转换产物安全放入背包或脚下，不覆盖整组物品。
     */
    public static void replaceHeldLampSafely(LivingEntity holder, InteractionHand hand, Item resultItem) {
        ItemStack current = holder.getItemInHand(hand);
        ItemStack resultStack = new ItemStack(resultItem, 1);
        boolean creative = SafeClientBridge.isCreative(holder);
        if (!creative) {
            current.shrink(1);
        }
        if (current.isEmpty()) {
            holder.setItemInHand(hand, resultStack);
            if (holder instanceof Player player) {
                player.getInventory().setChanged();
            }
        } else if (holder instanceof Player player) {
            if (!creative || !player.getInventory().contains(resultStack)) {
                if (!player.getInventory().add(resultStack)) {
                    player.drop(resultStack, false);
                }
                player.getInventory().setChanged();
            }
        } else {
            holder.setItemInHand(hand, resultStack);
        }
    }

    /**
     * 临时方块实体倒计时通用优化：单次读取并递减 NBT 计数，不发送每刻整块 NBT 同步包与客户端区块重绘标记。
     */
    public static double decrementBlockEntityTimer(LevelAccessor world, BlockPos pos, String tagKey) {
        if (world.isClientSide()) {
            return -1.0;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null) {
            return -1.0;
        }
        CompoundTag data = blockEntity.getPersistentData();
        double next = data.getDouble(tagKey) - 1.0;
        data.putDouble(tagKey, next);
        blockEntity.setChanged();
        return next;
    }

    public static void executeDarkIceTick(LevelAccessor world, double x, double y, double z) {
        if (world.isClientSide()) {
            return;
        }
        BlockPos pos = BlockPos.containing(x, y, z);
        if (decrementBlockEntityTimer(world, pos, "splitting") <= 0.0) {
            world.destroyBlock(pos, false);
            world.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        }
    }

    public static void executeFelSoilDestructibleTick(LevelAccessor world, double x, double y, double z) {
        if (world.isClientSide()) {
            return;
        }
        BlockPos pos = BlockPos.containing(x, y, z);
        if (decrementBlockEntityTimer(world, pos, "destructible") <= 0.0) {
            world.destroyBlock(pos, false);
        }
    }

    public static void executePuddleTick(LevelAccessor world, double x, double y, double z, boolean intoxication) {
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    intoxication ? BornInChaosV1ModParticleTypes.INTOXICATIND_BOMB_PART.get() : BornInChaosV1ModParticleTypes.STIMULATINGBUBBLES.get(),
                    x + 0.5, y, z + 0.5, 1, 0.2, 0.2, 0.2, 0.2
            );
        }
        BlockPos pos = BlockPos.containing(x, y, z);
        if (!world.isClientSide() && decrementBlockEntityTimer(world, pos, "puddle") <= 0.0) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), x + 0.5, y + 0.5, z + 0.5, 5, 0.3, 0.3, 0.3, 0.1);
            }
            if (world instanceof Level level) {
                if (!level.isClientSide()) {
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.3f, 1.0f);
                } else {
                    level.playLocalSound(x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.3f, 1.0f, false);
                }
            }
            return;
        }
        if (!world.getBlockState(BlockPos.containing(x, y - 1.0, z)).canOcclude()) {
            world.destroyBlock(pos, false);
        }
    }

    /**
     * 服务端节流且精准按目标召唤物子类扫描驯服，消除客户端无意义扫描与全实体 Stream 排序。
     */
    public static <T extends TamableAnimal> void tameNearbySummons(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity holder,
            double inflate,
            Class<T> targetClass
    ) {
        if (world.isClientSide() || !(holder instanceof Player owner)) {
            return;
        }
        if ((owner.tickCount & 3) != 0) {
            return;
        }
        AABB box = new AABB(x - inflate, y - inflate, z - inflate, x + inflate, y + inflate, z + inflate);
        List<T> found = world.getEntitiesOfClass(targetClass, box, e -> !e.isTame());
        for (int i = 0, size = found.size(); i < size; i++) {
            found.get(i).tame(owner);
        }
    }

    /**
     * 安全发放指定进度（带 ResourceLocation 缓存与判空保护）。
     */
    public static void awardAdvancementIfNotDone(ServerPlayer player, String advancementId) {
        ResourceLocation id = BicResourceAndAnimCache.parseLocation(advancementId);
        AdvancementHolder holder = player.server.getAdvancements().get(id);
        if (holder == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
        if (!progress.isDone()) {
            for (String criterion : progress.getRemainingCriteria()) {
                player.getAdvancements().award(holder, criterion);
            }
        }
    }

    /**
     * 安全检查目标落点是否适合南瓜爵士/南瓜头瞬移（双格高无遮挡，可选检测下方非水/岩浆）。
     */
    public static boolean isSafeTeleportSpot(LevelAccessor world, double tx, double ty, double tz, boolean checkFluidBelow) {
        BlockPos feetPos = BlockPos.containing(tx, ty, tz);
        if (world instanceof Level level && !level.hasChunkAt(feetPos)) {
            return false;
        }
        BlockPos headPos = BlockPos.containing(tx, ty + 1.0, tz);
        if (world.getBlockState(headPos).canOcclude()) {
            return false;
        }
        BlockState feetState = world.getBlockState(feetPos);
        if (feetState.canOcclude() && !feetState.is(Blocks.SNOW)) {
            return false;
        }
        if (checkFluidBelow) {
            Block below = world.getBlockState(feetPos.below()).getBlock();
            return below != Blocks.LAVA && below != Blocks.WATER;
        }
        return true;
    }

    public static void teleportEntityAndSyncPlayer(Entity entity, double tx, double ty, double tz) {
        entity.teleportTo(tx, ty, tz);
        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.teleport(tx, ty, tz, entity.getYRot(), entity.getXRot());
        }
    }

    public static void playSoundBothSides(LevelAccessor world, double x, double y, double z, String soundId, float volume, float pitch) {
        if (world instanceof Level level) {
            SoundEvent sound = BicResourceAndAnimCache.lookupRegistry(
                    net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT,
                    BicResourceAndAnimCache.parseLocation(soundId)
            );
            if (sound == null) {
                return;
            }
            if (!level.isClientSide()) {
                level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, volume, pitch);
            } else {
                level.playLocalSound(x, y, z, sound, SoundSource.NEUTRAL, volume, pitch, false);
            }
        }
    }

    public static void spawnPoofAndTeleportSound(LevelAccessor world, double x, double y, double z) {
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, 9, 1.0, 1.0, 1.0, 1.0);
        }
        if (!world.isClientSide()) {
            playSoundBothSides(world, x, y, z, "entity.enderman.teleport", 0.8f, 1.0f);
        }
    }
}
