package com.hp.borninchaosoptimizer.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 生于混沌优化模组配置文件（包含服务端通用配置与客户端渲染优化配置）。
 * 提供安全的未加载阶段默认值回退读取方法，避免属性创建等早期阶段抛异常。
 */
public final class BicOptimizerConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final Common COMMON;
    public static final Client CLIENT;

    static {
        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
        COMMON = new Common(commonBuilder);
        COMMON_SPEC = commonBuilder.build();

        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        CLIENT = new Client(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();
    }

    private BicOptimizerConfig() {
    }

    /**
     * 安全读取布尔配置，若配置文件尚未加载则返回默认值。
     */
    public static boolean getBoolean(ForgeConfigSpec.BooleanValue value) {
        try {
            if (COMMON_SPEC.isLoaded() || CLIENT_SPEC.isLoaded()) {
                return value.get();
            }
        } catch (IllegalStateException ignored) {
        }
        return value.getDefault();
    }

    /**
     * 安全读取整数配置，若配置文件尚未加载则返回默认值。
     */
    public static int getInt(ForgeConfigSpec.IntValue value) {
        try {
            if (COMMON_SPEC.isLoaded() || CLIENT_SPEC.isLoaded()) {
                return value.get();
            }
        } catch (IllegalStateException ignored) {
        }
        return value.getDefault();
    }

    /**
     * 安全读取浮点配置，若配置文件尚未加载则返回默认值。
     */
    public static double getDouble(ForgeConfigSpec.DoubleValue value) {
        try {
            if (COMMON_SPEC.isLoaded() || CLIENT_SPEC.isLoaded()) {
                return value.get();
            }
        } catch (IllegalStateException ignored) {
        }
        return value.getDefault();
    }

    public static final class Common {
        public final ForgeConfigSpec.BooleanValue enableGlobalFixes;
        public final ForgeConfigSpec.BooleanValue optimizeWorkQueue;
        public final ForgeConfigSpec.BooleanValue optimizeAnimationFactories;
        public final ForgeConfigSpec.BooleanValue fixScoreboardEntityLeak;
        public final ForgeConfigSpec.BooleanValue fixSeasonalEvents;
        public final ForgeConfigSpec.BooleanValue fixGlobalCombatEvents;

        public final ForgeConfigSpec.BooleanValue fixMotherSpiderSynchedData;
        public final ForgeConfigSpec.BooleanValue fixGoalTargetNpe;
        public final ForgeConfigSpec.BooleanValue optimizeEntityPathfinding;
        public final ForgeConfigSpec.IntValue maxFlyingVisitedNodes;
        public final ForgeConfigSpec.IntValue maxGroundVisitedNodes;
        public final ForgeConfigSpec.IntValue maxPathSegmentRange;
        public final ForgeConfigSpec.BooleanValue skipRedundantRefreshDimensions;
        public final ForgeConfigSpec.BooleanValue fixEntityHurtOrder;
        public final ForgeConfigSpec.BooleanValue fixProjectilePickupDupe;
        public final ForgeConfigSpec.BooleanValue optimizeRangedAttackGoals;
        public final ForgeConfigSpec.BooleanValue capMinionAndSwarmSpawning;
        public final ForgeConfigSpec.IntValue maxNearbySummonedMinions;
        public final ForgeConfigSpec.IntValue minionCapRadius;
        public final ForgeConfigSpec.BooleanValue allowWildThornshellCrabDespawn;
        public final ForgeConfigSpec.BooleanValue fixSpawnConditions;
        public final ForgeConfigSpec.BooleanValue fixHardcodedToolChecks;
        public final ForgeConfigSpec.BooleanValue fixDeathProtectionEvents;
        public final ForgeConfigSpec.BooleanValue fixWarningMessageLocalization;

        public final ForgeConfigSpec.BooleanValue fixPotionRefreshPrematureExpiry;
        public final ForgeConfigSpec.BooleanValue fixPrematureCureExpiry;
        public final ForgeConfigSpec.BooleanValue optimizeRottenSmellEffect;
        public final ForgeConfigSpec.IntValue rottenSmellParticleInterval;
        public final ForgeConfigSpec.IntValue maxNearbyRottenSmellSummons;
        public final ForgeConfigSpec.BooleanValue optimizeLostItemsEffect;
        public final ForgeConfigSpec.IntValue lostItemsScanInterval;
        public final ForgeConfigSpec.BooleanValue optimizeMyiasisEffect;
        public final ForgeConfigSpec.IntValue maxNearbyMyiasisMaggots;
        public final ForgeConfigSpec.BooleanValue optimizePotionAuraAndStateTicks;
        public final ForgeConfigSpec.BooleanValue optimizeSubEffectRefreshes;

        public final ForgeConfigSpec.BooleanValue fixDedicatedServerClientRefs;
        public final ForgeConfigSpec.BooleanValue optimizeSunAndSkyTickProcedures;
        public final ForgeConfigSpec.IntValue sunCheckInterval;
        public final ForgeConfigSpec.BooleanValue fixBossTransformAndTeleportBugs;
        public final ForgeConfigSpec.BooleanValue fixBossAttackHealthAndBuff;
        public final ForgeConfigSpec.BooleanValue fixEntityTickClientSideMutations;
        public final ForgeConfigSpec.BooleanValue optimizeInventoryAndArmorTicks;
        public final ForgeConfigSpec.IntValue minionTameScanInterval;
        public final ForgeConfigSpec.BooleanValue fixNutHammerDisarm;
        public final ForgeConfigSpec.BooleanValue fixConsumableCooldownAndCreative;
        public final ForgeConfigSpec.BooleanValue fixDurabilityAndShieldContext;
        public final ForgeConfigSpec.BooleanValue fixNonLivingAoeAndTrapDamage;
        public final ForgeConfigSpec.BooleanValue fixMissingAdvancementCrash;

        public final ForgeConfigSpec.BooleanValue optimizeTemporaryTickingBlocks;
        public final ForgeConfigSpec.IntValue temporaryBlockTickInterval;
        public final ForgeConfigSpec.BooleanValue optimizeStaticTrapBlockTicks;
        public final ForgeConfigSpec.BooleanValue fixDedicatedServerItemHoverCrash;
        public final ForgeConfigSpec.BooleanValue cacheSoundsAndAnimations;

        private Common(ForgeConfigSpec.Builder builder) {
            builder.comment("通用修复与服务端性能优化设置 / General bug fixes and server performance optimizations")
                    .push("general");
            this.enableGlobalFixes = builder
                    .comment("总开关：启用生于混沌服务端修复与性能优化 / Master switch: enable Born in Chaos server bug fixes and optimizations")
                    .define("enableGlobalFixes", true);
            this.optimizeWorkQueue = builder
                    .comment("修复延时任务队列 O(N*M) 删除、<=0 永久泄漏与单人切档跨世界残留问题 / Fix workQueue O(N*M) removal, <=0 leak, and cross-world leftover tasks")
                    .define("optimizeWorkQueue", true);
            this.optimizeAnimationFactories = builder
                    .comment("优化 EntityAnimationFactory 83 次 instanceof 全服扫描与 ItemAnimationFactory 每 tick 深拷贝物品 NBT / Optimize EntityAnimationFactory 83x instanceof scan and ItemAnimationFactory per-tick ItemStack.copy()")
                    .define("optimizeAnimationFactories", true);
            this.fixScoreboardEntityLeak = builder
                    .comment("修复门骑士、碎骨骷髅、荆棘蟹使用原版计分板存储临时状态导致 scoreboard.dat 永久膨胀与进服大包问题 / Fix entity UUID leak into vanilla Scoreboard (scoreboard.dat bloat)")
                    .define("fixScoreboardEntityLeak", true);
            this.fixSeasonalEvents = builder
                    .comment("修复节日生成事件在区块重载时覆盖僵尸/骷髅已拾取装备的 Bug，并缓存系统日期消除高频 Calendar 分配 / Fix seasonal spawn overwriting picked-up gear on chunk reload and cache Calendar lookups")
                    .define("fixSeasonalEvents", true);
            this.fixGlobalCombatEvents = builder
                    .comment("修复克朗普斯双端随机删物品与 41 格冗余拷贝、窃命者双倍真身复制、反伤递归崩溃及破盾双端不同步问题 / Fix Krampus client ghost item loss, Lifestealer boss duplication, retaliation recursion, and shield durability desync")
                    .define("fixGlobalCombatEvents", true);
            builder.pop();

            builder.comment("实体 AI、寻路与状态修复设置 / Entity AI, pathfinding, and state fixes")
                    .push("entities");
            this.fixMotherSpiderSynchedData = builder
                    .comment("修复母蜘蛛 DATA_FLAGS_ID 误注册到幼年蜘蛛类导致的跨端网络解码崩溃 Bug / Fix MotherSpiderEntity registering SynchedEntityData onto BabySpiderEntity.class")
                    .define("fixMotherSpiderSynchedData", true);
            this.fixGoalTargetNpe = builder
                    .comment("修复不息之灵与南瓜先生自定义冲刺 Goal 在奇数 tick 目标死亡/清空时抛出 Ticking entity 空指针崩溃 / Fix NullPointerException in RestlessSpiritEntity and MrPumpkinEntity custom charge Goals")
                    .define("fixGoalTargetNpe", true);
            this.optimizeEntityPathfinding = builder
                    .comment("优化超远距离索敌生物（如猩红追猎者 150 格三维寻路、梦魇潜行者 80 格寻路）的分段寻路预算及飞行生物地下实心方块无效巡逻 / Optimize long-range 3D/ground pathfinding and prevent flying mobs from strolling into solid underground blocks")
                    .define("optimizeEntityPathfinding", true);
            this.maxFlyingVisitedNodes = builder
                    .comment("三维飞行寻路单次 A* 最大访问体素节点数上限（原模组猩红追猎者高达 2400） / Max visited A* nodes per 3D flying path calculation")
                    .defineInRange("maxFlyingVisitedNodes", 256, 64, 2400);
            this.maxGroundVisitedNodes = builder
                    .comment("超远距离地面寻路单次 A* 最大访问节点数上限（原模组梦魇潜行者高达 1280） / Max visited A* nodes per long-range ground path calculation")
                    .defineInRange("maxGroundVisitedNodes", 384, 64, 1600);
            this.maxPathSegmentRange = builder
                    .comment("单次局部寻路窗口最大分段距离（保持原索敌感知距离不变，远距离自动按此步长分段推进，大幅减少 PathNavigationRegion 区块抓取范围） / Max local pathfinding segment range in blocks")
                    .defineInRange("maxPathSegmentRange", 32, 16, 128);
            this.skipRedundantRefreshDimensions = builder
                    .comment("跳过全部 83 个固定恒等体型（scale 1.0）生物每 tick 双端无条件调用的 refreshDimensions() / Skip per-tick refreshDimensions() on constant-size entities")
                    .define("skipRedundantRefreshDimensions", true);
            this.fixEntityHurtOrder = builder
                    .comment("修复母蜘蛛、梦魇潜行者等生物在免疫伤害与无敌帧判定前误触发受击召唤/叠怒气的问题 / Fix entities triggering on-hurt summons/rage on immunized damage or client side")
                    .define("fixEntityHurtOrder", true);
            this.fixProjectilePickupDupe = builder
                    .comment("修复全部 14 种法杖/炸弹投射物可被触碰刷物品及修改静态单例 PROJECTILE_ITEM 导致后续弹体隐形的 Bug")
                    .define("fixProjectilePickupDupe", true);
            this.optimizeRangedAttackGoals = builder
                    .comment("优化全部 13 个远程攻击内部类 RangedAttackGoal：增加目标判空防崩与动态寻路重算冷却，消除每 tick 重跑完整 A* 的开销")
                    .define("optimizeRangedAttackGoals", true);
            this.capMinionAndSwarmSpawning = builder
                    .comment("限制虫群宿主、母蜘蛛、唤骨者、灵魂向导、亡灵召唤等高频衍生召唤在局部区域内的同类实体数量上限，防止无限堆怪卡服")
                    .define("capMinionAndSwarmSpawning", true);
            this.maxNearbySummonedMinions = builder
                    .comment("高频衍生召唤在检测半径内允许存在的同类实体最大数量上限（默认 24，不影响正常战斗）")
                    .defineInRange("maxNearbySummonedMinions", 24, 4, 128);
            this.minionCapRadius = builder
                    .comment("高频衍生召唤同类实体密度统计半径（格）")
                    .defineInRange("minionCapRadius", 24, 8, 64);
            this.allowWildThornshellCrabDespawn = builder
                    .comment("修复野生刺壳蟹自然刷新却永久不消失导致长期占满 CREATURE（10 只）动物上限的缺陷；被玩家喂食、繁殖、命名或拴绳的刺壳蟹仍永久保留")
                    .define("allowWildThornshellCrabDespawn", true);
            this.fixSpawnConditions = builder
                    .comment("修复腐尸鱼/暴食鱼在和平模式下绕过敌对怪拦截反复刷出销毁等自定义刷怪条件缺陷")
                    .define("fixSpawnConditions", true);
            this.fixHardcodedToolChecks = builder
                    .comment("将焦木去皮、焦木板雕削、持斧破盾与黑泥岩起挖中的硬编码 AxeItem/PickaxeItem 改为原版标准工具标签与 ToolActions 判定")
                    .define("fixHardcodedToolChecks", true);
            this.fixDeathProtectionEvents = builder
                    .comment("以 HIGH 事件优先级处理暗影守护与死亡图腾免死逻辑以兼容倒地复活模组，并修复掉虚空与 /kill 误消耗图腾、创造模式误耗及主副手力量等级不一致缺陷")
                    .define("fixDeathProtectionEvents", true);
            this.fixWarningMessageLocalization = builder
                    .comment("修复梦魇潜行者与传教士入侵警告在服务端提前硬转字符串导致专用服务端输出原始翻译键及无法按客户端语言显示的问题")
                    .define("fixWarningMessageLocalization", true);
            builder.pop();

            builder.comment("药水效果与状态过程修复优化设置")
                    .push("potions");
            this.fixPotionRefreshPrematureExpiry = builder
                    .comment("修复诅咒印记、亡灵召唤、暗影溅射在活跃期间续期/升级时误触发 removeAttributeModifiers 提前召唤猩红追猎者或亡灵的严重 Bug")
                    .define("fixPotionRefreshPrematureExpiry", true);
            this.fixPrematureCureExpiry = builder
                    .comment("修复不死图腾净化、牛奶或指令提前清除诅咒印记与亡灵召唤时反而立刻触发惩罚刷怪的缺陷")
                    .define("fixPrematureCureExpiry", true);
            this.optimizeRottenSmellEffect = builder
                    .comment("优化腐臭气味（Rotten Smell）：仅服务端权威执行、限制附近尸蝇/尸鱼繁殖密度上限并节流环境苍蝇粒子发包 / Optimize Rotten Smell summon cap, particle rate, and server-only state")
                    .define("optimizeRottenSmellEffect", true);
            this.rottenSmellParticleInterval = builder
                    .comment("腐臭气味环境粒子（FLI）服务端发包间隔 tick（原模组为 1，即每只怪每秒发包 20 次） / Server particle packet interval in ticks for Rotten Smell")
                    .defineInRange("rottenSmellParticleInterval", 4, 1, 40);
            this.maxNearbyRottenSmellSummons = builder
                    .comment("腐臭气味在周围 16 格内允许继续孵化尸蝇/尸鱼的最大同类数量上限（设为 0 表示不限制；原模组无上限会导致实体破百帧率暴跌） / Max nearby Corpse Flies or Corpse Fish within 16 blocks before pausing Rotten Smell spawns")
                    .defineInRange("maxNearbyRottenSmellSummons", 12, 0, 128);
            this.optimizeLostItemsEffect = builder
                    .comment("替换失物寻找药水每 tick 执行的完整 Brigadier 命令解析与全物品 NBT 重序列化，改用原生 Java 地面掉落物扫描 / Replace Lost Items per-tick performPrefixedCommand NBT merge with native ItemEntity scan")
                    .define("optimizeLostItemsEffect", true);
            this.lostItemsScanInterval = builder
                    .comment("失物寻找药水扫描周围地面掉落物并标记发光的间隔 tick / Scan interval in ticks for Lost Items glowing effect")
                    .defineInRange("lostItemsScanInterval", 5, 1, 40);
            this.optimizeMyiasisEffect = builder
                    .comment("修复蝇蛆症每 tick 双端无条件 removeEffect 刷爆事件总线的问题，并限制附近腐蛆孵化密度上限 / Optimize Myiasis per-tick removeEffect spam and cap nearby Maggot spawns")
                    .define("optimizeMyiasisEffect", true);
            this.maxNearbyMyiasisMaggots = builder
                    .comment("蝇蛆症在周围 16 格内允许孵化腐蛆的最大同类数量上限（设为 0 表示不限制） / Max nearby Maggots within 16 blocks for Myiasis")
                    .defineInRange("maxNearbyMyiasisMaggots", 16, 0, 128);
            this.optimizePotionAuraAndStateTicks = builder
                    .comment("修复暴风雪客户端幽灵霜冰方块、恐惧威压/暗影溅射/震荡打击/活体炸弹/地狱火等双端不同步状态篡改、冗余流式排序与无条件 removeEffect 事件风暴")
                    .define("optimizePotionAuraAndStateTicks", true);
            this.optimizeSubEffectRefreshes = builder
                    .comment("对尸蝇、腐尸鱼、暴食鱼、子药水维持及穿戴护甲手持武器的每 tick 短时药水刷新做平滑余量节流，不断档削减 80%+ 药水发包风暴")
                    .define("optimizeSubEffectRefreshes", true);
            builder.pop();

            builder.comment("过程逻辑修复与高频扫描优化设置")
                    .push("procedures");
            this.fixDedicatedServerClientRefs = builder
                    .comment("修复黑泥岩挖掘、暗金属矿脉、腐化土壤、南瓜手枪与魔箭法杖过程引用客户端 Minecraft 类引发的专用服务端崩溃风险及双端重复扣耐久问题")
                    .define("fixDedicatedServerClientRefs", true);
            this.optimizeSunAndSkyTickProcedures = builder
                    .comment("优化十余种亡灵与幽灵生物每 tick 双端无条件天空光照扫描，以及梦魇潜行者每 tick 日历与名称组件重复分配")
                    .define("optimizeSunAndSkyTickProcedures", true);
            this.sunCheckInterval = builder
                    .comment("亡灵白天自燃与幽灵日光消散的天空遮挡检测间隔 tick（按实体 ID 自动错峰）")
                    .defineInRange("sunCheckInterval", 5, 1, 20);
            this.fixBossTransformAndTeleportBugs = builder
                    .comment("修复全阶段首领与分裂生物同 tick 连击双倍复制、受控幼灵到期借尸还魂、转化药剂双端扣减，以及南瓜领主/南瓜爵士受击瞬移穿墙窒息与水火逻辑笔误")
                    .define("fixBossTransformAndTeleportBugs", true);
            this.fixBossAttackHealthAndBuff = builder
                    .comment("修复南瓜领主与无头领主攻击时误拿受击者最大生命值判定自身二阶段血量导致活体炸弹技能失效，以及无头领主把力量增益误加给敌人的 Bug")
                    .define("fixBossAttackHealthAndBuff", true);
            this.fixEntityTickClientSideMutations = builder
                    .comment("拦截实体与炸弹投射物过程在客户端放置幽灵方块、客户端独立随机瞬移、无条件清除药水事件风暴及炸弹粒子按实体数倍增问题")
                    .define("fixEntityTickClientSideMutations", true);
            this.optimizeInventoryAndArmorTicks = builder
                    .comment("优化召唤法杖与传教士帽每 tick 全实体流式排序驯服扫描，以及暗金属套四部件重复执行、无条件清凋零事件风暴与进度重复查询")
                    .define("optimizeInventoryAndArmorTicks", true);
            this.minionTameScanInterval = builder
                    .comment("背包法杖与传教士帽扫描周围未驯服召唤物的间隔 tick")
                    .defineInRange("minionTameScanInterval", 5, 1, 20);
            this.fixNutHammerDisarm = builder
                    .comment("修复坚果锤击落头盔时的附魔头盔复制漏洞、生物头盔复制漏洞、无头盔生成空掉落物实体及冗余背包槽位深拷贝")
                    .define("fixNutHammerDisarm", true);
            this.fixConsumableCooldownAndCreative = builder
                    .comment("修复单格消耗品先扣数量导致空气进冷却可无限连点的漏洞、创造模式误扣消耗品、诡异礼盒副手无挥动动画及魔灯堆叠覆盖丢失问题")
                    .define("fixConsumableCooldownAndCreative", true);
            this.fixDurabilityAndShieldContext = builder
                    .comment("修复伐木僵尸破盾将空气加入冷却、破防药水与劈木斧不支持模组自定义盾牌，以及全模组物品耐久扣减在创造模式生效和缺失破碎动画音效的问题")
                    .define("fixDurabilityAndShieldContext", true);
            this.fixNonLivingAoeAndTrapDamage = builder
                    .comment("修复猎犬夹、霜噬之刃、震荡打击、暗影溅射、暴风雪等陷阱和范围技能误销毁地面战利品掉落物、画框及传教士误伤自身与同阵营灾厄村民的缺陷")
                    .define("fixNonLivingAoeAndTrapDamage", true);
            this.fixMissingAdvancementCrash = builder
                    .comment("为全部 54 个使用进度的过程补充空进度防护，防止整合包通过数据包裁剪进度后触发空指针崩服，并节流暗金属侍从全背包扫描")
                    .define("fixMissingAdvancementCrash", true);
            builder.pop();

            builder.comment("方块调度、方块实体同步风暴与物品服务端防崩设置")
                    .push("blocks_and_items");
            this.optimizeTemporaryTickingBlocks = builder
                    .comment("优化暗冰、可破坏邪土、迷醉水洼与亢奋水洼：移除每 tick 无意义的 sendBlockUpdated 全服发包与客户端区块重编译，合并调度步长并修复 == 0.0 负数永久泄漏死循环")
                    .define("optimizeTemporaryTickingBlocks", true);
            this.temporaryBlockTickInterval = builder
                    .comment("暗冰、可破坏邪土与药水洼的倒计时调度步长 tick（总持续时间保持原模组不变）")
                    .defineInRange("temporaryBlockTickInterval", 5, 1, 20);
            this.optimizeStaticTrapBlockTicks = builder
                    .comment("消除猎犬夹、闭合猎犬夹、棘壳陷阱与蛛网覆层永久每 1~2 tick 空转自调度，改为邻居方块更新瞬时触发，并修复棘壳陷阱恒真笔误及客户端幽灵触发")
                    .define("optimizeStaticTrapBlockTicks", true);
            this.fixDedicatedServerItemHoverCrash = builder
                    .comment("修复 31 种物品在专用服务端被其他模组读取 appendHoverText 时加载客户端 Screen.hasShiftDown() 引发的崩溃风险")
                    .define("fixDedicatedServerItemHoverCrash", true);
            this.cacheSoundsAndAnimations = builder
                    .comment("缓存全部 98 种实体的音效注册表查询与 GeckoLib 预构建 RawAnimation 实例，消除每渲染帧与脚步声的重复对象分配 / Cache entity SoundEvent lookups and GeckoLib RawAnimation instances")
                    .define("cacheSoundsAndAnimations", true);
            builder.pop();
        }
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue enableClientOptimizations;
        public final ForgeConfigSpec.BooleanValue cacheGeoResourceLocations;
        public final ForgeConfigSpec.BooleanValue optimizeOpaqueEntityRenderTypes;
        public final ForgeConfigSpec.BooleanValue optimizeGlowLayers;
        public final ForgeConfigSpec.BooleanValue batchEntityGlowLayers;
        public final ForgeConfigSpec.DoubleValue glowLayerMaxDistance;
        public final ForgeConfigSpec.DoubleValue smallMobGlowMaxDistance;
        public final ForgeConfigSpec.BooleanValue cacheHumanoidArmorModels;

        private Client(ForgeConfigSpec.Builder builder) {
            builder.comment("客户端渲染与资源缓存优化设置 / Client rendering and resource caching optimizations")
                    .push("client");
            this.enableClientOptimizations = builder
                    .comment("总开关：启用生于混沌客户端渲染与缓存优化 / Master switch: enable Born in Chaos client rendering and cache optimizations")
                    .define("enableClientOptimizations", true);
            this.cacheGeoResourceLocations = builder
                    .comment("缓存全部 83 个实体模型与 4 个三维物品模型的 ResourceLocation，消除每帧每实体 4~6 次字符串拼接与新对象分配 / Cache GeoModel ResourceLocation instances per entity and model")
                    .define("cacheGeoResourceLocations", true);
            this.optimizeOpaqueEntityRenderTypes = builder
                    .comment("自动检测贴图 Alpha 通道：将无半透明像素的实体与三维武器从高开销 entityTranslucent（每帧 CPU 面片深度排序且导致肢体前后穿模）切换为 entityCutoutNoCull，同时完整保留幽灵/追猎者等半透明贴图表现 / Use entityCutoutNoCull for non-translucent textures while preserving translucent ones")
                    .define("optimizeOpaqueEntityRenderTypes", true);
            this.optimizeGlowLayers = builder
                    .comment("预编译全部 44 个发光层（*_e.png）的稀疏骨骼与发光面片索引（自动剔除平均 90% 以上完全透明的非发光立方体/面片，零视觉差异） / Prune empty non-glowing cubes and quads in all 44 GeoRenderLayer glow layers")
                    .define("optimizeGlowLayers", true);
            this.batchEntityGlowLayers = builder
                    .comment("在世界实体渲染阶段延迟合并静态发光层面片，使同类实体本体与发光层各自合并为单次 DrawCall，解决 BufferSource.endBatch 频繁断批占用 20%+ 渲染线程的问题（安装光影模组 Oculus/Iris 时自动让出） / Batch static glow layer quads at the end of entity pass to prevent per-entity BufferSource flushes")
                    .define("batchEntityGlowLayers", true);
            this.glowLayerMaxDistance = builder
                    .comment("普通体型生物发光眼层最大渲染距离（格，设为 0 表示不限制） / Max render distance in blocks for normal-sized entity glow layers (0 = unlimited)")
                    .defineInRange("glowLayerMaxDistance", 64.0, 0.0, 256.0);
            this.smallMobGlowMaxDistance = builder
                    .comment("微型虫类生物（高度 < 0.65 格，如尸蝇、幼蛛、血虻）发光层最大渲染距离（格，设为 0 表示不限制） / Max render distance in blocks for tiny bug glow layers (0 = unlimited)")
                    .defineInRange("smallMobGlowMaxDistance", 32.0, 0.0, 256.0);
            this.cacheHumanoidArmorModels = builder
                    .comment("缓存全部 8 套共 12 件三维自定义盔甲的烘焙骨骼网格与 HumanoidModel 实例（F3+T 自动刷新），消除每帧渲染穿戴者时重复 bakeLayer 与对象分配")
                    .define("cacheHumanoidArmorModels", true);
            builder.pop();
        }
    }
}
