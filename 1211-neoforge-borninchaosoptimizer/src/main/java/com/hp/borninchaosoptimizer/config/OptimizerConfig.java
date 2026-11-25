package com.hp.borninchaosoptimizer.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 生于混沌优化模组通用配置定义。
 */
public final class OptimizerConfig {
    public static final ModConfigSpec SPEC;

    // 全局事件、网络与队列
    public static final ModConfigSpec.BooleanValue FIX_WORK_QUEUE_LEAK;
    public static final ModConfigSpec.BooleanValue OPTIMIZE_ENTITY_ANIMATION_SYNC;
    public static final ModConfigSpec.BooleanValue OPTIMIZE_ITEM_ANIMATION_SYNC;
    public static final ModConfigSpec.BooleanValue FIX_SCOREBOARD_LEAK;
    public static final ModConfigSpec.BooleanValue CLEAN_LEGACY_SCOREBOARD_ENTRIES;
    public static final ModConfigSpec.BooleanValue FIX_EFFECT_REMOVAL_PREMATURE_TRIGGER;
    public static final ModConfigSpec.BooleanValue FIX_BOSS_PHASE_HEALTH_CHECK;
    public static final ModConfigSpec.BooleanValue FIX_DEDICATED_SERVER_CLIENT_CRASH;
    public static final ModConfigSpec.BooleanValue FIX_KRAMPUS_DISARM_AND_SHIELD;
    public static final ModConfigSpec.BooleanValue FIX_THORNS_REENTRANT_AND_LOOKUP;
    public static final ModConfigSpec.BooleanValue FIX_SEASONAL_RELOAD_OVERWRITE;

    // 生物 AI、寻路与每刻逻辑
    public static final ModConfigSpec.BooleanValue OPTIMIZE_CONSTANT_ENTITY_DIMENSIONS;
    public static final ModConfigSpec.BooleanValue FIX_DOUBLE_DEATH_EXPERIENCE;
    public static final ModConfigSpec.BooleanValue OPTIMIZE_SKY_AND_SUNBURN_CHECKS;
    public static final ModConfigSpec.BooleanValue OPTIMIZE_FLYING_PATHFINDING;
    public static final ModConfigSpec.BooleanValue CAP_SUMMONED_PEST_DENSITY;
    public static final ModConfigSpec.IntValue MAX_NEARBY_PEST_COUNT;
    public static final ModConfigSpec.IntValue ROTTEN_SMELL_PARTICLE_INTERVAL;
    public static final ModConfigSpec.BooleanValue FIX_CUSTOM_GOAL_NULL_TARGET;
    public static final ModConfigSpec.BooleanValue FIX_BOSS_TELEPORT_INTO_WALLS;
    public static final ModConfigSpec.BooleanValue FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE;
    public static final ModConfigSpec.BooleanValue FIX_BOSS_AND_MINION_DUPLICATE_SPAWN;
    public static final ModConfigSpec.BooleanValue CAP_MINION_AND_SWARM_SPAWNING;
    public static final ModConfigSpec.IntValue MAX_NEARBY_SUMMONED_MINIONS;
    public static final ModConfigSpec.IntValue MINION_CAP_RADIUS;
    public static final ModConfigSpec.BooleanValue ALLOW_WILD_THORNSHELL_CRAB_DESPAWN;

    // 药水效果与状态循环
    public static final ModConfigSpec.BooleanValue REPLACE_LOST_ITEMS_COMMAND_WITH_API;
    public static final ModConfigSpec.BooleanValue OPTIMIZE_EFFECT_TICK_SCANS;

    // 客户端渲染与资源缓存
    public static final ModConfigSpec.BooleanValue CACHE_RESOURCE_LOCATIONS_AND_SOUNDS;
    public static final ModConfigSpec.BooleanValue CACHE_CUSTOM_ARMOR_MODELS;
    public static final ModConfigSpec.BooleanValue CACHE_RAW_ANIMATIONS;
    public static final ModConfigSpec.BooleanValue OPTIMIZE_GLOW_LAYERS_AND_RENDER_TYPES;
    public static final ModConfigSpec.BooleanValue OPTIMIZE_OPAQUE_ENTITY_RENDER_TYPES;

