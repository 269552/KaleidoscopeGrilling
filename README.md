# 森罗物语：烟火 Kaleidoscope Grilling

《森罗物语：烟火》是《森罗物语：厨房》的烧烤与热食扩展，为游戏加入动态烤串、固定烤串、风味油料、调料调制、榨油、新作物、烟火气机制和配套厨房设备。

Kaleidoscope Grilling is an expansion for Kaleidoscope Cookery focused on charcoal grilling, skewers, hot food, flavored oils, seasoning, food processing, and new ingredients.

当前版本：`1.1.1a`

## 支持版本

| Minecraft | 加载器 | Java | 森罗物语：厨房 |
| --- | --- | --- | --- |
| 1.20.1 | Forge 47.3.0+ | 17 | 1.4.1+ |
| 1.21.1 | NeoForge 21.1+ | 21 | 1.4.1+ |

《森罗物语：厨房》是必需依赖。JEI、Jade、机械动力、东方女仆、KubeJS 和《下单了》均为可选兼容。

## 主要内容

- 左手持木棍、右手选择食材，自由组合秘制串；也可以按照串谱制作拥有独立模型与效果的固定串。
- 使用烧烤架完成点火、放串、刷油、翻面、撒调料、烤熟和烤焦的完整流程。
- 菜籽油、辣椒油和熔岩辣椒油会为烤串及炒菜保留不同时长的🔥烟火气。
- 调料瓶可以装入不同的调料粉，为食物附加额外效果。
- 加入油菜、洋葱、红薯、折耳根和花椒树等作物与食材，以及榨油器、大缸、烤串盘和高级厨具架等设备。
- JEI 展示烧烤、穿串、榨油和调料配方；Jade 展示设备与女仆的工作状态。

## 1.1 系列亮点

### 烤串食用

- 所有烟火烤串拥有独立食用动画、进度条和专属音效。
- 食用达到 1.25 秒检查点后即可松开按键并完成结算；未达到检查点则取消且不消耗食物。
- 结算使用正常的食用流程，可以继承串中食材的食物效果，并兼容记录玩家进食历史的其他模组。
- 可通过配置关闭独立动画，改用总时长仍为 1.25 秒的原版食用方式。
- 新增羊肉串、黄金烤串和“普通”烤串，并重制固定串、秘制串及作物的 GUI 图标和模型。

### 东方女仆

- 女仆可以执行完整烧烤流程，并从背包或附近容器寻找生串、打火工具、油壶和调料瓶。
- 支持普通箱子、木桶、《下单了》冰箱和高级厨具架；搜索范围与高级厨具架一致。
- 女仆持有可继续烤制的生串时会暂时保留工具，避免反复归还和取用。
- 默认将熟串放入女仆背包；配置烧烤隙间后，会优先送入隙间绑定的容器。
- 工作过程提供动作、气泡提示和 Jade 状态显示。

### 机械动力

- 动力锯砍伐花椒木时可以收获花椒，作物支持研磨轮和粉碎轮加工。
- 机械手与传送带可以按顺序组装调料瓶、穿制固定串和秘制串。
- 机械冲压机可以驱动榨油器，每次冲压增加 8 点榨油进度，油渣可由底部传送带输出。
- 工作盆可以使用流体合成辣椒油和熔岩辣椒油，注液器可以灌装空桶和空油壶。
- 注液器只接受空油壶，已有液体的油壶会拒绝注液；机械手不会食用或使用已经完成的烤串。

### 容器整理

- 箱子、木桶及兼容冰箱可以合并同类且烟火气时间接近的食物。
- 冰箱普通整理只合并时限相差不超过 5 分钟的同类烤串；按住 Shift 整理可合并全部同类烤串。
- 合并后的烟火气时间按物品数量加权平均，并包含双端防复制校验。

## 配置

首次启动后会生成 `config/kaleidoscope_grilling-common.toml`。所有说明均同时提供中文和英文。

