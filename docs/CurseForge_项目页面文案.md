# CurseForge 项目页面文案

本稿对应当前 `0.1.2` GitHub 预发布版本，采用 V2 折中铜壳与同步的 400×400 项目图标；GitHub 预发布及附件已完成上传和核验，CurseForge 项目尚未创建或上传。构建与资源检查不代表游戏内验收，V2 外观、干净实例和联机等待测项目保留说明。只复制所需的公开文案，不要把编辑说明和待办清单整篇贴到项目页面。

## 1. 项目字段（作者填写）

| 字段 | 建议内容 |
| --- | --- |
| Game | Minecraft |
| Class / Project type | Mods |
| Name | Archaeology Compass |
| Summary | Find nearby suspicious sand and gravel with loot still inside using a craftable copper compass. |
| Main category | 如果后台提供 `Utility & QoL`，优先选择此项 |
| Additional categories | 如果后台提供 `Adventure and RPG`，可选；不要为了曝光添加无关分类 |
| Source | https://github.com/luanma114/archaeology-compass |
| Issues | https://github.com/luanma114/archaeology-compass/issues |
| Project license | 优先使用 Custom License，粘贴第 5 节的分内容许可说明；不是将全部内容仅标为 MIT |
| Project avatar | 已准备 [400×400 PNG 图标](<archaeology_compass_icon_400.png>)，基于原创铜制罗盘，深色背景，无文字 |

项目名称是否已被占用、分类选项是否存在，仍需在 CurseForge 后台确认。名称不附加 Minecraft 版本、NeoForge 或 Beta 字样；版本信息放在文件标签和描述里。

## 2. English description（公开正文，可直接复制）

# Archaeology Compass

**Spend less time searching for suspicious blocks and more time uncovering their loot.**

Archaeology Compass adds a craftable copper compass that points toward the nearest suspicious sand or suspicious gravel with archaeology loot still inside. Keep it in your inventory while exploring, and let the needle guide you to nearby finds.

**This is a nearby block finder—not a long-range structure locator.** You still need to explore the world and get close to an archaeological site.

## Features

- **Find nearby archaeology loot.** Locate suspicious sand and suspicious gravel that still have loot to uncover.
- **Works from your inventory.** You do not need to keep the compass in your hand.
- **Updates as you explore.** The compass searches again when its next scan detects that a target has been brushed out, broken, or moved outside the search range.
- **Spins without a usable target.** The needle keeps rotating when no valid synchronized target is available. Hold Shift to distinguish waiting for a result from a scan that found nothing.
- **Shift for details.** Hold Shift over the item to see search rules, the server's scan range, and the current search status.
- **Loaded chunks only.** Searching does not force distant chunks to load or generate.
- **Configurable on the server.** Adjust the horizontal radius, vertical radius, and scan interval.
- **Original artwork.** A 32×32, 32-frame copper compass with the balanced V2 shell: a subtle two-row copper side wall, a dark bottom edge, a pale-gold needle, and a green tail.
- **English and Simplified Chinese item text.**

## How to use

1. Craft an Archaeology Compass and keep it in your player inventory.
2. Explore areas containing suspicious sand or suspicious gravel.
3. Follow the pale-gold end of the needle toward the nearest valid target.
4. Brush the target to uncover its loot. The compass selects another target on a later scan if one is available.

By default, the compass searches within a **64-block horizontal radius** and **32 blocks above and below you**. It updates every **20 game ticks**, roughly once per second at normal server speed. Vertical distance does not reduce the horizontal search radius.

A newly acquired compass may need to wait until the next scan before receiving a result. The compass does not reveal the contents of the loot or display target coordinates.

## Crafting

Use **2 brushes, 2 copper ingots, and 1 compass** in a crafting table:

| | | |
| :---: | :---: | :---: |
| Empty | Brush | Empty |
| Copper Ingot | Compass | Copper Ingot |
| Empty | Brush | Empty |

Produces **1 Archaeology Compass**. The recipe is configured to unlock in the recipe book after obtaining a brush or a compass.

JEI is **not required**. The mod uses a standard shaped crafting recipe; its display in JEI has not yet been verified in-game for this prerelease.

## Requirements and installation

- **Minecraft:** 1.21.1
- **Mod loader:** NeoForge
- **NeoForge dependency minimum:** 21.1.235. Any newer version must also be compatible with Minecraft 1.21.1; newer loader versions have not been separately verified.
- **Java:** 21

Install the mod file into your instance's `mods` folder. For single-player, install it on the client. For multiplayer, **both the server and every joining client need the mod**.

