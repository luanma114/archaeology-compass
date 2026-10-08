# CurseForge 上传操作步骤

按顺序执行即可。文案内容不重复搬运，需要粘贴的段落直接指向 [`CurseForge_项目页面文案.md`](CurseForge_项目页面文案.md) 的对应小节。

> CurseForge 后台界面时有调整，字段位置以后台实际显示为准；本手册的**字段取值**和**判断依据**是稳定的。

---

## 0. 准备材料

| 用途 | 文件 | 备注 |
| --- | --- | --- |
| 项目头像 | `docs/archaeology_compass_icon_400.png` | 400×400，上传到 Logo Image 字段 |
| 上传的文件 | `build/libs/archaeologycompass-0.1.2.jar` | 73272 字节，sha256 `a161300658f5ab9485f0db1c44deddad189da1bd94e0690e7e5b43959c215d14` |
| 首图 | `docs/screenshots/01-needle-pointing.png` | 指针指向可疑沙 |
| 合成图 | `docs/screenshots/02-crafting.png` | 工作台合成 |
| 提示图 | `docs/screenshots/03-shift-tooltip.png` | Shift 提示 |
| 美术预览 | `docs/archaeology_compass_rotation.gif` | 必须标注"非游戏截图" |
| 文案 | `docs/CurseForge_项目页面文案.md` | 所有要粘贴的文字都在里面 |

**上传前先核对 jar 的哈希**，确认传的是与 GitHub Release 一致的那一份：

```powershell
Get-FileHash build\libs\archaeologycompass-0.1.2.jar -Algorithm SHA256
# 应为 a161300658f5ab9485f0db1c44deddad189da1bd94e0690e7e5b43959c215d14
```

---

## 1. 进入作者后台，创建项目

1. 用 CurseForge / Overwolf 账号登录
2. 打开 **https://authors.curseforge.com/#/projects/create/choose-game**
3. Game 选 **Minecraft**
4. Class / Project type 选 **Mods**

---

## 2. 填写项目字段

| 字段 | 填什么 |
| --- | --- |
| **Name** | `Archaeology Compass` |
| **Summary** | `Find nearby suspicious sand and gravel with loot still inside using a craftable copper compass.` |
| **Main category** | 后台若有 `Utility & QoL` 就选它 |
| **Additional categories** | 后台若有 `Adventure and RPG` 可选；不要为了曝光加无关分类，会被退回 |
| **Class** | `Mods` |

> ⚠️ **Name 必须唯一**，重名会被直接拒绝。提交前先在 CurseForge 搜一下 `Archaeology Compass` 有没有已被占用。
>
> ⚠️ **标题和描述必须是英文**，这是硬性要求。

---

## 3. Description（项目正文）

打开 [`CurseForge_项目页面文案.md`](CurseForge_项目页面文案.md)：

- **第 2 节**是英文正文，整段复制粘贴
- **第 3 节**是可选中文说明，**放在英文之后**
- 不要把第 1、4、5、6、7 节（那是给你自己看的填表说明）贴进去

正文里的验证声明已经按实际情况写好：验过的写"已验证"，没验过的明确写"未验证、不作声明"。**不要自行改成"全部验证通过"**。

---

## 4. License（许可）

这是**最容易填错**的一步。

- 在 Project License 下拉里选 **Custom License**（列表最底部）
- 粘贴 [`CurseForge_项目页面文案.md`](CurseForge_项目页面文案.md) **第 5 节**的全文

> ⚠️ **不要选下拉列表里的 `MIT License`**。本项目是分内容授权：代码 MIT、美术 CC BY 4.0。只选 MIT 会把美术资源误标为 MIT，等于错误声明授权。

---

## 5. Logo Image

上传 `docs/archaeology_compass_icon_400.png`。

---

## 6. 上传文件（Project Dashboard → Files）

项目元数据填完后**先不要急着关**，项目要等文件上传后才会进入审核。

| 字段 | 填什么 |
| --- | --- |
| **Upload file** | `archaeologycompass-0.1.2.jar` |
| **Display Name** | `Archaeology Compass 0.1.2 — Minecraft 1.21.1 (NeoForge)` |
| **Release Type** | 见下方说明，需要你决策 |
| **Changelog** | 粘贴 [`CurseForge_项目页面文案.md`](CurseForge_项目页面文案.md) **第 4 节**的英文 changelog |
| **Supported Version** | 游戏版本 `1.21.1`；Mod Loader 勾 **NeoForge** |
| **Related Projects** | 留空 |

> ⚠️ **Mod Loader 只勾 NeoForge，不要勾 Forge 或 Fabric**。这个 jar 只支持 NeoForge，勾错会让 Forge 玩家装了崩溃。
>
> 不需要声明对 NeoForge 的"Required Dependency"——加载器是通过 Mod Loader 字段体现的，不是第三方模组依赖。

### Release Type 需要你决策

官方规则里有一条容易踩的坑：

> **Projects must have at least one "Release" file before they sync to the CurseForge App.**

| 选项 | 后果 |
| --- | --- |
| **Release** | 项目会同步进 CurseForge App，玩家能在客户端里直接搜到并一键安装。代价是"Release"这个词暗示更成熟 |
| **Beta** | 对 Minecraft 项目在网页侧边栏仍会显示，但**不会同步进 App**，玩家只能用网页搜索找到 |
| **Alpha** | 同上，收录范围更窄 |

`CurseForge_项目页面文案.md` 第 4 节目前建议 Alpha，那是**在注意到 App 同步规则之前写的**，两者需要你权衡：

- 想要 App 曝光 → 选 **Release**，靠描述里的"未验证项"声明承担诚实性
- 想保持标签与成熟度一致 → 选 **Beta**，接受 App 不收录

---

## 7. 插入图片

图片**不能从本地引用**，必须先上传，再用描述编辑器插入。上传后按 [`CurseForge_项目页面文案.md`](CurseForge_项目页面文案.md) **第 6.1 节**的位置对照表放置，图注用 **6.2 节**的英文标题与说明。

顺序很重要：**`01-needle-pointing.png` 必须是首图**，它决定项目页的第一印象。

---

## 8. 提交审核

文件上传后，项目与文件状态变为 **Under Review**，进入人工审核。三种结果：

| 结果 | 含义 | 应对 |
| --- | --- | --- |
| 通过 | 项目公开可见 | — |
| **Changes Required** | 需要修改 | 按反馈改，改完重新提交 |
| **Rejected** | 被拒 | 看清理由，修好后可重新提交 |

---

## 9. 提交前自查（对照常见拒因）

- [ ] 项目名未被占用
- [ ] 标题与描述**全为英文**
- [ ] 描述足够详细，语法正确（本项目正文已满足）
- [ ] 许可选择与正文一致，**没有把美术误标为 MIT**
- [ ] 头像已上传
- [ ] **公开页面上没有外部下载入口**（不要把 GitHub Releases 的下载链接放进项目页；指向源码仓库是允许的）
- [ ] Mod Loader 只勾 NeoForge
- [ ] 分类没有堆砌无关项
- [ ] 上传的 jar 哈希与 GitHub Release 一致
- [ ] 描述里的验证声明与实际测试情况一致，没有夸大

---

## 10. 提交之后

- 审核通常需要一段时间；被退回时按反馈逐条修改
- 项目**至少要有一个 Release 文件**才会同步进 CurseForge App（见第 6 节）
- 后续发版：重新构建 → 更新 `gradle.properties` 版本号 → 更新 `docs/releases/` 说明 → 上传新文件
- 有问题可通过项目页的 Issues 链接（已写入 `mods.toml` 的 `issueTrackerURL`）收集反馈
