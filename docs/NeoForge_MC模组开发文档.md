# NeoForge Minecraft 模组开发文档

## 最新状态：0.1.2 预发布准备（2026-10-08 更新）

当前版本为 **`0.1.2`**，用户已批准发布 GitHub `v0.1.2` 预发布；本轮离线构建与资源校验已完成，GitHub 预发布正在准备；游戏内专项验收仍未完成。版本配置见 [gradle.properties](<../gradle.properties>)，项目说明见 [README](<../README.md>)。本节描述当前仓库的实现、V2 资源与发布边界；历史 `0.1.1` 验证和当前 `0.1.2` 待验项分别记录，不代表全部游戏功能已验收。下方 **2026-09-02 章节是历史快照**，其中原版占位外观等描述不代表当前版本。

### 开发、构建与 CI

环境：Minecraft `1.21.1`、NeoForge `21.1.235`、Java `21`，版本与工具链分别见 [gradle.properties](<../gradle.properties>) 和 [build.gradle](<../build.gradle>)。

```bat
gradlew.bat runClient
gradlew.bat runServer
gradlew.bat build
```

构建产物位于 `build/libs/`。[build.gradle](<../build.gradle>) 使用 `archivesName = mod_id` 与 `version = mod_version`，当前主 JAR 为 [archaeologycompass-0.1.2.jar](<../build/libs/archaeologycompass-0.1.2.jar>)（已完成本轮构建和资源检查），文件名不含 Minecraft 版本；发布时带上 Minecraft 版本是命名建议，不是现有构建规则。后续 Minecraft 版本须独立构建、测试和发布，不使用同一个 JAR 跨版本运行。

[CI 构建配置](<../.github/workflows/build.yml>) 在 push 和 pull request 时执行 `./gradlew build`，没有自动创建 Release 或上传发布附件的步骤；不能将触发 CI 等同于完成发布。

### 使用反馈与配置同步

- [ArchaeologyCompassClientEvents.java](<../src/main/java/com/luanma114/archaeologycompass/client/ArchaeologyCompassClientEvents.java>) 通过 `ItemTooltipEvent` 添加中英文用途说明，按住 Shift 展示扫描规则、范围与状态。
- 范围读取 NeoForge 同步的 SERVER 配置，仅在 `Config.SPEC.isLoaded()` 且存在玩家上下文时显示，不使用客户端自定范围。
- [ArchaeologyCompassClientState.java](<../src/main/java/com/luanma114/archaeologycompass/ArchaeologyCompassClientState.java>) 中的 `receivedTarget` 区分尚未接收结果与已确认无目标。
- [ArchaeologyCompassEvents.java](<../src/main/java/com/luanma114/archaeologycompass/ArchaeologyCompassEvents.java>) 中的 `INITIALIZED_PLAYERS` 记录扫描初始化状态，首次没有目标时补发空目标包；失去罗盘、换维度、重生和退出时处理初始化状态。普通物品栏或目标变化在下一次周期扫描时处理，默认每 `20` Tick 一次；登录、换维度、重生另有即时刷新入口。
- 玩家物品栏中没有罗盘、例如仅在物品列表中查看时，提示放入物品栏开始扫描，不将其他上下文误报为当前扫描结果。

### 配方书与 JEI

[有序配方](<../src/main/resources/data/archaeologycompass/recipe/archaeology_compass.json>) 使用 `minecraft:crafting_shaped`。现有材料与摆放不变：上下刷子、左右铜锭、中央指南针，产出一个考古罗盘。

[配方解锁 advancement](<../src/main/resources/data/archaeologycompass/advancement/recipes/tools/archaeology_compass.json>) 中，获得刷子、获得指南针和已解锁配方三个条件采用 OR 关系，奖励解锁对应配方。

JEI 使用标准工作台分类自动识别原版有序配方，不需要专用插件；[build.gradle](<../build.gradle>) 未增加 JEI 编译或强制运行依赖。既有验证记录中的测试客户端尚未安装 JEI，其配方展示及交互仍待游戏内验证。

### 美术资源与生成

当前美术为原创 32×32 铜制罗盘，包含 32 个角度帧，已采用 **V2 折中铜壳**：表盘相对旧薄壳上移 1 像素，前侧为 2 行铜壳及 1 行暗色收边，减弱黑色分界和横向色带，保留左亮右暗的金属层次。[资源生成脚本](<../generate_compass_assets.py>) 已同步该设计，指针纵向透视比例仍为 `0.64`；方向帧顺序、模型谓词和 Java 指针逻辑不变。这里增强的是贴图的厚度感，不是增加模型几何厚度。

