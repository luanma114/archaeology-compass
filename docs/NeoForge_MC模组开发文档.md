# NeoForge Minecraft 模组开发文档

## 考古罗盘：当前实现状态（2026-09-02）

### 已实现

| 模块 | 实现 |
| --- | --- |
| 目标版本 | Minecraft `1.21.1`、NeoForge `21.1.235`、Java `21` |
| Mod ID / 包名 | `archaeologycompass` / `com.luanma114.archaeologycompass` |
| 物品与本地化 | 注册 `archaeology_compass`；加入“工具与实用物品”创造模式标签页；含中英文名称 |
| 生存获取 | 有序配方：中心指南针、上下刷子、左右铜锭，产出 1 个考古罗盘 |
| 目标数据 | `archaeology_targets` 方块标签默认包含可疑沙子和可疑沙砾 |
| 有效性 | 目标须为 `BrushableBlockEntity`；保存数据含 `loot_table` 或 `item`。首次刷扫后出现 `item` 仍保持定位，完全刷空后停止定位 |
| 扫描 | 服务端仅遍历已加载区块的方块实体，不强制加载区块；选择水平范围、垂直范围内最近目标 |
| 配置 | 服务端可配置 `horizontalRadius`、`verticalRadius`、`scanIntervalTicks` |
| 状态与同步 | 按玩家 UUID 缓存目标；目标变化、无目标、放下罗盘、登录、换维度时同步；退出时清理缓存 |
| 动态指针 | 复用原版 `LodestoneTracker` 数据组件和 `minecraft:item/compass` 模型。服务端把目标写入主手/副手考古罗盘；客户端 `ArchaeologyCompassClient` 为考古罗盘注册 `minecraft:angle` 指针属性，读取物品上的磁石目标组件驱动 32 帧指南针指向目标；无目标（组件缺失或目标为空）时返回空值，指针持续旋转 |
| 网络隔离 | 网络层仅依赖无渲染 API 的 `ArchaeologyCompassClientState`；渲染/指针属性等客户端代码位于 `client/ArchaeologyCompassClient.java`，由 `ExampleMod` 在 `Dist.CLIENT` 分支调用，独立服务端不加载该客户端类 |
| 验证 | `gradlew.bat build` 成功；开发客户端启动时曾因空 `@EventBusSubscriber` 崩溃，已移除该标注并修复 |

### 当前代码结构

```text
src/main/java/com/luanma114/archaeologycompass/
├─ ExampleMod.java                       模组入口、物品注册、配置与网络注册
├─ Config.java                           服务端扫描配置
├─ ArchaeologyCompassEvents.java         扫描、有效性判定、罗盘组件写入、玩家生命周期处理
├─ ArchaeologyCompassTargetState.java    服务端每玩家目标缓存
├─ ArchaeologyCompassTargetPayload.java  S2C 目标状态包与编解码
├─ ArchaeologyCompassNetwork.java        网络包注册与发送
├─ ArchaeologyCompassClientState.java    无客户端渲染依赖的同步目标状态
└─ client/
   └─ ArchaeologyCompassClient.java      客户端专属：注册 `minecraft:angle` 指针属性（`Dist.CLIENT`）

src/main/resources/
├─ assets/archaeologycompass/
│  ├─ lang/en_us.json
│  ├─ lang/zh_cn.json
│  └─ models/item/archaeology_compass.json  继承原版动态指南针模型
└─ data/archaeologycompass/
   ├─ recipe/archaeology_compass.json
   └─ tags/block/archaeology_targets.json
```

### 已知限制与后续工作

1. 尚未完成游戏内人工验收。必须测试配方、目标指向、无目标旋转、刷扫中途、完全刷空、放下罗盘、登录、换维度。
2. 尚未完成独立服务端与双客户端联机测试。
3. 扫描已改为方块实体遍历，但大量已加载区块或大量玩家时仍应进行 TPS 压力测试；必要时增加分帧预算或区块索引。
4. 当前使用原版指南针外观作为开发占位。正式发布前可替换为自制模型和纹理；替换时需保留 `minecraft:angle` 指针属性注册（`ArchaeologyCompassClient`）以维持指向逻辑，或实现等价客户端模型属性。
5. 客户端渲染/指针属性代码已隔离在 `client/ArchaeologyCompassClient.java`（`Dist.CLIENT`）。未来任何 `Minecraft`、`ItemProperties`、模型或渲染器引用必须放进该 `Dist.CLIENT` 专属类，通用网络类不得直接引用。指针旋转效果仍需游戏内人工验收（见第 1 条）。

## 考古罗盘：功能需求规格

### 功能目标

新增物品“考古罗盘”。持有时表现为指南针：指针持续指向扫描范围内最近的可考古方块。范围内无目标时，指针持续旋转。

### 目标方块与兼容规则