    // 方块、物品与弹射物兜底修复
    public static final ModConfigSpec.BooleanValue FIX_EQUIPMENT_DURABILITY_CONTEXT;
    public static final ModConfigSpec.BooleanValue FIX_NUT_HAMMER_DISARM;
    public static final ModConfigSpec.BooleanValue FIX_CONSUMABLE_CREATIVE_AND_COOLDOWN;
    public static final ModConfigSpec.BooleanValue FIX_TRAP_AND_AOE_NON_LIVING_DAMAGE;
    public static final ModConfigSpec.BooleanValue OPTIMIZE_TEMPORARY_BLOCK_ENTITY_SYNC;
    public static final ModConfigSpec.IntValue TEMPORARY_BLOCK_TICK_INTERVAL;
    public static final ModConfigSpec.BooleanValue OPTIMIZE_EQUIPMENT_INVENTORY_TICKS;
    public static final ModConfigSpec.BooleanValue FIX_MISSING_ADVANCEMENT_NULL_CRASH;
    public static final ModConfigSpec.BooleanValue FIX_PROJECTILE_PICKUP_DUPE;
    public static final ModConfigSpec.BooleanValue OPTIMIZE_STATIC_TRAP_BLOCK_TICKS;
    public static final ModConfigSpec.BooleanValue FIX_CLIENT_SIDE_GHOST_MUTATIONS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("General, Global Events & Network Queue / 全局事件、网络包与延时队列优化").push("general_and_events");
        FIX_WORK_QUEUE_LEAK = builder
                .comment("Fix BornInChaosV1Mod.workQueue memory leak on <=0 tick tasks, exception residue, O(N*M) removal, and cross-world retention / 修复全局延时队列 <=0 刻任务永久泄漏、异常中断残留、O(N*M) 移除及退出存档残留问题")
                .define("fixWorkQueueLeak", true);
        OPTIMIZE_ENTITY_ANIMATION_SYNC = builder
                .comment("Optimize EntityAnimationFactory from 83 sequential instanceof checks per entity per tick to a single O(1) interface check / 将实体动画同步从全服每实体每刻 83 次顺序 instanceof 检查优化为单次 O(1) 接口分发")
                .define("optimizeEntityAnimationSync", true);
        OPTIMIZE_ITEM_ANIMATION_SYNC = builder
                .comment("Eliminate unconditional ItemStack.copy() and CompoundTag deep copies every player tick in ItemAnimationFactory / 消除玩家每刻无条件深拷贝主副手物品栈与 NBT 标签的开销")
                .define("optimizeItemAnimationSync", true);
        FIX_SCOREBOARD_LEAK = builder
                .comment("Replace global ServerScoreboard UUID score writes (Door Knight, Skeleton Thrasher, Thornshell Crab) with entity PersistentData to prevent scoreboard.dat bloat and packet spam / 将门骑士、碎骨骷髅、刺壳蟹对全局计分板的永久 UUID 写入替换为实体自身 NBT 状态，根除存档计分板膨胀与全服发包风暴")
                .define("fixScoreboardLeak", true);
        CLEAN_LEGACY_SCOREBOARD_ENTRIES = builder
                .comment("Clean up leftover dead-entity UUID entries under 'bloc', 'blocc', and 'eat' objectives on server start / 服务器启动时自动清理历史遗留在 bloc、blocc、eat 计分板项下的死实体 UUID 记录")
                .define("cleanLegacyScoreboardEntries", true);
        FIX_EFFECT_REMOVAL_PREMATURE_TRIGGER = builder
                .comment("Prevent Cursed Mark and Undead Summon from prematurely spawning ambush mobs when cured by Totem or /effect clear / 修复诅咒印记与亡灵召唤被图腾或指令驱散时反而立即触发惩罚召唤的缺陷")
                .define("fixEffectRemovalPrematureTrigger", true);
        FIX_BOSS_PHASE_HEALTH_CHECK = builder
                .comment("Fix Lord Pumpkinhead and Lord The Headless comparing boss health against victim max health instead of boss max health, and fix friendly buffing of enemy mobs / 修复南瓜领主与无头领主将自身生命与受击者最大生命对比导致二阶段技能失效及误给敌人加力量效果的缺陷")
                .define("fixBossPhaseHealthCheck", true);
        FIX_DEDICATED_SERVER_CLIENT_CRASH = builder
                .comment("Fix NoClassDefFoundError crashes on dedicated servers when mining Black Argillite / Dark Metal Deposit / Rotten Soil or firing Pumpkin Pistol / Staff of Magic Arrows / 修复专用服务端挖掘黑泥岩、暗金属矿、腐壤或发射南瓜手枪与魔矢法杖时加载客户端类导致崩服的缺陷")
                .define("fixDedicatedServerClientCrash", true);
        FIX_KRAMPUS_DISARM_AND_SHIELD = builder
                .comment("Fix Krampus & Henchman spawning empty ItemEntities on bare-handed players and eliminate 41-slot inventory deep-copy loops / 修复坎卜斯及爪牙对空手玩家生成空掉落物、消除无意义的 41 格背包深拷贝循环并补全破盾上下文")
                .define("fixKrampusDisarmAndShield", true);
        FIX_THORNS_REENTRANT_AND_LOOKUP = builder
                .comment("Add reentrancy guard to retaliation damage procedures and avoid redundant registry lookups when Spiny Shell armor is not worn / 为反伤过程添加防重入保护避免死循环崩服，并消除未穿刺壳甲时的冗余附魔注册表查询")
                .define("fixThornsReentrantAndLookup", true);
        FIX_SEASONAL_RELOAD_OVERWRITE = builder
                .comment("Prevent seasonal spawn handlers from re-rolling and overwriting mob equipment on every chunk reload, and cache Calendar lookups / 防止季节性刷怪事件在每次区块重载时重复判定并覆盖怪物已有装备，同时缓存日历查询")
                .define("fixSeasonalReloadOverwrite", true);
        builder.pop();

