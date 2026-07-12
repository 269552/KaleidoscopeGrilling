# 烤串动态模型状态映射

客户端物品属性：`kaleidoscope_grilling:skewer_state`

三个食物位分别使用 `0-3`：

- `0`：空位
- `1`：食物模型 1
- `2`：食物模型 2
- `3`：食物模型 3

状态编码：

```text
stateCode = food1 * 16 + food2 * 4 + food3
propertyValue = stateCode / 64
```

约束：

- `food1` 在有效烤串中不为空。
- 穿串过程允许 `food2`、`food3` 为空。
- 三个位均非空时共有 `3 × 3 × 3 = 27` 种组合。
- 包含中间空位状态时，代码可覆盖 `3 × 4 × 4 = 48` 种组合。
- 每次插入食材时随机确定该食材使用模型 `1/2/3`，结果写入物品数据 `SkewerModelVariants`。
- 成品继续保留 `SkewerIngredients` 和 `SkewerModelVariants`，烧烤加工时必须原样复制。
