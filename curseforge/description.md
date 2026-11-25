**Born in Chaos Optimizer** is a lightweight performance optimization and bug-fix addon for **Born in Chaos**, fixing crashes, memory/save leaks, severe server/client lag spikes, and gameplay bugs while keeping 100% of the original mechanics, visuals, and balance intact.

## Key Fixes & Optimizations

- **Dedicated Server & Runtime Crash Fixes**: Fixes `NoClassDefFoundError` dedicated server crashes when mining Black Argillite / Dark Metal Deposit / Rotten Soil or firing the Pumpkin Pistol / Staff of Magic Arrows, fixes odd-tick `NullPointerException` crashes in custom mob AI goals, and prevents `NullPointerException` crashes when modpacks remove advancements via datapacks.
- **Memory & Save-Bloat Leak Fixes**: Fixes `BornInChaosV1Mod.workQueue` leaking `<= 0` tick tasks forever, `O(N*M)` queue removal, and cross-world task carryover; replaces permanent entity UUID scoreboard entries (`bloc`, `blocc`, `eat` from Door Knight, Skeleton Thrasher, and Thornshell Crab) with entity NBT and cleans up legacy save bloat on startup.
- **Server TPS & Pathfinding Lag Fixes**: Eliminates 100ms+ 3D/ground pathfinding spikes on long-range mobs (such as Scarlet Persecutor and Nightmare Stalker) via segmented pathfinding without reducing aggro range; replaces per-tick `/execute` command parsing in the Lost Items effect with native AABB queries; caps uncontrolled Corpse Fly and Maggot chain-breeding density; replaces 83 sequential per-entity per-tick `instanceof` checks with `O(1)` dispatch; removes per-tick BlockEntity packet & chunk re-render spam on Dark Ice, Destructible Fel Soil, and Puddles; and eliminates idle per-tick self-scheduling on static traps.
- **Client FPS & Rendering Optimizations**: Eliminates shared-buffer thrashing and per-entity draw-call flushes on emissive glow layers while preserving full glow and transparency visuals; caches custom 3D armor models (stopping per-frame `bakeLayer` rebuilds), `ResourceLocation`s, sounds, and GeckoLib animations.
- **Gameplay & Item Duplication Fixes**: Fixes Boss phase-check health bugs (Lord Pumpkinhead / Lord The Headless), Boss hit-teleporting into solid walls or lava, same-tick duplicate Boss phase spawns, Mother Spider & Nightmare Stalker triggering on-hurt skills on immune damage, seasonal spawn handlers overwriting mob-looted gear on chunk reload, Nut Hammer enchanted-helmet duplication, projectile pickup duplication, single-stack consumables putting `Items.AIR` on cooldown (0-cooldown spam), Creative-mode durability/item consumption, and Frostbitten Blade / Hound Trap destroying ground loot and item frames.

## Configuration

All fixes and optimizations can be individually toggled in `borninchaosoptimizer-common.toml` (and `borninchaosoptimizer-client.toml` where applicable).

## Requirements

- Born in Chaos

## Source Code

- https://github.com/hp3081925122/Born-in-Chaos-Optimizer

---

**Born in Chaos Optimizer** 是 **生于混沌（Born in Chaos）** 的轻量级性能优化与缺陷修复附属模组，在 100% 保留原版机制、数值、动画与发光视觉效果的前提下，集中修复崩服、存档与内存泄漏、服务端/客户端严重卡顿以及各类玩法漏洞。

## 主要修复与优化

- **专用服务端与运行期崩溃修复**：修复挖掘黑泥岩、暗金属矿、腐壤以及使用南瓜手枪、魔矢法杖时加载客户端类导致专用服务端 `NoClassDefFoundError` 崩服的问题；修复自定义 AI 奇数刻目标为空导致的空指针崩服；修复整合包通过数据包裁剪进度后触发的空指针崩服。
- **内存泄漏与存档膨胀治理**：修复全局延时队列 `<= 0` 刻任务永久驻留泄漏、`O(N*M)` 移除开销与跨存档残留；将门骑士、碎骨骷髅、刺壳蟹向全局计分板永久写入实体 UUID 分数的逻辑改为实体自身 NBT，并在开服时自动清理旧存档残留的垃圾计分项。
- **服务端 TPS 与寻路卡顿治理**：在不缩短原有 150 格/100 格索敌距离的前提下，通过分段寻路消除猩红追猎者与梦魇潜行者单次加载数百区块造成的 100ms+ 寻路尖峰；将失物药水每刻原版 `/execute` 命令解析替换为原生范围查询；为腐臭与蝇蛆病增加局部尸蝇/蛆虫繁殖密度上限；将全服每实体每刻 83 次顺序 `instanceof` 优化为单次 `O(1)` 接口分发；消除暗冰、可毁邪土与药水水洼每刻全量 NBT 发包和区块网格重绘风暴，并移除静态陷阱方块的永久每刻空转自调度。
- **客户端 FPS 与渲染管线优化**：解决生物发光层与本体层抢占共享缓冲区导致的每帧断批冲洗（Buffer Thrashing），在完整保留发光与半透明表现的同时大幅降低 DrawCall 与面片排序开销；缓存全部自定义 3D 盔甲烘焙模型（消除每帧重复 `bakeLayer`）、资源路径、音效与动画实例。
- **玩法缺陷与刷物品漏洞修复**：修复南瓜领主/无头领主半血阶段误拿受击者最大生命对比、Boss 受击瞬移卡进实心墙体窒息、同刻多段伤害刷出双倍二阶段 Boss、母蜘蛛与梦魇潜行者在免疫伤害下误触发召唤/攒怒、区块重载覆盖怪物拾取的玩家装备、坚果锤击落附魔头盔刷物品、贴身/盾反白嫖弹射物材料、单格消耗品将空气加入冷却导致 0 冷却无限连点、创造模式误扣耐久/消耗品，以及霜噬之刃右键技能与捕兽夹误毁地面战利品和展示框等问题。

## 配置说明

所有修复与优化项均可在 `borninchaosoptimizer-common.toml`（及对应版本的客户端配置文件）中独立开关。

## 前置依赖

- Born in Chaos（生于混沌）

## 源码仓库

- https://github.com/hp3081925122/Born-in-Chaos-Optimizer