        builder.comment("Entity AI, Pathfinding & Tick Optimizations / 生物 AI、寻路与每刻逻辑优化").push("ai_and_entities");
        OPTIMIZE_CONSTANT_ENTITY_DIMENSIONS = builder
                .comment("Skip redundant refreshDimensions() calls every tick on entities whose getDefaultDimensions uses a constant scale / 跳过恒定尺寸生物在 baseTick 中每刻无意义调用的 refreshDimensions()（避免每刻双端触发 EntityEvent.Size 与碰撞箱重建）")
                .define("optimizeConstantEntityDimensions", true);
        FIX_DOUBLE_DEATH_EXPERIENCE = builder
                .comment("Fix 1.21.1 double experience orb drop and client-side premature removal in MCreator entity tickDeath overrides / 修复 1.21.1 生物死亡时掉落双倍经验球以及客户端提前本地销毁实体的缺陷")
                .define("fixDoubleDeathExperience", true);
        OPTIMIZE_SKY_AND_SUNBURN_CHECKS = builder
                .comment("Throttle expensive per-tick canSeeSkyFromBelowWater / block scans and redundant effect re-applications on entities / 优化生物每刻昂贵的水下天空视线扫描与重复施加同名药水效果的开销")
                .define("optimizeSkyAndSunburnChecks", true);
        OPTIMIZE_FLYING_PATHFINDING = builder
                .comment("Throttle excessive per-tick A* path recalculations on Scarlet Persecutor and flying spirits that cause 50-100ms server spikes / 限制猩红追猎者与飞行幽灵每刻高频重算三维 A* 寻路导致的 50-100ms 服务端卡顿尖峰")
                .define("optimizeFlyingPathfinding", true);
        CAP_SUMMONED_PEST_DENSITY = builder
                .comment("Cap nearby Corpse Fly and Maggot density spawned by Rotten Smell / Myiasis /Infestation to prevent entity explosion lag / 限制腐臭、蝇蛆病等效果在局部区域无限繁殖尸蝇与蛆虫的密度上限，防止实体爆炸性堆积卡死客户端")
                .define("capSummonedPestDensity", true);
        MAX_NEARBY_PEST_COUNT = builder
                .comment("Maximum number of Corpse Flies or Maggots allowed within 16 blocks before effect-based spawning pauses / 半径 16 格内允许通过药水效果继续滋生的尸蝇或蛆虫最大数量")
                .defineInRange("maxNearbyPestCount", 24, 4, 256);
        ROTTEN_SMELL_PARTICLE_INTERVAL = builder
                .comment("Server particle packet interval in ticks for Rotten Smell ambient flies (1 = every tick) / 腐臭效果服务端环境苍蝇粒子发包间隔刻数")
                .defineInRange("rottenSmellParticleInterval", 4, 1, 40);
        FIX_CUSTOM_GOAL_NULL_TARGET = builder
                .comment("Guard custom Goal.tick() implementations against null or dead targets on odd server AI ticks, and throttle per-tick RangedAttackGoal repathing / 为自定义 AI Goal.tick() 增加目标判空与存活保护，防止奇数刻空指针崩服，并限制远程攻击目标每刻高频重算寻路")
                .define("fixCustomGoalNullTarget", true);
        FIX_BOSS_TELEPORT_INTO_WALLS = builder
                .comment("Fix Lord Pumpkinhead and Sir Pumpkinhead hit-teleport procedures checking wrong coordinates for snow and unconditionally teleporting into solid walls on the Z- fallback branch / 修复南瓜领主与南瓜爵士受击瞬移时坐标写反及兜底分支不检查方块阻挡直接瞬移进实心墙体窒息的缺陷")
                .define("fixBossTeleportIntoWalls", true);
        FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE = builder
                .comment("Fix Mother Spider and Nightmare Stalker triggering on-hurt summons/rage before immunity & super.hurt checks, and fix Mother Spider's second baby spider particle coordinate / 修复母蜘蛛与梦魇潜行者在摔落/箭矢免疫及无敌帧判定前误触发受击召唤与怒气累加，并修正母蜘蛛第二只幼蛛粒子坐标")
                .define("fixEntityHurtOrderAndSummonParticle", true);
        FIX_BOSS_AND_MINION_DUPLICATE_SPAWN = builder
                .comment("Prevent same-tick duplicate Boss phase spawns (Lifestealer, Supreme Bonescaller, Lord/Sir Pumpkinhead, Phantom Creeper) and fix Controlled Baby Skeleton / Mr. Pumpkin respawning after expiration / 修复首领转阶段与幻影苦力怕同刻多段伤害刷出双倍首领，以及受控幼年骷髅与受控南瓜先生抗性到期消失后原地无限重生的缺陷")
                .define("fixBossAndMinionDuplicateSpawn", true);
        CAP_MINION_AND_SWARM_SPAWNING = builder
                .comment("Cap nearby summoned minion and swarm entity density from Swarmer, Mother Spider, Bonescaller, Spirit Guide, and Undead Summon to prevent runaway entity flooding / 限制虫群宿主、母蜘蛛、唤骨者、灵魂向导、亡灵召唤等高频衍生召唤的局部同类实体密度上限，防止无限堆怪卡服")
                .define("capMinionAndSwarmSpawning", true);
        MAX_NEARBY_SUMMONED_MINIONS = builder
                .comment("Maximum alive entities of the same summoned type allowed within minionCapRadius before pausing extra spawns / 检测半径内允许存在的同类衍生实体最大数量上限（默认 24，不影响正常战斗）")
                .defineInRange("maxNearbySummonedMinions", 24, 4, 128);
        MINION_CAP_RADIUS = builder
                .comment("Radius in blocks to count nearby summoned minions/swarms / 衍生实体密度统计半径（格）")
                .defineInRange("minionCapRadius", 24, 8, 64);
        ALLOW_WILD_THORNSHELL_CRAB_DESPAWN = builder
                .comment("Allow wild naturally spawned Thornshell Crabs to despawn when far away so they do not permanently clog the 10-slot CREATURE cap; fed, bred, named, or leashed crabs remain persistent / 修复野生刺壳蟹自然刷新却永久不消失导致长期占满 CREATURE（10 只）动物上限的缺陷；被玩家喂食、繁殖、命名或拴绳的刺壳蟹仍永久保留")
                .define("allowWildThornshellCrabDespawn", true);
        builder.pop();

