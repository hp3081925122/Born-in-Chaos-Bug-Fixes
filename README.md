# Born in Chaos Optimizer

<p align="center">
  <img src="curseforge/cf-project-icon.png" width="220" alt="Born in Chaos Optimizer" />
</p>

**Born in Chaos Optimizer** is a lightweight performance optimization and bug-fix addon for **Born in Chaos** (`born_in_chaos_v1`), available for **Forge 1.20.1** and **NeoForge 1.21.1**.

**Born in Chaos Optimizer** 是面向 **《生于混沌》（Born in Chaos）** 的性能优化与缺陷修复附属模组（支持 **Forge 1.20.1** 与 **NeoForge 1.21.1**），在 100% 保留原版机制、数值、动画与视觉表现的前提下集中治理崩服、泄漏、卡顿与玩法漏洞。

---

## Project Modules / 版本目录

- `1201-forge-borninchaosoptimizer` — Minecraft `1.20.1` (Forge)
- `1211-neoforge-borninchaosoptimizer` — Minecraft `1.21.1` (NeoForge)

---

## Highlights / 核心修复与优化

- **Crash Fixes / 崩溃修复**：Dedicated server `NoClassDefFoundError` fixes (Black Argillite, Dark Metal Deposit, Rotten Soil, Pumpkin Pistol, Staff of Magic Arrows, item hover text), odd-tick custom AI goal `NullPointerException` guards, Mother Spider `SynchedEntityData` fix (1.20.1), and missing-advancement NPE guards.
- **Leak & Save-Bloat Fixes / 泄漏治理**：Fixes `workQueue` `<= 0` tick permanent leaks and cross-world retention; replaces permanent entity UUID `ServerScoreboard` writes (`bloc`, `blocc`, `eat`) with entity NBT and cleans legacy save bloat on server start.
- **Server TPS Optimizations / 服务端减负**：Segmented 3D/ground pathfinding for 150-block Scarlet Persecutor & Nightmare Stalker without reducing follow range; native AABB lookup replacing per-tick `/execute` command parsing in Lost Items; nearby density caps for Corpse Fly / Maggot breeding; `O(1)` animation sync dispatch; removal of per-tick `sendBlockUpdated` chunk-remesh spam on Dark Ice / Fel Soil / Puddles; `neighborChanged`-driven trap support checks.
- **Client FPS Optimizations / 客户端提帧**：Glow-layer batching eliminating shared-buffer thrashing, cached custom 3D `HumanoidModel`s, cached `ResourceLocation`s, sounds, and `RawAnimation`s.
- **Gameplay & Dupe Fixes / 玩法与刷物漏洞修复**：Fixes Boss phase health comparison, Boss teleporting into walls/lava, same-tick duplicate Boss spawns, immune-damage hurt triggers, chunk-reload gear overwrites, Nut Hammer enchanted-helmet dupe, projectile pickup dupe, single-stack `Items.AIR` 0-cooldown spam, and Frostbitten Blade / Hound Trap destroying ground loot.
