**Born in Chaos Bug Fixes** is a bug-fix and stability addon for **Born in Chaos**, resolving server crashes, memory/save leaks, runaway mob spawning, and gameplay bugs while keeping original mechanics intact.

## Fixed Bugs

- **Dedicated Server & Runtime Crashes**
  - Fixed `NoClassDefFoundError` server crashes when mining Black Argillite, Dark Metal Deposit, or Rotten Soil, firing the Pumpkin Pistol or Staff of Magic Arrows, or reading item tooltips on a server.
  - Fixed odd-tick `NullPointerException` crashes in custom mob ranged AI goals when a target is cleared.
  - Fixed `NullPointerException` crashes when advancements are removed or modified by modpack datapacks.
  - Fixed `Mother Spider` entity data synchronization class mismatch and null/missing-tag issues during player variable sync.
- **Memory Leaks, World Save Bloat & Runaway Spawns**
  - Fixed the global delayed-task queue (`workQueue`) never removing `<= 0` tick tasks and carrying leftover tasks across world loads.
  - Fixed Door Knight, Skeleton Thrasher, and Thornshell Crab permanently writing entity UUID entries into `scoreboard.dat` instead of entity NBT (legacy scoreboard entries are cleaned up automatically on server start).
  - Fixed wild naturally-spawned Thornshell Crabs permanently setting `persistenceRequired` and never despawning (clogging the vanilla animal mob cap); fed, bred, named, or leashed crabs remain persistent.
  - Added local density caps for runaway Corpse Fly / Maggot chain-breeding and high-frequency minion/swarm summoning (Swarmer, Mother Spider, Bonescaller, Supreme Bonescaller, Spirit Guide, Undead Summon, and zombie death maggots), fixed unsegmented ultra-long-range pathfinding server freezes, and fixed static traps infinitely rescheduling block ticks every tick.
- **Boss & Mob Combat Bugs**
  - Fixed Lord Pumpkinhead and Lord The Headless checking the victim's max health instead of their own max health for phase-2 attacks.
  - Fixed Lord Pumpkinhead, Sir Pumpkinhead, and Pumpkinhead teleporting into solid blocks or lava when hit and suffocating.
  - Fixed multi-hit damage within the same tick spawning duplicate phase-2 Bosses or split mobs (Supreme Bonescaller, Lord/Sir Pumpkinhead, Decaying Zombie, Siamese Skeletons, Pumpkin Spirit) and fixed mobs dropping double death XP.
  - Fixed Pumpkin Bosses, Krampus, Lifestealer, Mother Spider, Nightmare Stalker, and Phantom Creeper triggering teleport, minion summon, rage, or split-vanish before damage immunity and invulnerability checks (such as immune arrows, fire, fall, or explosion damage).
  - Fixed Wood Splitter Axe, Zombie Lumberjack, and Block Break failing to disable shields properly, and fixed Stunning Strike affecting the attacker or non-living entities.
- **Item Duplication, Consumables & World Interaction Bugs**
  - Fixed Nut Hammer duplicating enchanted helmets when disarming targets, and fixed infinite projectile material duplication via point-blank or shield-deflected pickup.
  - Fixed seasonal spawn checks overwriting armor and weapons looted by mobs upon chunk reload.
  - Fixed single-item consumables shrinking before cooldown registration (which placed `Items.AIR` on cooldown and allowed 0-cooldown spam), Fel Lamp wiping stacks larger than 1, Transmuting Elixir spawning duplicate converted mobs, and Creative/Spectator players losing consumables or item durability.
  - Fixed potion renewal or manual effect removal (Totem / commands) prematurely triggering expiry summon penalties.
  - Fixed Frostbitten Blade and Hound Trap destroying ground drops, XP orbs, and item frames, and fixed Intoxicating Bomb failing to spawn particles on empty ground or leaving client-side ghost puddles.

## Configuration & Requirements

- All fixes can be toggled individually in `borninchaosoptimizer-common.toml` (and `borninchaosoptimizer-client.toml`).
- Requires the latest version of **Born in Chaos**.

## Source Code

- https://github.com/hp3081925122/Born-in-Chaos-Bug-Fixes

---