- 贴图：[archaeology_compass_00.png](<../src/main/resources/assets/archaeologycompass/textures/item/archaeology_compass_00.png>) 至 [archaeology_compass_31.png](<../src/main/resources/assets/archaeologycompass/textures/item/archaeology_compass_31.png>)，均位于 `src/main/resources/assets/archaeologycompass/textures/item/`。
- 帧模型：[archaeology_compass_00.json](<../src/main/resources/assets/archaeologycompass/models/item/archaeology_compass_00.json>) 至 [archaeology_compass_31.json](<../src/main/resources/assets/archaeologycompass/models/item/archaeology_compass_31.json>)，均位于 `src/main/resources/assets/archaeologycompass/models/item/`。
- [主模型](<../src/main/resources/assets/archaeologycompass/models/item/archaeology_compass.json>) 通过 `minecraft:angle` overrides 选择原创帧模型，保持原有方向映射，并非直接引用原版指南针贴图。
- [全帧预览](<archaeology_compass_preview.png>)。
- [旋转动画](<archaeology_compass_rotation.gif>)。
- [400×400 项目图标](<archaeology_compass_icon_400.png>) 已同步 V2 铜壳外观，与当前正式贴图保持一致。

![32 帧贴图全览](<archaeology_compass_preview.png>)

修改根目录 [generate_compass_assets.py](<../generate_compass_assets.py>) 可调整外观并重新生成贴图、帧模型及预览，需要 Python 3 与 Pillow：

```bat
py -3 -m pip install Pillow
py -3 generate_compass_assets.py
```

预览是资源动画，不是游戏截图，播放速度不代表游戏内指针转速。

### 历史验证记录与当前待验项

以下已完成事项沿用此前文档记录，不表示本次重新执行，也不自动证明 `0.1.1` 或当前 `0.1.2` 的全部交互已验收：

- 此前离线 Gradle build 已通过，当时的 JAR 已确认包含贴图、配方解锁资源及许可文件。
- 此前已校验 32 帧均为不同的 32×32 RGBA 贴图、透明轮廓一致，模型引用和中英文提示键符合预期。
- 此前开发客户端成功启动并进入单人世界，无崩溃。启动和资源加载正常不等于交互验收通过。
- 2026-09-02 历史快照记录了单人基础人工验收，不覆盖后来新增的提示、配方书解锁与新版外观。
- 此前 `0.1.1` 开发客户端已成功启动并进入单人世界，用户反馈该轮测试功能正常，但认为旧贴图偏薄；这属于用户人工测试反馈，不是独服、双客户端或性能测试记录。
- 此前已按用户选择正式采用 V2 折中铜壳，生成器与候选贴图逐像素一致；32 帧均为不同的 32×32 RGBA 贴图且透明轮廓一致，方向映射与 Java 实现未变。当前 [400×400 项目图标](<archaeology_compass_icon_400.png>) 也已同步 V2 外观；V2 尚未重新进行游戏内外观验收。
- **历史 `0.1.1` 构建记录**：采用 V2 后，`gradlew.bat build --offline` 成功；当时已确认 JAR 内 32 帧贴图与批准的 V2 候选完全一致，模型方向映射未变，未混入候选预览或原版参考素材。测试任务显示 `NO-SOURCE`，此结果是历史构建与资源校验，不是自动化功能测试通过，也不是本轮 `0.1.2` 构建结果。
- **本轮 `0.1.2` 构建与资源检查**：`gradlew.bat build --offline` 成功；[发行 JAR](<../build/libs/archaeologycompass-0.1.2.jar>) 的模组版本为 `0.1.2`，32 帧贴图与批准的 V2 完全一致，模型方向映射未变，配方、解锁、标签和语言资源均存在，三份许可文件与源码一致。测试任务为 `NO-SOURCE`，不等于自动化功能测试通过。发布内容见 [v0.1.2 说明](<releases/v0.1.2.md>)。
- V2 游戏内外观、`0.1.2` JAR 干净实例安装、独立服务端与双客户端、JEI 展示及性能压力仍需专项验证；此前“功能正常”反馈未逐项列出用例，不据此补记这些项目通过。
- 当前没有自动化功能测试函数；构建成功或保留开发测试运行配置，均不能描述为自动化功能测试通过。

### 许可与发布边界

本项目按内容分别授权，不是全部文件同时适用两种许可证：

- Java、生成脚本、模型 JSON、配置和项目文档等非美术内容采用 MIT，见根目录 [LICENSE](<../LICENSE>)。
- 罗盘 PNG 贴图、全帧 PNG 预览和 GIF 旋转动画采用 CC BY 4.0，具体范围和完整条款链接见根目录 [LICENSE_ASSETS](<../LICENSE_ASSETS>)。
- NeoForged MDK 模板保留原有 MIT 版权与许可声明，见 [TEMPLATE_LICENSE.txt](<../TEMPLATE_LICENSE.txt>)。第三方材料和 Minecraft 资源不属于项目授权范围。
- 生成脚本采用 MIT，不改变仓库已生成美术资源的 CC BY 4.0 授权。

美术署名示例：

> Archaeology Compass artwork by luanma114, licensed under CC BY 4.0. Source: https://github.com/luanma114/archaeology-compass · License: https://creativecommons.org/licenses/by/4.0/ 。分发修改版时须补充修改说明。