| 配置项 | 默认值 | 作用 |
| --- | --- | --- |
| `hot_food.enableCookeryFoodHeatAndSeasoning` | `true` | 允许厨房炒锅、炖锅为料理添加烟火气和调料效果 |
| `hot_food.enableSmeltedFoodHeat` | `false` | 让熔炉及烟熏炉产出的食物获得烟火气 |
| `hot_food.smeltedFoodSeconds` | `30` | 熔炉料理烟火气的持续秒数 |
| `hot_food.hotSaturationPercent` | `125` | 带烟火气食物的饱和度倍率百分比 |
| `skewers.allowSkewersAtFullHunger` | `true` | 允许满饱食度时继续食用烤串 |
| `skewers.enableEatingAnimations` | `true` | 启用独立食用动画、进度条和专属音效 |
| `rendering.enableSkewerGuiCache` | `true` | 缓存已完成烤串的 GUI 图标 |
| `rendering.useFixedSkewer64xCache` | `false` | 为固定串使用生成的 64×64 GUI 图标 |
| `rendering.useCustomSkewer64xCache` | `false` | 为秘制串使用生成的 64×64 模型截图 |
| `maid_grilling.enabled` | `true` | 启用女仆烧烤任务 |
| `maid_grilling.bubbleCooldownTicks` | `600` | 同类女仆状态气泡再次出现前的冷却时间 |
| `maid_grilling.actionSpeedMultiplier` | `1.0` | 女仆烧烤动作和动画的速度倍率 |

## KubeJS 串类扩展

Forge 1.20.1 与 NeoForge 1.21.1 均支持通过 KubeJS 扩展串类。穿串配方应放入 `kubejs/server_scripts`，进入世界时自动加载；修改后执行 `/reload` 即可同步到客户端和 JEI。

已有固定串也可以使用 `Grilling.modifyFixedSkewer(rawId, options)` 调整穿串材料、额外 Buff、持续时间、食用动画和模型来源；未填写的字段会保留原设置。

以下示例把三个苹果穿成外部模组物品：

```javascript
Grilling.threadingRecipe("example_mod:apple_skewer", [
  "minecraft:apple",
  "minecraft:apple",
  "minecraft:apple"
])
```

完整接口支持以下五种模式：

1. 生串和熟串物品都不存在，由 KubeJS 注册物品并使用烟火生成的模型。
2. 生串和熟串物品已经存在，将外部物品完整接入烟火的穿串、烤制和食用流程。
3. 只有生串物品，由烟火生成对应的熟串结果。
4. 只添加木棍穿串转换，完成后变为外部物品，不接管其模型、效果和食用行为。
5. 修改烟火已有固定串的穿串材料、额外 Buff、持续时间、食用动画和模型来源。

物品注册需要放入 `kubejs/startup_scripts` 并重启游戏。五种模式的参数、模型来源和完整示例参见 [兼容开发指南](COMPATIBILITY.md#kubejs-五种串类模式forge-1201-与-neoforge-1211)。

## 第三方兼容接口

模组公开烧烤架自动化、串类、烟火气、榨油容器、调料瓶和高级厨具架接口。第三方模组可以在不模拟玩家的情况下接入烧烤流程，也可以通过物品标签扩展高级厨具架支持的调料和工具。

接口签名、调用时机、数据格式和事务安全要求参见 [COMPATIBILITY.md](COMPATIBILITY.md)。

## 构建

构建会从 Maven 仓库获取开发依赖，无需将依赖源码或 JAR 放入本仓库。

NeoForge 1.21.1：

```powershell
.\gradlew.bat :neoforge-1.21.1:build
```

Forge 1.20.1：

```powershell
Set-Location forge-1.20.1
.\gradlew.bat build
```

构建产物分别位于 `neoforge-1.21.1/build/libs/` 和 `forge-1.20.1/build/libs/`。

## 项目结构

- `common/`：两版共用的数据包、语言、模型、贴图和音效资源。
- `forge-1.20.1/`：Forge 1.20.1 源码与构建配置。
- `neoforge-1.21.1/`：NeoForge 1.21.1 源码与构建配置。
- `COMPATIBILITY.md`：第三方模组兼容开发指南。

Forge 与 NeoForge 的公共行为应保持一致，平台差异应限制在注册、事件和能力接口层。

## 许可证

- Java 源码采用 [BSD 3-Clause License](LICENSE-CODE)。
- 模型、贴图、音频、语言和其他资源采用 [CC BY-NC-SA 4.0](LICENSE-ASSETS)。
- 项目依赖并包含对《森罗物语：厨房》的适配，归属信息见 [NOTICE](NOTICE)。
