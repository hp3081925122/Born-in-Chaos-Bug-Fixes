# Born in Chaos Bug Fixes / 生于混沌 Bug 修复

<p align="center">
  <img src="curseforge/cf-project-icon.png" width="220" alt="Born in Chaos Bug Fixes" />
</p>

**Born in Chaos Bug Fixes** is a bug-fix and stability addon for **Born in Chaos** (`born_in_chaos_v1`), resolving dedicated server crashes, memory/save leaks, runaway mob summoning, and gameplay bugs while keeping original mechanics intact.

**Born in Chaos Bug Fixes（生于混沌 Bug 修复）** 是面向 **《生于混沌》（Born in Chaos）** 的缺陷修复与稳定性附属模组，在不改变原版玩法机制的前提下，集中修复专用服务端崩服、内存/存档泄漏、召唤堆怪与各类玩法漏洞。

---

## Project Modules / 版本目录

- `1201-forge-borninchaosoptimizer` — Minecraft `1.20.1` (Forge, requires `born_in_chaos_v1 >= 1.7.5`)
- `1211-neoforge-borninchaosoptimizer` — Minecraft `1.21.1` (NeoForge, requires `born_in_chaos_v1 >= 1.7.6`)

---

## Fixed Bugs / 修复的 Bug 汇总

- **Dedicated Server & Runtime Crashes / 专用服务端与运行期崩溃**
  - Fixed `NoClassDefFoundError` server crashes when mining Black Argillite, Dark Metal Deposit, or Rotten Soil, firing the Pumpkin Pistol or Staff of Magic Arrows, or reading item tooltips on a server.
  - Fixed odd-tick `NullPointerException` crashes in custom mob ranged AI goals when a target is cleared.
  - Fixed `NullPointerException` crashes when advancements are removed or modified by modpack datapacks.
  - Fixed `Mother Spider` entity data synchronization class mismatch and null/missing-tag issues during player variable sync.
- **Memory Leaks, World Save Bloat & Runaway Spawns / 内存泄漏、存档膨胀与堆怪漏洞**
  - Fixed the global delayed-task queue (`workQueue`) never removing `<= 0` tick tasks and carrying leftover tasks across world loads.
  - Fixed Door Knight, Skeleton Thrasher, and Thornshell Crab permanently writing entity UUID entries into `scoreboard.dat` instead of entity NBT (legacy scoreboard entries are cleaned up automatically on server start).
  - Fixed wild naturally-spawned Thornshell Crabs permanently setting `persistenceRequired` and never despawning (clogging the vanilla `CREATURE` cap); fed, bred, named, or leashed crabs remain persistent.
  - Added local density caps for runaway Corpse Fly / Maggot chain-breeding and high-frequency summoned minions (Swarmer, Mother Spider, Bonescaller, Supreme Bonescaller, Spirit Guide, Undead Summon, and zombie death maggots), fixed unsegmented ultra-long-range pathfinding server freezes, and fixed static traps infinitely rescheduling block ticks every tick.
- **Boss & Mob Combat Bugs / Boss 与生物战斗缺陷**
  - Fixed Lord Pumpkinhead and Lord The Headless checking the victim's max health instead of their own max health for phase-2 attacks.
  - Fixed Lord Pumpkinhead, Sir Pumpkinhead, and Pumpkinhead teleporting into solid blocks or lava when hit and suffocating.
  - Fixed multi-hit damage within the same tick spawning duplicate phase-2 Bosses or split mobs (Supreme Bonescaller, Lord/Sir Pumpkinhead, Decaying Zombie, Siamese Skeletons, Pumpkin Spirit) and fixed mobs dropping double death XP.
  - Fixed Pumpkin Bosses, Krampus, Lifestealer, Mother Spider, Nightmare Stalker, and Phantom Creeper triggering teleport, minion summon, rage, or split-vanish before damage immunity and invulnerability checks.
  - Fixed Wood Splitter Axe, Zombie Lumberjack, and Block Break failing to disable shields properly, and fixed Stunning Strike affecting the attacker or non-living entities.
- **Item Duplication, Consumables & World Interaction Bugs / 刷物品、消耗品与世界交互缺陷**
  - Fixed Nut Hammer duplicating enchanted helmets when disarming targets, and fixed infinite projectile material duplication via point-blank or shield-deflected pickup.
  - Fixed seasonal spawn checks overwriting armor and weapons looted by mobs upon chunk reload.
  - Fixed single-item consumables shrinking before cooldown registration (putting `Items.AIR` on cooldown for 0-cooldown spam), Fel Lamp wiping stacks larger than 1, Transmuting Elixir spawning duplicate converted mobs, and Creative/Spectator players losing consumables or item durability.
  - Fixed potion renewal or manual effect removal (Totem / commands) prematurely triggering expiry summon penalties.
  - Fixed Frostbitten Blade and Hound Trap destroying ground drops, XP orbs, and item frames, and fixed Intoxicating Bomb failing to spawn particles on empty ground or leaving client-side ghost puddles.