This build targets **NeoForge only**. Forge, Fabric, and other Minecraft versions are not supported by this file.

## Server configuration

| Setting | Default | Allowed range |
| --- | ---: | --- |
| `horizontalRadius` | 64 blocks | 1–128 |
| `verticalRadius` | 32 blocks | 1–64 |
| `scanIntervalTicks` | 20 ticks | 1–1200 |

The server controls these settings, including the integrated server in single-player. Larger scan ranges and shorter intervals increase scanning work; performance in large multiplayer environments has not yet been benchmarked.

## Prerelease status and limitations

**Version 0.1.2 is an early prerelease focused on artwork.** It was validated in a development client on a newly created world: crafting, the needle pointing at a nearby target, the Shift tooltip including its "target found" state, and the V2 artwork in game were all confirmed.

The following have **not** been verified for this release, and no claim is made about them: installation from the release JAR in a clean instance, dedicated-server multiplayer, JEI display, and performance under large scan ranges or high player counts. Expect possible issues and please report them.

- Only loaded chunks within the configured range are searched.
- Block changes and ordinary inventory changes are reflected on the next scan, not instantly.
- Adding a third-party block to `archaeologycompass:archaeology_targets` is not enough by itself. Its block entity must be a Minecraft `BrushableBlockEntity` or a subclass and pass the mod's loot-state checks. Arbitrary custom archaeology implementations are not supported.

## Modpacks, license, and support

You may include this mod in modpacks under its applicable license terms.

