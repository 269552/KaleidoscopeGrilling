# 森罗物语：烟火

《森罗物语：烟火》是《森罗物语：厨房》的烧烤扩展，当前同时维护 Forge 1.20.1 与 NeoForge 1.21.1。

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
