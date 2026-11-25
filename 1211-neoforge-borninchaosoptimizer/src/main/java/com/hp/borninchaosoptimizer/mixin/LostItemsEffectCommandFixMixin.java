package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import java.util.List;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.LostItemsKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 将丢失物品药水（Lost Items）每刻执行的原版 '/execute as @e[type=item,nbt={OnGround:1b}] run data merge' 命令解析与双次完整 NBT 序列化，
 * 替换为原生 AABB ItemEntity 查询与 setGlowingTag(true)，彻底消除每刻命令分发与整包 NBT 读写开销。
 */
@Mixin(value = LostItemsKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class LostItemsEffectCommandFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$replaceCommandWithNativeItemScan(LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.REPLACE_LOST_ITEMS_COMMAND_WITH_API)) {
            return;
        }
        ci.cancel();
        if (entity == null) {
            return;
        }
        if (world instanceof ServerLevel serverLevel) {
            AABB searchBox = new AABB(
                    entity.getX() - 10.0, entity.getY() - 10.0, entity.getZ() - 10.0,
                    entity.getX() + 10.0, entity.getY() + 10.0, entity.getZ() + 10.0
            );
            List<ItemEntity> groundItems = serverLevel.getEntitiesOfClass(
                    ItemEntity.class,
                    searchBox,
                    item -> item.onGround() && !item.isCurrentlyGlowing() && item.distanceToSqr(entity) <= 100.0
            );
            for (ItemEntity item : groundItems) {
                item.setGlowingTag(true);
            }
        }
        if (entity instanceof LivingEntity living) {
            MobEffectInstance instance = living.getEffect(BornInChaosV1ModMobEffects.LOST_ITEMS);
            if (instance != null && instance.getDuration() < 20) {
                living.removeEffect(BornInChaosV1ModMobEffects.LOST_ITEMS);
            }
        }
    }
}