整合包可在遵守许可条件的前提下收录和分发模组。[build.gradle](<../build.gradle>) 的 JAR 任务打包 [LICENSE](<../LICENSE>)、[LICENSE_ASSETS](<../LICENSE_ASSETS>) 和 [TEMPLATE_LICENSE.txt](<../TEMPLATE_LICENSE.txt>)；[模组元数据](<../src/main/resources/META-INF/neoforge.mods.toml>) 从 [gradle.properties](<../gradle.properties>) 展开 `MIT AND CC-BY-4.0`。

既有 `v0.1.0` 和 `v0.1.1` 的标签、Release 与附件保持不变，其中 `v0.1.0` 旧附件仍含 `All Rights Reserved` 元数据及较早功能外观。当前 `0.1.2` 沿用分内容许可声明，采用 V2 铜壳及同步的 [400×400 V2 图标](<archaeology_compass_icon_400.png>)；发行包已完成离线构建和资源校验，GitHub 预发布正在准备。不能把旧附件描述为本次构建，也不覆盖旧附件。

## 考古罗盘：历史实现快照（2026-09-02，非当前状态）

本章保留当时的实现、目录结构与人工验收记录，**不是当前 `0.1.2` 的状态说明，也不是本次重新运行的验证结论**。原版指南针占位外观、当时的文件清单与后续工作仅适用于该历史快照；当前资源、配置和扫描行为以上方“最新状态”及下方“当前功能规则”为准。

### 当时已实现

| 模块 | 实现 |
| --- | --- |
| 目标版本 | Minecraft `1.21.1`、NeoForge `21.1.235`、Java `21` |
| Mod ID / 包名 | `archaeologycompass` / `com.luanma114.archaeologycompass` |
| 物品与本地化 | 注册 `archaeology_compass`；加入“工具与实用物品”创造模式标签页；含中英文名称 |
| 生存获取 | 有序配方：中心指南针、上下刷子、左右铜锭，产出 1 个考古罗盘 |
| 目标数据 | `archaeology_targets` 方块标签默认包含可疑沙子和可疑沙砾 |
| 有效性 | 目标须为 `BrushableBlockEntity`；保存数据含 `LootTable` 或 `item`。首次刷扫后出现 `item` 仍保持定位，完全刷空后停止定位 |
| 扫描 | 服务端仅遍历已加载区块的方块实体，不强制加载区块；选择水平范围、垂直范围内最近目标 |
| 配置 | 服务端可配置 `horizontalRadius`、`verticalRadius`、`scanIntervalTicks` |
| 状态与同步 | 按玩家 UUID 缓存目标；目标变化、无目标、物品栏中失去罗盘、登录、换维度时同步；退出时清理缓存 |
| 动态指针 | 服务端把目标通过 S2C 包下发给客户端，客户端 `ArchaeologyCompassPropertyFunction` 从 `ArchaeologyCompassClientState` 读取目标，按原版指向逻辑驱动 32 帧 `minecraft:item/compass` 模型；无目标时指针匀速顺时针旋转。模型含完整 32 帧 `minecraft:angle` override 列表 |
| 网络隔离 | 网络层仅依赖无渲染 API 的 `ArchaeologyCompassClientState`；渲染/指针属性等客户端代码位于 `client/ArchaeologyCompassClient.java`，由 `ExampleMod` 在 `Dist.CLIENT` 分支调用，独立服务端不加载该客户端类 |
| 验证 | `gradlew.bat build` 成功；开发客户端启动时曾因空 `@EventBusSubscriber` 崩溃，已移除该标注并修复 |

### 当时代码结构（历史清单）

```text
src/main/java/com/luanma114/archaeologycompass/
├─ ExampleMod.java                       模组入口、物品注册、配置与网络注册
├─ Config.java                           服务端扫描配置
├─ ArchaeologyCompassEvents.java         扫描、有效性判定、玩家生命周期处理
├─ ArchaeologyCompassTargetState.java    服务端每玩家目标缓存
├─ ArchaeologyCompassTargetPayload.java  S2C 目标状态包与编解码
├─ ArchaeologyCompassNetwork.java        网络包注册与发送
├─ ArchaeologyCompassClientState.java    无客户端渲染依赖的同步目标状态
└─ client/
   ├─ ArchaeologyCompassClient.java            客户端专属：注册 `minecraft:angle` 指针属性（`Dist.CLIENT`）
   └─ ArchaeologyCompassPropertyFunction.java  指针角度计算：读取 S2C 状态，指向目标或匀速顺时针旋转

src/main/resources/
├─ assets/archaeologycompass/
│  ├─ lang/en_us.json
│  ├─ lang/zh_cn.json
│  └─ models/item/archaeology_compass.json  32 帧 `minecraft:angle` override 指针模型
└─ data/archaeologycompass/
   ├─ recipe/archaeology_compass.json
   └─ tags/block/archaeology_targets.json
```

### 当时已知限制与后续工作（历史记录）