- **Code and non-artwork project content:** [MIT License](https://github.com/luanma114/archaeology-compass/blob/main/LICENSE).
- **Compass textures and the specified artwork previews:** [Creative Commons Attribution 4.0 International](https://creativecommons.org/licenses/by/4.0/), with scope and attribution details in the [artwork license notice](https://github.com/luanma114/archaeology-compass/blob/main/LICENSE_ASSETS).

These licenses apply to different parts of the project, not as a choice of license for every file. Keep the required notices, credit the artwork where applicable, and indicate artwork modifications when distributing them.

**Artwork credit:** Archaeology Compass artwork by luanma114, licensed under CC BY 4.0.

Found a problem? Please [open an issue](https://github.com/luanma114/archaeology-compass/issues) with your Minecraft, NeoForge, and mod versions, whether you are playing single-player or multiplayer, and steps to reproduce it. Include relevant logs if available, and remove private information before sharing them.

[Source code](https://github.com/luanma114/archaeology-compass)

---

## 3. 可选中文说明（放在完整英文正文之后）

### 中文介绍：考古罗盘

考古罗盘新增一枚可合成的铜制罗盘，帮助寻找附近仍有考古战利品的**可疑沙子和可疑沙砾**。

它寻找的是附近的考古方块，**不是远距离遗迹建筑**。你仍需先探索并接近考古地点。

- 放在玩家物品栏里即可生效，不必一直拿在手中。
- 浅金色长针指向最近目标，绿色短尾用于区分方向。
- 默认水平搜索半径为 **64 格**，上下各 **32 格**，每 **20 Tick** 更新一次。
- 目标刷空、被挖掉或离开范围后，在下一扫描周期重新寻找；没有目标时指针持续旋转。
- 按住 **Shift** 查看扫描规则、服务器范围和搜索状态。
- 只搜索已加载区块，不会为了定位加载远处区域。
- 合成需要 **两把刷子、两个铜锭和一个指南针**：上下放刷子，左右放铜锭，中央放指南针。

**版本要求：Minecraft 1.21.1、NeoForge 21.1.235 或更新的 1.21.1 兼容版本、Java 21。** 更新的 NeoForge 版本未逐一验证。多人游戏需要客户端和服务器都安装；此文件不支持 Forge、Fabric 或其他 Minecraft 版本。

当前 **0.1.2 为美术更新预发布**，采用两行铜制侧壁、一行暗色收边的 V2 折中外观，并同步项目图标；定位、配方和指针逻辑未改。已在新建存档的开发客户端中确认：合成、指针指向附近目标、Shift 提示（含「已发现考古目标」状态）以及 V2 游戏内外观。

以下项目**本次未做验证，页面不作任何相关声明**：发布 JAR 在干净实例中的安装、独立服务器多人联机、JEI 展示，以及大范围扫描与多人场景下的性能。JEI 不是必需依赖。

代码及非美术内容采用 MIT，指定美术资源采用 CC BY 4.0；两者按内容分别适用。整合包可以在遵守相应许可条款的前提下收录。问题反馈请提交 [Issue](https://github.com/luanma114/archaeology-compass/issues)。

## 4. 文件上传字段与更新日志（作者填写）

### 文件字段

| 字段 | 当前建议 |
| --- | --- |
| Upload file | [archaeologycompass-0.1.2.jar](<../build/libs/archaeologycompass-0.1.2.jar>) 已通过离线构建及版本、V2贴图、模型和许可检查；上传 CurseForge 前仍建议完成干净实例验收，不把构建检查当作游戏测试 |
| Display Name | Archaeology Compass 0.1.2 — Minecraft 1.21.1 (NeoForge) |
| Release Type | 按目前验证状态选择 **Alpha**；完成单人回归和独服双客户端验证后再考虑 Beta，不直接标稳定 Release |
| Game Version | 1.21.1 |
| Mod Loader | NeoForge；不勾选 Forge 或 Fabric |
| Required relations | 目前没有额外第三方模组必需依赖；Minecraft、NeoForge 要求通过版本标签和说明体现 |
| Optional relations | JEI 非必需，当前不宣传“已实测兼容”；无需为了上传添加关系 |

如果上传前完成测试，应据实更新第 2、3 节的预发布状态，不沿用已经过时的“仍待验证”，也不在未测试时删掉限制。文件版本变化时同步改标题、文件字段和更新日志。

### English changelog for 0.1.2（可直接粘贴到 Changelog）

# Archaeology Compass 0.1.2

Early prerelease for **Minecraft 1.21.1 / NeoForge**.

## Changes since 0.1.1

- Adopted the balanced V2 copper-shell textures across all 32 frames: a two-row side wall, a dark bottom edge, and a softer front seam.
- Updated the 400×400 project icon to match the V2 artwork.
- Synchronized artwork previews, the asset generator, project documentation, and CurseForge page copy.
- Kept the existing target scanning, synchronization, recipe, and needle-angle behavior unchanged.

## Validation notice

This is not a stable-release claim. Crafting, needle behaviour, the Shift tooltip, and the V2 artwork were validated in a development client on a newly created world. Installation from the release JAR in a clean instance, dedicated-server multiplayer, JEI display, and performance under large scan ranges or high player counts have not been verified, and no claims are made about them.

## 5. Custom License 字段文案（可复制）

Archaeology Compass uses separate licenses for different types of content:

1. Code and non-artwork project content, including Java sources, the generator script, item model JSON, configuration, and project documentation, are licensed under the MIT License. Copyright (c) 2026 luanma114. Full terms: https://github.com/luanma114/archaeology-compass/blob/main/LICENSE
2. The compass PNG textures and the artwork previews specified in the artwork notice are licensed under Creative Commons Attribution 4.0 International (CC BY 4.0). Creator: luanma114. License: https://creativecommons.org/licenses/by/4.0/ . Full legal terms: https://creativecommons.org/licenses/by/4.0/legalcode.en . Scope and attribution notice: https://github.com/luanma114/archaeology-compass/blob/main/LICENSE_ASSETS
3. NeoForged MDK template material retains its original MIT copyright and license notice. Third-party materials, trademarks, and Minecraft assets are not covered by the project's licenses.

The licenses apply by content, not as alternative licenses for all files. Retain applicable notices and comply with attribution and modification-notice requirements when redistributing artwork. This summary does not replace the full license terms.

## 6. 图片标题与说明（作者准备图片后使用）

已准备 [400×400 项目图标](<archaeology_compass_icon_400.png>)，上传至项目头像字段。真实游戏截图已按 [`screenshots/README.md`](screenshots/README.md) 的规范拍摄完成，位于 `docs/screenshots/`，本稿不使用假截图、不预填不存在的图片 URL。

### 6.1 图片插入位置对照

CurseForge 项目页图片**无法从本地直接引用**，需先在网页端上传（或经项目 Gallery），再在描述编辑器中插入。上传后按此表定位插入点：

| 顺序 | 文件 | 状态 | 插入位置 | 说明 |
| --- | --- | --- | --- | --- |
| 1 | `01-needle-pointing.png` | ✅ 已就位 | 正文 `## Features` 标题**之前** | 首图必须是"指针指向可疑沙"这一核心卖点；画面中快捷栏第 2 格为原版指南针，可作对照 |
| 2 | `02-crafting.png` | ✅ 已就位 | 正文 `## Crafting` 表格**之后** | 紧邻配方说明 |
| 3 | `03-shift-tooltip.png` | ✅ 已就位 | 正文 `## How to use` 段落**之后** | 配合"按住 Shift 查看详情"的说明；画面含"已发现考古目标"状态 |
| 4 | `archaeology_compass_rotation.gif` | ✅ 已就位 | 正文 `## Features` 的「Original artwork」条目**之后** | 美术预览，必须保留"非游戏截图"标注 |
| 5 | `04-compass-vs-vanilla.png` | ⬜ 未拍摄 | `## Requirements and installation` **之前** | 可选；当前仅在首图的快捷栏中并排出现，若需特写需单独拍摄 |

### 6.2 图注文案

| 素材 | 英文标题 | 英文说明 |
| --- | --- | --- |
| 指针指向可疑沙 | A compass for nearby archaeology | Keep the Archaeology Compass in your inventory while exploring nearby suspicious blocks. |
| 工作台合成 | Crafting the Archaeology Compass | Two brushes, two copper ingots, and one compass produce an Archaeology Compass. |
| Shift 提示 | Search details at a glance | Hold Shift over the item to view scan rules, the server's range, and the current search status. |
| 与原版指南针并排（可选） | Copper craftsmanship, familiar feel | The Archaeology Compass sits alongside the vanilla compass—same reading habit, different target. |
| 美术旋转预览 | Copper compass artwork preview | Artwork animation preview, not an in-game capture. Preview speed does not represent the in-game needle speed. |

### 6.3 上传顺序建议

1. 先传 `01-needle-pointing.png` 作为首图，它决定项目页第一印象。
2. 再传 `02`、`03`，最后补美术预览。
3. 每张图的标题与说明按 6.2 填写，英文在前。
4. 美术预览图必须标注"非游戏截图"，不得与真实截图混排在同一组。

可选美术素材已有 [旋转预览](<archaeology_compass_rotation.gif>) 和 [32 帧总览](<archaeology_compass_preview.png>)，不要将它们称为游戏截图。[项目图标](<archaeology_compass_icon_400.png>) 使用 V2 折中铜壳罗盘第 19 帧整数倍放大，配深色背景、测量环和铜色角标；可通过 [图标生成脚本](<../generate_project_icon.py>) 重现（Python 3 + Pillow）。图标沿用 [美术许可](<../LICENSE_ASSETS>) 的 CC BY 4.0 授权。

## 7. 发布前核对（内部清单，不粘贴到公开正文）

- [x] 准备上传的 JAR 已重新构建（`build/libs/archaeologycompass-0.1.2.jar`）。
- [ ] 该 JAR 在干净实例中安装验证 —— **未做**；公开正文已相应声明不作验证声明。
- [x] 核对实际版本、文件标签、公开正文及 Changelog 一致。
- [x] 测试合成、配方书、Shift 提示、指针方向（开发客户端 + 新建存档，证据见 `docs/验收清单.md`）。
- [ ] 测试无目标旋转、刷空/挖掉目标、离开范围 —— **未留存证据**。
- [ ] 测试重连、换维度和重生；独立服务器与双客户端验证 —— **未做**。
- [x] 没有把“编译成功”写成“所有测试通过”，没有把 JEI 或新版本加载器写成已实测。
- [ ] 项目名称未被占用（需在后台确认）；头像 400×400 ✅；真实游戏截图 ✅（`docs/screenshots/`）。
- [ ] 英文描述和简介先于其他语言；标题不混入游戏版本信息。
- [ ] 公开页面无 GitHub Releases 或其他外部文件下载入口；源码、许可证和问题反馈链接按用途保留。
- [ ] 许可选择与正文一致；没有把全部美术误标为 MIT。
- [ ] 不因本次文案或纯注释修改额外发布无功能变化的文件来刷更新；首次上传使用对应且已验收的发行包。

文案依据：[项目说明](<../README.md>)、[开发文档的历史验证与待验项](<NeoForge_MC模组开发文档.md#历史验证记录与当前待验项>)、[代码许可](<../LICENSE>)、[美术许可](<../LICENSE_ASSETS>)。

平台规则参考：[审核政策](https://support.curseforge.com/support/solutions/articles/9000197279-moderation-policies)、[创建与提交项目](https://support.curseforge.com/support/solutions/articles/9000197241-creating-and-submitting-a-project)、[文件发布类型与标签](https://support.curseforge.com/support/solutions/articles/9000197242-file-project-types-and-additional-fields)。提交时以后台最新规则和审核反馈为准，本稿不保证审核通过。
