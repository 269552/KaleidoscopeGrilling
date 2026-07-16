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

## 当前实现（2026-07-14）

- 实际生成 39 个有效流程模型：一食材 3 个、二食材 9 个、三食材 27 个。
- 模型位于 `assets/kaleidoscope_grilling/models/item/skewer_states/state_<code>.json`。
- `unfinished_skewer.json` 与 `secret_skewer.json` 共用上述 39 个 predicate 覆盖。
- 每个食材几何块沿串签方向拆分为 6 个 tint 色块。
- `food1` 使用 tint `0-5`，`food2` 使用 tint `8-13`，`food3` 使用 tint `16-21`。
- 客户端从对应原始 `ItemStack` 的物品模型粒子贴图中段采集 6 个非透明代表色。
- 采色缓存按物品注册名与组件/NBT hash 区分，资源包重载时清空。
- 烧烤架渲染复制物品并写入仅客户端使用的视觉阶段，不修改服务器保存的真实串签数据。
- 视觉阶段为：原色、刷油增亮、浅熟、中熟、熟制暖色、焦黑。