1. 已完成单人基础人工验收（配方、目标指向、无目标旋转、刷扫、刷空、物品栏内/手中持有、登录、换维度）。
2. 尚未完成独立服务端与双客户端联机测试。
3. 扫描已改为方块实体遍历，但大量已加载区块或大量玩家时仍应进行 TPS 压力测试；必要时增加分帧预算或区块索引。
4. 当时使用原版指南针外观作为开发占位，曾计划正式发布前替换为自制模型和纹理；替换需保留 `minecraft:angle` 指针属性注册或实现等价客户端模型属性。这是历史计划，当前原创外观以上方最新状态为准。
5. 客户端渲染/指针属性代码已隔离在 `client/`（`Dist.CLIENT`）：`ArchaeologyCompassClient` 注册 `minecraft:angle` 属性，`ArchaeologyCompassPropertyFunction` 计算指针角度。未来任何 `Minecraft`、`ItemProperties`、模型或渲染器引用必须放进该 `Dist.CLIENT` 专属类，通用网络类不得直接引用。

## 考古罗盘：当前功能规则与未实现优化

本章描述当前 `0.1.2` 代码行为；明确标为“未实现”的优化不是现有功能、配置或验收结论。后面的通用教程用于开发参考，不代表本项目已实现所有示例模块。

### 功能目标

新增物品“考古罗盘”。玩家物品栏中拥有它时表现为指南针：指针指向扫描范围内最近的可考古方块，并随服务端扫描结果更新。范围内无目标时，指针持续顺时针旋转。

### 目标方块与兼容规则

- 默认候选为原版 `minecraft:suspicious_sand`、`minecraft:suspicious_gravel`，由 [目标方块标签](<../src/main/resources/data/archaeologycompass/tags/block/archaeology_targets.json>) 定义。
- 正式 Mod ID 已是 `archaeologycompass`，见 [gradle.properties](<../gradle.properties>) 和 [ExampleMod.java](<../src/main/java/com/luanma114/archaeologycompass/ExampleMod.java>)；标签 ID 为 `archaeologycompass:archaeology_targets`，不是 `archaeology_compass:archaeology_targets`，无需替换占位 ID。
- [ArchaeologyCompassEvents.java](<../src/main/java/com/luanma114/archaeologycompass/ArchaeologyCompassEvents.java>) 要求候选同时属于该标签、方块实体是 `BrushableBlockEntity`（包括其子类），且保存的 NBT 中存在 `LootTable` 或 `item` 键。首次刷扫后由 `LootTable` 转为 `item` 时仍有效；完全刷出后两者均不存在，不再定位。
- 其他模组或整合包可通过数据包向 `archaeologycompass:archaeology_targets` 添加候选方块，但**仅加入标签不足以兼容**，还须满足上述方块实体类型与 NBT 规则。任意自定义考古方块实体的适配未实现。
- 当前服务端配置只有水平半径、垂直半径、扫描间隔三项，没有最大扫描量或单 Tick 工作预算配置。

[目标标签文件](<../src/main/resources/data/archaeologycompass/tags/block/archaeology_targets.json>) 的内容与位置：

```json
{
  "replace": false,
  "values": [
    "minecraft:suspicious_sand",
    "minecraft:suspicious_gravel"
  ]
}
```

```text
src/main/resources/data/archaeologycompass/tags/block/archaeology_targets.json
```

### 定位与更新时序

以下行为由 [ArchaeologyCompassEvents.java](<../src/main/java/com/luanma114/archaeologycompass/ArchaeologyCompassEvents.java>) 实现：

1. 玩家物品栏（含主手、副手）中存在考古罗盘时，服务端在 `PlayerTickEvent.Post` 中按 `player.tickCount % scanIntervalTicks == 0` 触发周期扫描；默认每 `20` Tick 一次，正常 `20 TPS` 下约一秒。
2. 一次调用在同一个 Tick 内遍历玩家当前维度、附近已加载区块的方块实体。水平距离按目标方块中心相对玩家实际位置计算；垂直范围按目标方块 Y 与玩家方块坐标 Y 的差判断。
3. 从本次范围内全部有效候选中，按方块中心到玩家位置的三维欧氏距离选择最近者，记录维度与方块坐标。每次重新计算最近目标，没有独立的“优先保留旧锁定”流程。
4. 目标被刷空、挖掉、移出范围，或玩家在普通游戏过程中失去罗盘时，**在下一次周期更新时**重新扫描或清除缓存；不是每 Tick 即时清除。在默认配置下通常最多等待约 `20` Tick（服务器卡顿时实际时间可更长）。同次扫描找到其他目标时可直接替换，不额外等待下一轮。
5. 登录、换维度和重生有专门事件入口，立即按物品栏状态刷新当前维度目标；退出清理服务端内存缓存。物品栏中新获得罗盘通常等待下一次周期更新，没有单独的拾取即扫事件。
6. 本次无有效目标时清除缓存，客户端显示持续旋转指针。目标坐标或维度变化、旧目标被清除时发送 S2C 状态；首次扫描确认无目标时也补发空目标包。

### 当前性能规则

当前实现不是遍历范围内每一个方块，也不是跨 Tick 分帧扫描：

