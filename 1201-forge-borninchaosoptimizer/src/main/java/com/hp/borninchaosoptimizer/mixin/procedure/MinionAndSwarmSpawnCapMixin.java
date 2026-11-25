package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.util.BicSummonCapHelper;
import net.mcreator.borninchaosv.procedures.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 为生于混沌 13 个单签名高频召唤/分裂/虫群衍生过程增加局部同类实体密度上限，
 * 防止绕过原版刷怪池上限造成局部实体堆积卡服。
 */
@Mixin({
        BonecallProcedure.class,
        BonecallerSupRanProcedure.class,
        BonescallerKoghdaSushchnostRanienaProcedure.class,
        MotherSpiderDeathTimeIsReachedProcedure.class,
        SpiritGudSproProcedure.class,
        SwarmerPriRanieniiSushchnostiProcedure.class,
        SwarmerDeathTimeIsReachedProcedure.class,
        UndeadSummonunKazhdyiTikVoVriemiaEffiektaProcedure.class,
        BarrelZombieDeathTimeIsReachedProcedure.class,
        DoorKnightDeathTimeIsReachedProcedure.class,
        ZombieBruiserdedProcedure.class,
        ZombieClownDeathTimeIsReachedProcedure.class,
        ZombieFishermandeathProcedure.class
})
public abstract class MinionAndSwarmSpawnCapMixin {
    @Redirect(
            method = "execute",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;)Lnet/minecraft/world/entity/Entity;",
                    remap = true
            )
    )
    private static <T extends Entity> T bic$capSummonedEntitySpawn(
            EntityType<T> entityType, ServerLevel level, BlockPos pos, MobSpawnType spawnType
    ) {
        return BicSummonCapHelper.spawnWithCap(entityType, level, pos, spawnType);
    }
}