- 默认候选：原版 `minecraft:suspicious_sand`、`minecraft:suspicious_gravel`；仅当对应方块实体仍含未刷出的考古战利品时，才视为有效目标。
- 使用数据包方块标签 `<mod_id>:archaeology_targets` 定义候选方块。发布前将全文 `<mod_id>` 替换为正式 Mod ID，例如 `archaeology_compass`。
- 模组配置提供扫描半径、扫描间隔、最大扫描量等数值。
- 其他模组或整合包可通过数据包向 `<mod_id>:archaeology_targets` 添加候选方块；兼容方块还须由代码定义有效性判定，不能只依赖方块标签。

标签示例：

```json
{
  "replace": false,
  "values": [
    "minecraft:suspicious_sand",
    "minecraft:suspicious_gravel"
  ]
}
```

文件位置：

```text
src/main/resources/data/<mod_id>/tags/block/archaeology_targets.json
```

### 定位与锁定规则

1. 玩家主手或副手持有考古罗盘时，服务端按扫描间隔搜索目标。
2. 在当前维度、以玩家位置为中心的水平半径和垂直范围内搜索标签目标方块。
3. 从完整扫描结果的有效目标中选择欧氏距离最近者，记录其维度与方块坐标。
4. 已锁定目标仍存在、仍属于标签、仍通过考古战利品有效性判定、仍在范围内、仍与玩家处于同一维度时，继续指向它。
5. 锁定目标被刷空、被挖掘、超出范围或玩家切换维度时，立即清除锁定状态；仅在下一次完整扫描结束后选择新目标。扫描结果中的更近目标可在该次完整扫描结束后替换当前锁定目标。
6. 无有效目标时清除锁定状态，客户端显示持续旋转指针。

### 性能规则

不得每 Tick 扫描半径内全部方块。默认设计：

- 服务端每 `20` Tick 扫描一次；
- 默认水平半径 `64` 格，垂直半径 `32` 格；
- 每次仅检查已加载区块；不为扫描强制加载区块；
- 搜索按区块和高度分批执行；`maxBlocksPerScan` 为单 Tick 工作预算，不是一次搜索的总上限；
- 一次扫描跨多个 Tick 完成。仅在完成覆盖范围内全部已加载候选位置后，才用结果更新最近目标；
- 最近目标结果按玩家缓存，锁定目标失效时立即清除；下一次完整扫描结束后再更新；
- 扫描半径、垂直半径、间隔、单 Tick 工作预算均放入服务端配置。

若完整三维扫描仍造成卡顿，改为“已知目标索引”：区块加载时记录标签方块坐标，方块变化时维护索引，罗盘只查询当前已加载区块的索引。

### 客户端表现与同步

- 服务端为权威端：搜索、锁定、失效判定均在服务端执行。
- 服务端仅向持有罗盘的玩家同步锁定目标坐标或“无目标”状态。
- 客户端根据玩家朝向和目标坐标计算罗盘指针角度。
- 无目标时客户端使用持续旋转角度；不向服务端发送旋转状态。
- 物品模型首发按 Minecraft `1.21.1` 对应 NeoForge API 实现。模型 JSON、物品模型定义和客户端属性注册在后续版本可能变化；版本专属代码不得直接复制到其他版本分支。
- 独立服务端不得加载渲染、模型属性或其他客户端专属类。

### 配置项建议

| 键 | 默认值 | 含义 |
| --- | ---: | --- |
| `horizontalRadius` | `64` | 水平搜索半径，单位：格 |
| `verticalRadius` | `32` | 垂直搜索半径，单位：格 |
| `scanIntervalTicks` | `20` | 搜索间隔，单位：Tick |
| `maxBlocksPerScan` | `8192` | 每 Tick 最大候选方块检查数；完整扫描可跨多个 Tick |
| `requireHoldingCompass` | `true` | 仅持有罗盘时搜索 |

配置必须限制合理上下限，防止服务器将半径设得过大后发生卡顿。

### 资源与注册清单

```text
注册 ID：archaeology_compass
翻译键：item.<mod_id>.archaeology_compass
模型：按锁定的精确 Minecraft/NeoForge 版本实现
纹理：assets/<mod_id>/textures/item/archaeology_compass.png
方块标签：data/<mod_id>/tags/block/archaeology_targets.json
```

物品需加入创造模式标签页，并添加合成配方和中英文翻译。

### 验收标准

- [ ] 默认仅定位仍有未刷出战利品的可疑沙子与可疑沙砾。
- [ ] 每次完整扫描结束后，在多个目标中选择最近目标。
- [ ] 当前锁定目标被清除、超出范围或换维度后，自动更新。
- [ ] 范围内无目标时，指针持续旋转。
- [ ] 仅扫描已加载区块，不触发区块加载。
- [ ] 其他模组方块加入标签后可被定位。
- [ ] 双客户端连接独立服务端时，各自获得正确目标。
- [ ] 高扫描频率和大范围配置被限制，服务器 TPS 无明显下降。

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
- 文件名带模组名、Minecraft 版本、模组版本；
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