- 默认每 `20` Tick 扫描一次，默认水平半径 `64` 格、垂直半径 `32` 格；
- 通过 `getChunkNow` 只读取已加载区块，不为扫描强制加载区块；
- 按区块读取 `chunk.getBlockEntities().values()`，在**同一次调用、同一个 Tick 内**完成方块实体遍历与最近目标选择；
- 按玩家 UUID 缓存上次目标，用于比较结果并减少重复同步，不是目标索引；
- [Config.java](<../src/main/java/com/luanma114/archaeologycompass/Config.java>) 只配置两个半径和扫描间隔。大量已加载区块或大量玩家时仍需 TPS 压力测试，当前没有实测的性能保证。

### 未来性能优化方案（明确未实现）

若压力测试证明周期方块实体遍历仍造成卡顿，可考虑以下方案；**当前没有对应实现或配置**：

1. **分帧工作预算**：将已加载区块中的候选遍历分配到多个 Tick，以拟议的 `maxBlocksPerScan`（例如 `8192`）作为单 Tick 工作预算，而不是整次扫描的总上限。须处理扫描期间玩家移动、区块卸载和候选变化，并在完整搜索完成后更新最近目标。该值尚未进入配置规范，不应写入现有配置作为有效选项。
2. **已知目标索引**：在区块加载时记录候选坐标，在方块或方块实体状态变化时维护索引，卸载时移除；罗盘仅查询当前已加载区块索引，并继续执行有效性与范围检查。该索引尚不存在。

这些方案保留为后续设计，不改变当前“一次 Tick 内完整遍历、周期刷新目标”的语义；是否实施须先专项测量，再调整实现及验证。

### 客户端表现与同步

- 服务端为权威端：搜索、失效判定与目标缓存均在服务端执行。
- 服务端通过显式 S2C 包向对应玩家同步目标坐标或“无目标”状态，不依赖物品数据组件同步。失去罗盘的玩家仍可能收到清除旧目标的空状态包。
- 客户端从 S2C 状态读取目标，根据玩家朝向和目标坐标计算罗盘指针角度；无目标时持续顺时针旋转，不向服务端发送旋转状态。
- [主模型](<../src/main/resources/assets/archaeologycompass/models/item/archaeology_compass.json>) 使用原创 32 帧资源及 `minecraft:angle` overrides，客户端属性注册以 Minecraft `1.21.1` 对应 NeoForge API 为准。模型 JSON、物品模型定义和客户端 API 在后续版本可能变化，版本专属代码不得直接复制到其他版本分支。
- 独立服务端不得加载渲染、模型属性或其他客户端专属类；隔离代码不等于已经完成独立服务端实机验收。

### 当前配置项（仅三项）

定义及允许范围见 [Config.java](<../src/main/java/com/luanma114/archaeologycompass/Config.java>)，由 [ExampleMod.java](<../src/main/java/com/luanma114/archaeologycompass/ExampleMod.java>) 注册为 SERVER 配置：

| 键 | 默认值 | 允许范围 | 含义 |
| --- | ---: | --- | --- |
| `horizontalRadius` | `64` | `1`–`128` | 水平搜索半径，单位：格 |
| `verticalRadius` | `32` | `1`–`64` | 上下搜索半径，单位：格 |
| `scanIntervalTicks` | `20` | `1`–`1200` | 周期搜索间隔，单位：Tick |

配置上下限用于限制不合理数值，不保证所有允许组合都没有性能问题。当前只要玩家物品栏内存在罗盘即扫描，无需且不存在 `requireHoldingCompass` 开关；`maxBlocksPerScan` 也不是现有配置项。

### 当前资源与注册清单

[ExampleMod.java](<../src/main/java/com/luanma114/archaeologycompass/ExampleMod.java>) 注册物品并加入“工具与实用物品”创造模式标签页；[有序配方](<../src/main/resources/data/archaeologycompass/recipe/archaeology_compass.json>)、[配方解锁 advancement](<../src/main/resources/data/archaeologycompass/advancement/recipes/tools/archaeology_compass.json>)、[中文翻译](<../src/main/resources/assets/archaeologycompass/lang/zh_cn.json>) 与 [英文翻译](<../src/main/resources/assets/archaeologycompass/lang/en_us.json>) 已有对应资源。

```text
Mod ID：archaeologycompass
完整物品 ID：archaeologycompass:archaeology_compass
注册路径：archaeology_compass
翻译键：item.archaeologycompass.archaeology_compass
主模型：src/main/resources/assets/archaeologycompass/models/item/archaeology_compass.json
帧模型：src/main/resources/assets/archaeologycompass/models/item/archaeology_compass_00.json 至 archaeology_compass_31.json
原创纹理：src/main/resources/assets/archaeologycompass/textures/item/archaeology_compass_00.png 至 archaeology_compass_31.png
方块标签：src/main/resources/data/archaeologycompass/tags/block/archaeology_targets.json
```

### 后续专项验收清单（不是已通过记录）

以下保留为待执行检查；既有单人验收记录见历史快照，不据此勾选当前预发布的专项测试：