        builder.comment("Mob Effects Optimizations / 药水效果优化与缺陷修复").push("mob_effects");
        REPLACE_LOST_ITEMS_COMMAND_WITH_API = builder
                .comment("Replace per-tick '/execute as @e[type=item...]' command parsing in Lost Items effect with direct AABB ItemEntity lookup / 将丢失物品效果每刻执行的原版命令解析替换为原生范围 ItemEntity API，彻底消除命令解析器开销")
                .define("replaceLostItemsCommandWithApi", true);
        OPTIMIZE_EFFECT_TICK_SCANS = builder
                .comment("Optimize expensive AABB entity scans and redundant allocations in MobEffect tick procedures / 优化药水效果每刻过程中的大范围实体扫描与冗余排序分配")
                .define("optimizeEffectTickScans", true);
        builder.pop();

        builder.comment("Client Rendering & Allocation Caches / 客户端渲染管线与对象缓存优化").push("client_rendering");
        CACHE_RESOURCE_LOCATIONS_AND_SOUNDS = builder
                .comment("Cache ResourceLocation and SoundEvent lookups in GeoModels and entity sound methods instead of parsing strings every frame/step / 缓存 GeoModel 模型/贴图/动画路径与实体音效查找，消除每帧每步重复字符串解析分配")
                .define("cacheResourceLocationsAndSounds", true);
        CACHE_CUSTOM_ARMOR_MODELS = builder
                .comment("Cache baked HumanoidModel instances for Born in Chaos custom armors instead of calling bakeLayer() multiple times every render frame / 缓存生于混沌自定义 3D 盔甲烘焙模型，消除每渲染帧重复调用多次 bakeLayer() 重建网格的巨额开销")
                .define("cacheCustomArmorModels", true);
        CACHE_RAW_ANIMATIONS = builder
                .comment("Reuse immutable RawAnimation constants in GeckoLib predicates instead of allocating new RawAnimation builders every frame / 复用预构建的 RawAnimation 实例，消除每渲染帧在动画谓词中重复分配动画构建器的开销")
                .define("cacheRawAnimations", true);
        OPTIMIZE_GLOW_LAYERS_AND_RENDER_TYPES = builder
                .comment("Cache RenderType instances in GeoRenderLayers and entity renderers to reduce RenderType state churn and per-frame allocations while preserving all visual effects / 缓存发光层与实体渲染器的 RenderType 实例，在完全保留原有发光与半透明视觉表现的前提下降低每帧渲染状态开销")
                .define("optimizeGlowLayersAndRenderTypes", true);
        OPTIMIZE_OPAQUE_ENTITY_RENDER_TYPES = builder
                .comment("Automatically use entityCutoutNoCull for non-translucent entity and 3D item textures while preserving entityTranslucent for semi-transparent spirits/wings / 自动检测贴图半透明像素：对纯不透明/二值镂空贴图使用 entityCutoutNoCull 以消除每帧 CPU 面片深度排序与肢体穿模，同时完整保留半透明生物表现")
                .define("optimizeOpaqueEntityRenderTypes", true);
        builder.pop();

