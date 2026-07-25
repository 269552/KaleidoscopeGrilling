# 森罗物语：烟火 Kaleidoscope Grilling

《森罗物语：烟火》是《森罗物语：厨房》的烧烤与热食扩展，为游戏加入动态烤串、风味油料、调料调制、榨油、新食材、热食机制和配套厨房设备。

Kaleidoscope Grilling is an expansion for Kaleidoscope Cookery focused on grilling, hot food, flavored oils, seasoning, food processing, and new ingredients.

## 支持版本

| Minecraft | 加载器 | Java | 森罗厨房 |
| --- | --- | --- | --- |
| 1.20.1 | Forge 47.4.0+ | 17 | 1.4.1+ |
| 1.21.1 | NeoForge 21.1+ | 21 | 1.4.1+ |

当前开发版本：`0.9.9b`。

## 主要内容

- 固定配方与自由组合并存的动态烤串系统。
- 点火、刷油、四次翻面、撒料、烤焦和趁热食用的完整烧烤流程。
- 菜籽油、辣椒油、熔岩辣椒油及榨油器、大缸和油壶联动。
- 可放置、调制、摇晃并重复使用的特制调料瓶。
- 折耳根、红薯、油菜、洋葱和花椒等食材及联动料理。
- 签谱、烤串餐盘、高级厨具架、Jade 信息和 JEI 烹饪配方展示。
- 面向其他模组的烧烤架、串类、热食、榨油容器和厨具架兼容接口。

## 构建

构建会从 Modrinth Maven 自动获取森罗厨房，无需把依赖源码或 JAR 放入仓库。

NeoForge 1.21.1：

```powershell
.\gradlew.bat -p neoforge-1.21.1 build
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

## 自动化兼容

烧烤架提供公开的 `GrillAutomationApi`，第三方机械可以按原有规则模拟或执行放串、刷油、翻面、撒料和取出。该接口不伪造玩家，不会触发玩家动画、聊天提示或成就；手动右键仍保留完整表现。

接口签名、返回状态与安全事务示例参见 [COMPATIBILITY.md](COMPATIBILITY.md)。

## 高级厨具架兼容

高级厨具架是厨房工作站，不是通用储物容器：

- 上方 5 个分类仓接受调料容器和油壶。
- 下方 4 个分类仓接受厨刀、锅铲、点火工具等厨房工具。
- 其他模组可通过以下公开物品标签扩展这两个类别。

| 标签 | 对应仓位 | 用途 |
| --- | --- | --- |
| `#kaleidoscope_grilling:advanced_rack_seasonings` | 上方 5 格 | 注册调料瓶、油壶或其他调味容器 |
| `#kaleidoscope_grilling:advanced_rack_tools` | 下方 4 格 | 注册厨刀、锅铲、点火工具或其他厨房工具 |

标签文件路径：

- Forge 1.20.1：`data/kaleidoscope_grilling/tags/items/<标签名>.json`
- NeoForge 1.21.1：`data/kaleidoscope_grilling/tags/item/<标签名>.json`

例如，将第三方调料罐加入上方分类仓：

```json
{
  "replace": false,
  "values": [
    "example_mod:seasoning_jar"
  ]
}
```

将第三方厨具加入下方分类仓时使用相同格式，并将文件命名为 `advanced_rack_tools.json`。

需要根据 NBT 或数据组件判断物品状态时，可使用公开 Java 类：

```java
cn.breezeth.kaleidoscope_grilling.AdvancedRackCompatApi
```

它提供 `registerSeasoningItem`、`registerToolItem`、`registerSeasoningRule` 和 `registerToolRule`。注册型兼容应在物品注册完成后执行，并在客户端与服务端保持一致。

其他公开兼容接口参见 [COMPATIBILITY.md](COMPATIBILITY.md)。

## 参与开发

提交问题或代码前请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。Forge 与 NeoForge 的公共行为应保持一致，平台差异应限制在注册、事件和能力接口层。

## 许可证

- Java 源码采用 [BSD 3-Clause License](LICENSE-CODE)。
- 模型、贴图、音频、语言和其他资源采用 [CC BY-NC-SA 4.0](LICENSE-ASSETS)。
- 项目依赖并包含对《森罗物语：厨房》的适配，归属信息见 [NOTICE](NOTICE)。