- [ ] 默认仅定位符合 `BrushableBlockEntity` 与 `LootTable`/`item` 规则的可疑沙子与可疑沙砾。
- [ ] 每次单 Tick 完整扫描后，在多个有效候选中选择最近目标。
- [ ] 刷空、挖掘、离开范围或失去罗盘在下一周期更新；登录、换维度、重生按事件即时刷新。
- [ ] 范围内无目标时持续旋转，首次空结果与等待结果提示可正确区分。
- [ ] 仅扫描已加载区块，不触发区块加载。
- [ ] 第三方候选加入标签并满足方块实体类型和 NBT 规则时可定位；只加入标签的非兼容实体不会误报。
- [ ] 使用反馈、配方书实际解锁、V2 游戏内外观与 JEI 配方展示分别验证。
- [ ] 在无开发环境的干净实例安装 `0.1.2` 主 JAR 并验证，不以开发客户端测试替代。
- [ ] 双客户端连接独立服务端时，各自获得正确目标。
- [ ] 检查配置边界，并对高扫描频率、大范围与多人场景进行 TPS 压力测试，不预先宣称无明显下降。

## 1. 目标与范围

本文首发面向 NeoForge 与 Minecraft `1.21.1`。架构预留后续 Minecraft 版本支持；每个目标版本独立构建、测试与发布，不承诺同一 JAR 跨 Minecraft 大版本运行。覆盖环境搭建、工程结构、内容注册、资源制作、事件、数据保存、网络同步、调试、构建与发布。

开发前先固定以下版本，整个项目生命周期内不要随意混用：

| 项目 | 建议 |
| --- | --- |
| JDK | Java 21（Minecraft `1.21.1`）；新增目标版本时按该版本要求调整 |
| 构建工具 | Gradle Wrapper |
| IDE | IntelliJ IDEA Community/Ultimate |
| Loader | NeoForge |
| 映射/依赖版本 | 由 NeoForge MDK 的 `gradle.properties` 管理 |

## 2. 环境搭建

### 2.1 安装基础工具

1. 安装目标版本要求的 JDK。
2. 配置 `JAVA_HOME`，终端执行 `java -version` 确认版本正确。
3. 安装 Git 与 IntelliJ IDEA。
4. 从 NeoForge 官方 MDK 下载页获取目标 Minecraft 版本的 MDK。

不要手动安装全局 Gradle。项目使用 `gradlew` / `gradlew.bat`，避免团队成员 Gradle 版本不一致。

### 2.2 初始化项目

解压 MDK，重命名目录为模组工程名，例如 `my_mod`。用 IntelliJ IDEA 打开根目录的 `build.gradle`。

Windows 常用命令：

```bat
gradlew.bat --version
gradlew.bat build
gradlew.bat runClient
gradlew.bat runServer
```

首次执行会下载 Gradle、NeoForge、Minecraft 开发依赖，耗时取决于网络与缓存情况。

### 2.3 验证结果

执行：

```bat
gradlew.bat runClient
```

开发客户端启动后，模组列表中应出现示例模组。若启动失败，先检查：

- JDK 主版本是否匹配；
- `gradle.properties` 中 Minecraft 与 NeoForge 版本是否互相兼容；
- 是否由 IDE 使用错误 JDK；
- 国内网络是否无法获取 Maven 依赖。

## 3. 项目命名规范

以模组 ID 为核心标识。假设模组 ID 为 `my_mod`：

| 项目 | 示例 | 规则 |
| --- | --- | --- |
| Mod ID | `my_mod` | 小写字母、数字、下划线；稳定后不要改 |
| Java 包 | `com.example.mymod` | 全小写；通常不含下划线 |
| 主类 | `MyMod` | PascalCase |
| 资源命名空间 | `my_mod` | 必须等于 Mod ID |
| 注册 ID | `ruby_ore` | 小写蛇形命名 |
| 翻译键 | `item.my_mod.ruby` | `类型.modid.名称` |

`mod_id` 变更会影响存档中的物品、方块、实体、标签、配方、网络协议等标识。正式发布后视为兼容性变更。

## 4. 推荐工程结构

```text
src/main/
├─ java/com/example/mymod/
│  ├─ MyMod.java
│  ├─ registry/
│  │  ├─ ModItems.java
│  │  ├─ ModBlocks.java
│  │  ├─ ModBlockEntities.java
│  │  ├─ ModEntities.java
│  │  ├─ ModMenus.java
│  │  └─ ModCreativeTabs.java
│  ├─ event/
│  │  ├─ CommonEvents.java
│  │  └─ ClientEvents.java
│  ├─ network/
│  ├─ data/
│  ├─ config/
│  └─ client/
│     ├─ screen/
│     └─ render/
└─ resources/
   ├─ META-INF/neoforge.mods.toml
   ├─ assets/my_mod/
   │  ├─ lang/
   │  ├─ textures/
   │  ├─ models/
   │  ├─ blockstates/
   │  └─ sounds.json
   └─ data/my_mod/
      ├─ recipe/
      ├─ loot_table/
      ├─ tags/
      └─ advancement/
```

职责分离：

- `registry`：注册表对象与延迟注册。
- `event`：游戏事件监听。客户端事件和通用事件分开。
- `client`：仅客户端代码，如渲染器、界面、按键绑定。
- `network`：客户端与服务端包定义、编解码、处理逻辑。
- `data`：数据生成器与运行时存档数据。
- `resources/assets`：客户端资源。
- `resources/data`：数据包资源，服务端可读取。