**Born in Chaos Bug Fixes（生于混沌 Bug 修复）** 是 **生于混沌（Born in Chaos）** 的缺陷修复与稳定性附属模组，在不改变原版玩法机制的前提下，集中修复崩服、内存/存档泄漏、刷怪堆积与各类玩法 Bug。

## 修复的 Bug 汇总

- **专用服务端与运行期崩溃**
  - 修复挖掘黑泥岩、暗金属矿、腐壤，使用南瓜手枪、魔矢法杖，以及服务端读取物品描述时因加载客户端类导致的 `NoClassDefFoundError` 崩服。
  - 修复自定义远程 AI 在奇数刻丢失目标时引发的空指针崩溃。
  - 修复整合包通过数据包移除或修改进度后触发的空指针崩溃。
  - 修复母蜘蛛实体同步数据注册类填错，以及玩家变量同步时的空指针与缺键覆盖问题。
- **内存泄漏、存档膨胀与堆怪漏洞**
  - 修复全局延时任务队列（`workQueue`）中 `<= 0` 刻任务永不移除的内存泄漏及跨存档残留问题。
  - 修复门骑士、碎骨骷髅、刺壳蟹将一次性实体 UUID 分数永久写入世界计分板（`scoreboard.dat`）导致的存档膨胀问题（改为实体自身 NBT，并在开服时自动清理旧存档残留计分项）。
  - 修复野生自然刷新的刺壳蟹永久标记不消失导致长期占满原版动物刷怪池上限的问题（被玩家喂食、繁殖、命名或拴绳的刺壳蟹仍永久保留）。
  - 修复腐臭/蝇蛆病无上限连锁繁殖，并为虫群宿主、母蜘蛛、唤骨者、至尊唤骨者、灵魂向导、亡灵召唤及僵尸产蛆等高频衍生召唤补充局部实体密度保护上限；修复超远距离单次寻路卡死服务端以及静态陷阱方块每刻无限空转自调度的问题。
- **Boss 与生物战斗缺陷**
  - 修复南瓜领主与无头领主判定二阶段血量时误读取受击目标最大生命值的逻辑错误。
  - 修复南瓜领主、南瓜爵士、南瓜头受击瞬移时卡进实心墙体或岩浆窒息的问题。
  - 修复同刻多段伤害导致二阶段 Boss 或分裂生物重复生成双份（至尊唤骨者、南瓜领主/爵士、腐烂僵尸、连体骷髅、南瓜魔灵），以及生物死亡掉落双倍经验的问题。
  - 修复各阶段南瓜首领、坎卜斯、窃命者真身、母蜘蛛、梦魇潜行者与幻影苦力怕在判定伤害免疫和无敌帧之前，就被免疫的箭矢、火焰、摔落或爆炸伤害提前触发瞬移、召怪、攒怒或分裂消失的问题。
  - 修复劈木斧、伐木僵尸与破盾效果无法正常令盾牌进入冷却，以及眩晕打击误伤攻击者自身或非生物实体的问题。
- **刷物品漏洞、消耗品与世界交互缺陷**
  - 修复坚果锤击落附魔头盔刷双份装备，以及贴身/盾反拾取弹射物无限白嫖材料的漏洞。
  - 修复区块重载时节日生成逻辑覆盖怪物已拾取的玩家装备与武器的问题。
  - 修复单格消耗品先扣物品再加冷却（导致对空气加冷却、可 0 冷却无限连点）、邪能提灯叠放使用时整组清空、嬗变灵药一次转化生成两只生物，以及创造/旁观模式误扣消耗品和耐久的问题。
  - 修复药水续杯或图腾/指令主动清除效果时错误触发到期惩罚召唤的问题。
  - 修复霜噬之刃与捕兽夹误杀地面掉落物、经验球和展示框，以及迷醉炸弹落空地无粒子、客户端残留幽灵积液方块的问题。

## 配置与前置

- 所有修复项均可在 `borninchaosoptimizer-common.toml`（及客户端配置文件）中独立开关。
- 前置需要安装最新版本的 **Born in Chaos（生于混沌）**。

## 源码仓库

- https://github.com/hp3081925122/Born-in-Chaos-Bug-Fixes