        builder.comment("Items, Blocks & Projectiles Fixes / 物品、方块与弹射物缺陷修复").push("items_and_blocks");
        FIX_EQUIPMENT_DURABILITY_CONTEXT = builder
                .comment("Pass proper LivingEntity & EquipmentSlot context to hurtAndBreak so Creative players do not lose durability and item break sounds/particles work properly / 为物品耐久扣减补全持有者与槽位上下文，修复创造模式误扣耐久及武器盾牌碎裂无音效粒子的缺陷")
                .define("fixEquipmentDurabilityContext", true);
        FIX_NUT_HAMMER_DISARM = builder
                .comment("Fix Nut Hammer enchanted helmet duplication, infinite mob helmet duplication without ItemHandler, and empty ItemEntity drops on bare-headed targets / 修复坚果锤击落附魔头盔刷物品漏洞、无容器能力生物无限刷头盔及对无头盔目标掉落空物品的缺陷")
                .define("fixNutHammerDisarm", true);
        FIX_CONSUMABLE_CREATIVE_AND_COOLDOWN = builder
                .comment("Fix single-stack consumables applying cooldown to Items.AIR after shrink(1), prevent Creative item consumption, and preserve stacked Fel Lamps / 修复消耗品单格使用时先扣数量导致将空气加入冷却（0冷却无限连点）、创造模式误扣消耗品及魔灯堆叠覆盖丢失的缺陷")
                .define("fixConsumableCreativeAndCooldown", true);
        FIX_TRAP_AND_AOE_NON_LIVING_DAMAGE = builder
                .comment("Prevent Hound Trap & Frostbitten Blade AOE from destroying dropped items/frames, fix Spiny Shell Trap fly immunity tautology, and fix Intoxicating Bomb per-entity particle multiplication / 修复捕兽夹与霜噬之刃范围技能误毁地面掉落物与展示框、刺壳陷阱逻辑恒真误伤飞虫及迷醉炸弹按实体数倍增粒子的缺陷")
                .define("fixTrapAndAoeNonLivingDamage", true);
        OPTIMIZE_TEMPORARY_BLOCK_ENTITY_SYNC = builder
                .comment("Eliminate redundant per-tick sendBlockUpdated NBT packets and client chunk re-renders on Dark Ice, Destructible Fel Soil, and Puddle blocks / 消除暗冰、可毁邪土、刺激水洼与迷醉水洼每刻无意义触发的整块 NBT 同步包与客户端区块重建风暴")
                .define("optimizeTemporaryBlockEntitySync", true);
        TEMPORARY_BLOCK_TICK_INTERVAL = builder
                .comment("Scheduled tick step interval for Dark Ice, Destructible Fel Soil, and Puddle blocks (total duration remains unchanged) / 暗冰、可毁邪土与药水水洼的倒计时合并调度步长（总持续时长保持不变）")
                .defineInRange("temporaryBlockTickInterval", 5, 1, 20);
        OPTIMIZE_EQUIPMENT_INVENTORY_TICKS = builder
                .comment("Optimize per-tick AABB entity scans on summoner staffs/hats and throttle redundant 10-30 tick effect packet spam on equipped armor & weapons / 优化召唤法杖与帽子在背包内每刻双端实体扫描，并节流盔甲武器每刻重复刷新短时药水导致的发包风暴")
                .define("optimizeEquipmentInventoryTicks", true);
        FIX_MISSING_ADVANCEMENT_NULL_CRASH = builder
                .comment("Guard PlayerAdvancements.getOrStartProgress against null AdvancementHolders when modpacks disable advancements via datapack / 防止整合包通过数据包移除进度后穿戴暗金属套、梦魇斗篷或触发进度检查时发生空指针崩服")
                .define("fixMissingAdvancementNullCrash", true);
        FIX_PROJECTILE_PICKUP_DUPE = builder
                .comment("Disallow player ground/deflect pickup on all 14 Born in Chaos AbstractArrow projectiles to prevent free Spiritual Dust / bomb duplication / 禁止玩家贴身或盾反拾取全部 14 种生于混沌弹射物，防止白嫖灵尘、炸弹与复活节彩蛋刷物品")
                .define("fixProjectilePickupDupe", true);
        OPTIMIZE_STATIC_TRAP_BLOCK_TICKS = builder
                .comment("Replace permanent 1-2 tick self-scheduling on Hound Trap, Closed Hound Trap, Spiny Shell Trap, and Cobweb Cover with neighborChanged-driven support checks / 将捕兽夹、闭合捕兽夹、刺壳陷阱与蛛网覆层的永久每 1~2 刻空转自调度优化为邻格更新驱动检测，使静置陷阱常驻服务端开销降为 0")
                .define("optimizeStaticTrapBlockTicks", true);
        FIX_CLIENT_SIDE_GHOST_MUTATIONS = builder
                .comment("Prevent client-side ghost block placements (Dark Ice, Fel Soil, Puddles), client desynced teleports, and fix Stimulation Splash particle loop multiplication / 拦截实体与弹射物在客户端生成未同步的幽灵暗冰/邪土/水洼与本地瞬移拉扯，并修复刺激药水瓶空地无粒子、怪堆千粒掉帧缺陷")
                .define("fixClientSideGhostMutations", true);
        builder.pop();

        SPEC = builder.build();
    }

    /**
     * 安全读取布尔配置，若配置尚未加载完成则回退到默认值，避免早期事件阶段抛出异常。
     */
    public static boolean isEnabled(ModConfigSpec.BooleanValue option) {
        try {
            if (SPEC.isLoaded()) {
                return option.get();
            }
        } catch (IllegalStateException ignored) {
        }
        return option.getDefault();
    }

    /**
     * 安全读取整数配置，若配置尚未加载完成则回退到默认值。
     */
    public static int getInt(ModConfigSpec.IntValue option) {
        try {
            if (SPEC.isLoaded()) {
                return option.get();
            }
        } catch (IllegalStateException ignored) {
        }
        return option.getDefault();
    }

    private OptimizerConfig() {
    }
}