禁止在通用初始化路径中直接引用客户端专属类。独立服务器没有客户端类，错误引用会导致服务端崩溃。

## 5. 模组元数据

`src/main/resources/META-INF/neoforge.mods.toml` 定义模组元数据、依赖和展示信息。至少维护：

- `modId`：与代码、资源目录一致；
- `version`：推荐来自 Gradle 项目版本；
- `displayName`、`description`、`authors`；
- Minecraft 与 NeoForge 依赖版本范围；
- license、issueTrackerURL、logoFile（如有）。

发布前检查元数据中没有保留 MDK 示例值。

## 6. 内容注册

NeoForge 使用注册表注册游戏内容。采用 MDK 当前模板提供的延迟注册方式，不要在静态初始化中直接构造并写入原版注册表。

注册内容通常包括：

- 物品：`Item`；
- 方块：`Block`；
- 方块物品：`BlockItem`；
- 生物实体：`EntityType`；
- 方块实体：`BlockEntityType`；
- 菜单：`MenuType`；
- 创造模式标签页：`CreativeModeTab`；
- 声音、粒子、效果、数据组件等。

示例模式：

```java
public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MyMod.MOD_ID);

    public static final DeferredItem<Item> RUBY = ITEMS.registerSimpleItem("ruby");

    private ModItems() {}
}
```

在主模组类中绑定注册器：

```java
@Mod(MyMod.MOD_ID)
public final class MyMod {
    public static final String MOD_ID = "my_mod";

    public MyMod(IEventBus modBus) {
        ModItems.ITEMS.register(modBus);
    }
}
```

具体泛型、注册 API 以所用 MDK 版本为准。NeoForge 小版本升级可能调整注册类名与 builder API。

## 7. 资源与数据文件

### 7.1 客户端资源

物品 `ruby` 常见资源：

```text
assets/my_mod/
├─ lang/zh_cn.json
├─ lang/en_us.json
├─ models/item/ruby.json
└─ textures/item/ruby.png
```

翻译示例：

```json
{
  "item.my_mod.ruby": "红宝石"
}
```

资源路径和 JSON ID 必须全小写。纹理文件通常为 PNG，物品纹理尺寸常用 `16x16`、`32x32` 或 `64x64`。

### 7.2 服务端数据

配方、战利品表、标签、进度属于数据包内容。它们应放在：

```text
data/my_mod/
```

需重点维护：

- 方块掉落战利品表。未配置时方块通常不会正常掉落；
- 物品/方块标签。用于矿物词典式兼容、配方归类、工具挖掘等级等；
- 配方。避免只在创造模式中可获得；
- 世界生成数据。矿石、结构、生物群系修改必须通过目标版本推荐的数据驱动机制实现。

## 8. 事件机制

NeoForge 事件大体分两类：

| 类型 | 用途 |
| --- | --- |
| Mod Event Bus | 注册内容、客户端扩展、配置加载等模组生命周期事件 |
| Game Event Bus | 玩家登录、方块交互、实体死亡、Tick 等运行时游戏事件 |

事件处理规则：

1. 先确认事件运行的物理端或逻辑端。
2. 涉及世界状态、背包、伤害、实体生成等权威数据时，只在服务端修改。
3. Tick 事件避免遍历全部世界实体或执行磁盘/网络 I/O。
4. 可取消事件时，明确取消条件，避免误伤其他模组逻辑。
5. 事件监听器不存储过期 `Level`、`Player`、`Entity` 引用。

## 9. 客户端、服务端与联机

### 9.1 权威原则

服务端决定游戏状态。客户端负责输入、展示、预测和渲染。

客户端不能直接修改：

- 玩家背包；
- 世界方块；
- 实体生命值；
- 任务进度；
- 持久化存档数据。

正确流程：

```text
客户端输入 → C2S 数据包 → 服务端校验并修改状态 → S2C 数据包/原版同步 → 客户端显示
```

### 9.2 网络包要求

每个自定义网络包需要：

- 稳定包 ID；
- 明确编解码；
- 方向约束：C2S 或 S2C；
- 服务端/客户端线程安全处理；
- 服务端对玩家权限、距离、目标存在性、物品状态进行校验。

永远不要信任客户端传入数值，例如伤害、数量、坐标、目标实体 ID 或是否拥有物品。

### 9.3 开发验证

单人模式不等于联机测试。每个网络功能至少测试：

1. `runServer` 启动独立服务端；
2. 两个开发客户端连接；
3. 不同玩家同时操作；
4. 玩家断线重连；
5. 世界保存后重启服务端。

## 10. 配置与存档数据

配置适合存放服主或用户可调整的规则，例如数值倍率、功能开关、生成概率。

- Common 配置：客户端与服务端都可能需要；
- Server 配置：按存档或服务器规则管理；
- Client 配置：按本地画面、按键、HUD 偏好管理。

持久化数据按作用域选择：

