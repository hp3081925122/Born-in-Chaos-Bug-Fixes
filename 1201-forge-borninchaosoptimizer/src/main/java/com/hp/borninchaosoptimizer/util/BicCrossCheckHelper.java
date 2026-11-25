package com.hp.borninchaosoptimizer.util;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 1.20.1 跨版本对账通用修复辅助类：
 * 封装空进度防崩兜底、坚果锤防复制卸除头盔、模组盾牌识别与完整耐久扣减上下文逻辑。
 */
public final class BicCrossCheckHelper {
    private static final ResourceLocation BONK_HIT_ID = ResourceLocation.tryParse("born_in_chaos_v1:bonk_hit");
    private static final AdvancementProgress COMPLETED_DUMMY_PROGRESS = new CompletedDummyProgress();
    private static final ThreadLocal<Item> CONSUMED_ITEM_HOLDER = new ThreadLocal<>();

    private BicCrossCheckHelper() {
    }

    /**
     * 当整合包数据包移除了生于混沌的某个进度时（advancement == null），
     * 返回 isDone() 为 true 且剩余条件为空的安全占位进度，既避免前置检查空指针又阻止 award(null, ...) 崩服。
     */
    public static AdvancementProgress safeGetOrStartProgress(PlayerAdvancements playerAdvancements, Advancement advancement) {
        if (advancement == null) {
            if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                    && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixMissingAdvancementCrash)) {
                return COMPLETED_DUMMY_PROGRESS;
            }
        }
        return playerAdvancements.getOrStartProgress(advancement);
    }

    /**
     * 判断物品是否为原版或模组自定义盾牌。
     */
    public static boolean isShield(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof ShieldItem || stack.canPerformAction(ToolActions.SHIELD_BLOCK);
    }

    /**
     * 判断实体是否处于创造或旁观模式（服务端与客户端均零客户端类直接依赖）。
     */
    public static boolean isCreativeOrSpectator(Entity entity) {
        if (entity instanceof Player player) {
            return player.getAbilities().instabuild || player.isSpectator();
        }
        return false;
    }

    /**
     * 使用完整的实体装备槽上下文扣减物品耐久并广播破碎音效粒子，创造模式自动免扣耐久。
     */
    public static void hurtItemWithContext(ItemStack stack, int amount, LivingEntity holder) {
        if (stack == null || stack.isEmpty() || amount <= 0 || holder == null) {
            return;
        }
        if (isCreativeOrSpectator(holder)) {
            return;
        }
        EquipmentSlot matchedSlot = resolveHoldingSlot(holder, stack);
        if (matchedSlot != null) {
            stack.hurtAndBreak(amount, holder, entity -> entity.broadcastBreakEvent(matchedSlot));
            return;
        }
        ServerPlayer sp = holder instanceof ServerPlayer s ? s : null;
        if (stack.hurt(amount, holder.getRandom(), sp)) {
            stack.shrink(1);
            stack.setDamageValue(0);
        }
    }

    /**
     * 对正在举盾格挡的实体扣减盾牌耐久并在扣碎前保留真实盾牌 Item 引用以施加冷却。
     */
    public static Item hurtBlockingShield(LivingEntity target, int amount) {
        ItemStack useStack = target.getUseItem();
        if (!isShield(useStack)) {
            return null;
        }
        Item shieldItem = useStack.getItem();
        InteractionHand usedHand = target.getUsedItemHand();
        if (!isCreativeOrSpectator(target)) {
            useStack.hurtAndBreak(amount, target, entity -> entity.broadcastBreakEvent(usedHand));
        }
        if (useStack.isEmpty()) {
            target.stopUsingItem();
        }
        return shieldItem;
    }

    private static EquipmentSlot resolveHoldingSlot(LivingEntity holder, ItemStack target) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (holder.getItemBySlot(slot) == target) {
                return slot;
            }
        }
        return null;
    }

    /**
     * 精确修复坚果锤击落头盔逻辑：
     * 仅在目标确实佩戴非空头盔时在服务端检定，直接清空 HEAD 槽位以根除附魔头盔与模组生物头盔复制漏洞。
     */
    public static void executeNutHammerDisarm(LevelAccessor world, Entity entity) {
        if (!(entity instanceof LivingEntity living) || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        ItemStack headStack = living.getItemBySlot(EquipmentSlot.HEAD);
        if (headStack.isEmpty()) {
            return;
        }
        boolean isPlayer = living instanceof Player;
        double chance = isPlayer ? 0.35 : 0.60;
        if (serverLevel.getRandom().nextDouble() >= chance) {
            return;
        }

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
                living.getX(),
                living.getY() + 1.5,
                living.getZ(),
                5,
                0.25,
                0.2,
                0.25,
                0.1
        );
        SoundEvent bonk = BONK_HIT_ID != null ? ForgeRegistries.SOUND_EVENTS.getValue(BONK_HIT_ID) : null;
        if (bonk != null) {
            float volume = isPlayer ? 0.8f : 0.6f;
            serverLevel.playSound(
                    null,
                    BlockPos.containing(living.getX(), living.getY() + 1.5, living.getZ()),
                    bonk,
                    SoundSource.NEUTRAL,
                    volume,
                    0.9f
            );
        }
    }

    /**
     * 在 Redirect ItemStack.hurt(int, RandomSource, ServerPlayer) 时统一应用创造免疫与破碎广播。
     */
    public static boolean redirectItemStackHurt(ItemStack stack, int amount, RandomSource random, ServerPlayer nullPlayer, Entity contextEntity) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixDurabilityAndShieldContext)) {
            return stack.hurt(amount, random, nullPlayer);
        }
        if (isCreativeOrSpectator(contextEntity)) {
            return false;
        }
        ServerPlayer sp = contextEntity instanceof ServerPlayer s ? s : nullPlayer;
        boolean broken = stack.hurt(amount, random, sp);
        if (broken && contextEntity instanceof LivingEntity living) {
            EquipmentSlot slot = resolveHoldingSlot(living, stack);
            if (slot != null) {
                living.broadcastBreakEvent(slot);
            }
        }
        return broken;
    }

    /**
     * 安全扣减消耗品数量：在数量归零前记录真实物品引用供冷却使用，并在创造或旁观模式下免扣数量。
     */
    public static void safeShrinkConsumable(ItemStack stack, int amount, Entity actor) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixConsumableCooldownAndCreative)) {
            stack.shrink(amount);
            return;
        }
        if (!stack.isEmpty()) {
            CONSUMED_ITEM_HOLDER.set(stack.getItem());
        }
        if (!isCreativeOrSpectator(actor)) {
            stack.shrink(amount);
        }
    }

    /**
     * 当单格消耗品被 shrink(1) 清空成空气栈后，还原其真实 Item 类型施加冷却。
     */
    public static void restoreActualItemCooldown(ItemCooldowns cooldowns, Item item, int ticks) {
        Item captured = CONSUMED_ITEM_HOLDER.get();
        CONSUMED_ITEM_HOLDER.remove();
        Item realItem = (item == Items.AIR && captured != null && captured != Items.AIR) ? captured : item;
        if (realItem != Items.AIR) {
            cooldowns.addCooldown(realItem, ticks);
        }
    }

    /**
     * 清理当前线程暂存的消耗品引用。
     */
    public static void clearConsumedItemThreadLocal() {
        CONSUMED_ITEM_HOLDER.remove();
    }

    /**
     * 魔灯与坐骑交互扣减物品时应用创造模式保护。
     */
    public static void shrinkLampItem(ItemStack stack, int amount, Entity actor) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixConsumableCooldownAndCreative)
                || !isCreativeOrSpectator(actor)) {
            stack.shrink(amount);
        }
    }

    /**
     * 当手持堆叠魔灯大于 1 时保留手中剩余堆叠，将新生成的空灯或满灯放入玩家背包或掉落。
     */
    public static void preserveRemainingLampStack(LivingEntity holder, InteractionHand hand, ItemStack resultStack) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixConsumableCooldownAndCreative)) {
            holder.setItemInHand(hand, resultStack);
            return;
        }
        ItemStack remaining = holder.getItemInHand(hand);
        if (remaining.isEmpty()) {
            holder.setItemInHand(hand, resultStack);
            return;
        }
        if (holder instanceof Player player) {
            if (!isCreativeOrSpectator(player) || !player.getInventory().contains(resultStack)) {
                if (!player.getInventory().add(resultStack)) {
                    player.drop(resultStack, false);
                }
            }
        } else {
            holder.setItemInHand(hand, resultStack);
        }
    }

    private static final class CompletedDummyProgress extends AdvancementProgress {
        @Override
        public boolean isDone() {
            return true;
        }
    }
}