| 数据归属 | 推荐位置 |
| --- | --- |
| 玩家专属进度 | 玩家附加数据或目标版本推荐的玩家持久化方案 |
| 世界全局状态 | `SavedData` 或对应世界持久化方案 |
| 方块内部库存/状态 | Block Entity |
| 实体专属状态 | Entity 附加数据或自定义实体字段 |
| 仅本次运行缓存 | 内存，不写存档 |

存档数据必须考虑版本迁移。字段改名、删除或语义变化时，保留默认值与旧数据兼容迁移逻辑。

## 11. Data Generator

内容数量增加后，使用 Data Generator 生成模型、方块状态、语言文件、配方、标签、战利品表。

收益：

- 减少手写 JSON 路径错误；
- 注册 ID 改动后统一更新；
- 使掉落、模型、标签覆盖率可审查；
- 降低内容扩展成本。

生成的资源是否提交 Git，按团队规范统一。通常提交运行时需要的 JSON；不要提交 `build/`、`.gradle/`、IDE 临时目录与运行日志。

## 12. 调试与测试清单

### 12.1 常用任务

```bat
gradlew.bat runClient
gradlew.bat runServer
gradlew.bat build
gradlew.bat clean build
```

`clean build` 会清除构建产物，排查资源或缓存异常时使用；不要将其作为每次调试的默认命令。

### 12.2 提交前检查

- [ ] 客户端可启动，无注册冲突；
- [ ] 独立服务端可启动，无客户端类加载错误；
- [ ] 新物品有名称、模型、纹理；
- [ ] 新方块有方块状态、模型、掉落表与方块物品；
- [ ] 生存模式可正常获取或配方合理；
- [ ] 单人、局域网/独立服务端均验证；
- [ ] 中文与英文翻译完整；
- [ ] 不输出调试日志、密钥、绝对本机路径；
- [ ] `gradlew.bat build` 成功；
- [ ] 在干净实例安装生成 JAR 并测试。

## 13. 构建与发布

执行：

```bat
gradlew.bat build
```

产物通常位于：

```text
build/libs/
```

发布包要求：

- 使用 `build/libs/` 中非 `-sources`、非 `-dev` 的主 JAR；
- 建议发布文件名带模组名、Minecraft 版本、模组版本；这不是本项目当前默认命名规则。[build.gradle](<../build.gradle>) 与 [gradle.properties](<../gradle.properties>) 当前默认产物名为 `archaeologycompass-0.1.2.jar`（本轮构建待确认），未自动附加 Minecraft 版本；
- 明确依赖的 NeoForge 与 Minecraft 版本范围；
- 标记客户端、服务端或双端可用；
- 提供变更记录、许可证、问题反馈地址；
- 发布前在无开发环境的客户端或服务端实例安装验证。

不要发布 `src/`、`build/`、`.gradle/` 或包含开发配置的压缩包。

## 14. Git 忽略建议

保留：

```text
gradlew
gradlew.bat
gradle/wrapper/
build.gradle
gradle.properties
settings.gradle
src/
```

忽略：

```text
.gradle/
build/
run/
.idea/
*.iml
out/
```

若团队共享 IntelliJ 项目配置，仅提交经约定的配置文件，不提交个人工作区文件。

## 15. 多版本支持与升级策略

首发分支固定 Minecraft `1.21.1` 与其兼容 NeoForge 版本。后续支持版本采用“共享设计、版本独立实现”策略：

- 每个 Minecraft 版本使用独立 Gradle 子项目或独立维护分支；每个版本产出独立 JAR；
- 共享内容 ID、数据包命名、配置键和网络语义；避免因版本分支导致存档和配置语义漂移；
- 将注册、事件、网络、模型渲染等易变 API 隔离在版本专属代码中；功能规则、扫描策略、标签约定保持一致；
- 每个目标版本锁定自己的 JDK、Minecraft、NeoForge 和 Gradle Wrapper 版本；
- 新版本发布前，在该版本执行客户端、独立服务端、双客户端联机和干净实例安装测试。

Minecraft 和 NeoForge 大版本升级常包含映射、注册、数据格式、网络 API、渲染 API 改动。升级顺序：

1. 建立独立升级分支；
2. 升级 MDK / Gradle / Java 到目标要求；
3. 先让 `runClient` 与 `runServer` 编译启动；
4. 修复注册与事件 API；
5. 修复资源、数据生成和数据包格式；
6. 验证存档兼容与联机；
7. 合并前完成完整构建与回归测试。

不要把功能开发和大版本迁移混在同一提交中。

## 16. 首个可发布功能建议

按以下顺序做最小闭环：

1. 注册一个物品；
2. 加入中英文名称、模型、纹理；
3. 加入创造模式标签页；
4. 添加合成配方；
5. 添加一个方块及掉落表；
6. 添加服务端事件逻辑；
7. 添加需要同步的客户端表现；
8. 独立服务端双客户端联机测试；
9. 执行 `gradlew.bat build` 并安装测试。

先完成可验证闭环，再扩展复杂系统，如 GUI、机器、多方块结构、任务系统、维度或世界生成。
