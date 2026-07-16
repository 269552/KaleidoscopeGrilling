# 森罗物语：烟火 - 模组兼容开发指南

本文面向希望与“森罗物语：烟火”联动的模组或数据包作者，汇总当前公开 Java API、数据包格式、物品标签和标准能力接口。

## 基本信息

- Mod ID：`kaleidoscope_grilling`
- Java 包：`cn.breezeth.kaleidoscope_grilling`
- Forge：Minecraft 1.20.1
- NeoForge：Minecraft 1.21.1
- 当前接口文档版本：v0.4

普通兼容优先使用物品标签和数据包。只有需要读取 NBT、数据组件或运行时状态时，才建议调用 Java API。

如果“森罗物语：烟火”是可选依赖，请先通过对应加载器的 `ModList` 判断 `kaleidoscope_grilling` 是否已加载，并将直接引用 API 类的代码放在独立兼容类中，避免缺少模组时触发类加载错误。

## API 总览

| 能力 | 接口或数据入口 | 用途 |
| --- | --- | --- |
| 热属性 | `HotFoodApi` | 标记热食、查询热状态、注册默认热时长、复制调料 |
| 榨油进度 | `OilPressApi` | 由其他机械或交互向榨油器提交进度 |
| 榨油容器 | `OilPressContainerApi` | 注册可接收榨油器菜籽油输出的容器 |
| 串类扩展 | `SkewerCompatApi` | 注册特殊原料判定和自定义熟制结果 |
| 固定串 | `data/<namespace>/grilling/*.json` | 声明原料、生串和熟串映射 |
| 调料材料 | `data/<namespace>/grilling/*.json` | 将第三方物品映射到已有调料效果种类 |
| 大缸自动化 | 标准 `IFluidHandler` | 通过管道、泵或其他流体容器输入输出 |

## 调用时机

所有 `register...` 方法都应只调用一次，建议在物品注册完成后的公共初始化阶段执行。

涉及客户端交互预测或 HUD 探测的接口必须在客户端与服务端都注册相同规则：

- `HotFoodApi.registerHeatDuration`
- `OilPressContainerApi.register`
- `SkewerCompatApi.registerIngredientRule`
- `SkewerCompatApi.registerCookingRule`

重复使用同一个 `ResourceLocation` 注册处理器会抛出 `IllegalArgumentException`。

## 热属性 API

类：`cn.breezeth.kaleidoscope_grilling.HotFoodApi`

### 注册默认热时长

```java
HotFoodApi.registerHeatDuration(ModItems.MY_STEW.get(), 120);
```

单位为秒，必须大于 0。未注册物品的默认热时长为 30 秒。

### 将某个物品堆标记为热食

```java
HotFoodApi.makeHot(resultStack, level);
HotFoodApi.makeHot(resultStack, level, 180);
```

- 两个参数版本使用该物品注册的默认时长。
- 三个参数版本为本次物品堆指定时长，单位为秒。
- 方法直接修改传入的 `ItemStack`，应对实际输出堆调用。
- 热状态按世界 `gameTime` 记录，不使用现实时间。

### 查询热状态

```java
int defaultSeconds = HotFoodApi.getHeatDurationSeconds(stack);
if (HotFoodApi.isHot(stack, level)) {
    // 自定义联动逻辑
}
```

### 将调料复制到食物

```java
HotFoodApi.season(foodStack, seasoningStack);
```

该方法只复制“特制调料”保存的材料数据。调料效果需要食物仍具有热属性，并在本模组的食用结算流程中应用。

## 榨油进度 API

类：`cn.breezeth.kaleidoscope_grilling.OilPressApi`

```java
boolean accepted = OilPressApi.addProgress(level, pressPos, 2);
int total = OilPressApi.requiredProgress(); // 当前为 16
```

`addProgress` 仅在服务端可能成功。返回 `false` 的常见原因：

- 指定位置不是榨油器。
- 榨油器内不足 4 个油饼。
- `amount <= 0`。
- 榨油器正处于等待输出容器状态。

达到 16 进度后，榨油器进入完成动画和输出流程。此 API 本身不施加玩家右键的 0.5 秒冷却，机械或其他模组应自行控制调用频率。

### 数据包压榨工具

标签：`#kaleidoscope_grilling:press_stones`

标签中的物品右键榨油器时，每次增加 1 点进度。铁砧方块物品始终增加 4 点，不需要加入此标签。

Forge 1.20.1 路径：

```text
data/kaleidoscope_grilling/tags/items/press_stones.json
```

NeoForge 1.21.1 路径：

```text
data/kaleidoscope_grilling/tags/item/press_stones.json
```

示例：

```json
{
  "replace": false,
  "values": ["example_mod:basalt_hammer_block"]
}
```

## 榨油输出容器 API

类：`cn.breezeth.kaleidoscope_grilling.OilPressContainerApi`

榨油器每批输出 4 桶菜籽油。默认扫描范围为榨油器水平正负 4 格、垂直正负 2 格。

### 注册容器处理器

```java
OilPressContainerApi.register(ResourceLocation.tryParse("example_mod:oil_tank"),
        new OilPressContainerApi.Handler() {
            @Override
            public OilPressContainerApi.Probe probe(Level level, BlockPos pos, int canolaBuckets) {
                if (!(level.getBlockEntity(pos) instanceof ExampleTankBlockEntity tank)) {
                    return OilPressContainerApi.Probe.NOT_CONTAINER;
                }
                if (!tank.acceptsCanolaOil()) {
                    return OilPressContainerApi.Probe.INCOMPATIBLE;
                }
                return tank.canInsertBuckets(canolaBuckets)
                        ? OilPressContainerApi.Probe.READY
                        : OilPressContainerApi.Probe.FULL;
            }

            @Override
            public boolean insert(Level level, BlockPos pos, int canolaBuckets) {
                return level.getBlockEntity(pos) instanceof ExampleTankBlockEntity tank
                        && tank.insertCanolaBucketsAtomically(canolaBuckets);
            }
        });
```

### `Probe` 语义

| 值 | 含义 |
| --- | --- |
| `NOT_CONTAINER` | 此位置不是该处理器负责的容器 |
| `READY` | 容器可完整接收指定桶数 |
| `FULL` | 是有效容器，但剩余容量不足 |
| `INCOMPATIBLE` | 是有效容器，但当前流体种类或状态不兼容 |

实现要求：

- `probe` 必须无副作用，可在客户端 HUD 探测中调用。
- `insert` 只在真正输出时调用，并应执行整批原子插入。
- 禁止只插入一部分后返回 `false`，否则会造成流体复制或损失。
- `canolaBuckets` 使用桶为单位，不是 mB。
- 处理器应在两端注册；真正修改容器内容只应发生在服务端。

API 还公开以下查询方法，通常无需由第三方主动调用：

```java
OilPressContainerApi.TransferResult result =
        OilPressContainerApi.probeNearby(level, pressPos, 4);

OilPressContainerApi.TransferResult inserted =
        OilPressContainerApi.insertNearby(level, pressPos, 4);
```

## 串类兼容

类：`cn.breezeth.kaleidoscope_grilling.SkewerCompatApi`

### 默认规则

- 具有食物属性的物品默认可以串入。
- 非食物物品可通过允许标签加入。
- 禁止标签优先级最高。
- 秘制烤串会保存原始 `ItemStack`，因此第三方食物的 NBT 或数据组件可以参与食用效果继承。

### 串类标签

| 标签 | 用途 |
| --- | --- |
| `#kaleidoscope_grilling:skewerable_ingredients` | 允许骨头等非食物原料串入 |
| `#kaleidoscope_grilling:unskewerable_ingredients` | 禁止特定食物或物品串入 |
| `#kaleidoscope_grilling:raw_skewers` | 将第三方生串标记为烧烤架可接受物品 |

扩展这些目标标签时必须使用本模组命名空间：Forge 1.20.1 放在 `data/kaleidoscope_grilling/tags/items/`，NeoForge 1.21.1 放在 `data/kaleidoscope_grilling/tags/item/`。

示例：

```json
{
  "replace": false,
  "values": ["example_mod:cheese_wedge"]
}
```

### 固定串数据格式

文件放在：

```text
data/<你的命名空间>/grilling/<任意文件名>.json
```

示例：

```json
{
  "skewers": [
    {
      "id": "example_mod:raw_cheese_skewer",
      "cooked_result": "example_mod:grilled_cheese_skewer",
      "ingredients": [
        ["#example_mod:cheese_wedges"],
        ["minecraft:beetroot", "example_mod:onion_slice"]
      ]
    }
  ]
}
```

字段说明：

| 字段 | 必需 | 说明 |
| --- | --- | --- |
| `id` | 是 | 已注册的生串物品完整 ID |
| `cooked_result` | 推荐必填 | 已注册的熟串物品完整 ID |
| `ingredients` | 是 | 1 至 3 个原料槽 |

每个原料槽是候选选择器数组，支持物品 ID 和以 `#` 开头的物品标签。声明在固定串数据中的 `id` 会自动被烧烤架识别，即使没有加入 `raw_skewers` 标签。

旧数据未提供 `cooked_result` 时，仅在生串路径以 `raw_` 开头的情况下，按相同命名空间推断 `grilled_`。无效熟串映射会降级为迷之烤串。

### 特殊原料 Java 规则

适用于依赖 NBT、数据组件或实体状态的原料：

```java
SkewerCompatApi.registerIngredientRule(
        ResourceLocation.tryParse("example_mod:filled_food_rule"),
        (stack, eater) -> {
            if (!stack.is(ModItems.SPECIAL_CONTAINER_FOOD.get())) {
                return SkewerCompatApi.Decision.PASS;
            }
            return hasRequiredFilling(stack)
                    ? SkewerCompatApi.Decision.ALLOW
                    : SkewerCompatApi.Decision.DENY;
        });
```

规则按注册顺序执行，第一个返回 `ALLOW` 或 `DENY` 的规则结束判断；`PASS` 表示交给后续规则、标签和默认食物判定。

其他模组如需复用最终判定，可调用：

```java
boolean accepted = SkewerCompatApi.canSkewer(stack, livingEntity);
```

### 自定义熟制 Java 规则

```java
SkewerCompatApi.registerCookingRule(
        ResourceLocation.tryParse("example_mod:component_skewer_cooking"),
        raw -> {
            if (!raw.is(ModItems.RAW_COMPONENT_SKEWER.get())) return ItemStack.EMPTY;
            ItemStack cooked = new ItemStack(ModItems.COOKED_COMPONENT_SKEWER.get());
            copyMyComponents(raw, cooked);
            return cooked;
        });
```

规则按注册顺序执行，第一个返回非空 `ItemStack` 的规则优先于固定串 JSON 映射。应返回新的物品堆，不要直接修改烧烤架中的输入堆。

`SkewerCompatApi.customCookedResult(raw)` 用于查询 Java 规则结果；它不查询固定串 JSON，通常应由本模组烧烤架内部调用。

## 调料材料数据扩展

第三方材料可以映射到本模组已有调料效果种类，无需 Java 代码：

```json
{
  "seasoning_effects": [
    {"ingredient": "example_mod:swift_spice", "kind": "speed"},
    {"ingredient": "example_mod:power_spice", "kind": "strength"}
  ]
}
```

文件同样放在 `data/<namespace>/grilling/*.json`。当前支持的 `kind`：

| kind | 对应行为 |
| --- | --- |
| `speed` | 速度效果材料 |
| `strength` | 力量效果材料 |
| `duration` | 延长调料效果时间 |
| `totem` | 重金属效果材料 |
| `vitality` | 龙血效果材料 |

当前数据接口只能把材料映射到已有种类，不支持仅通过 JSON 创建全新的调料效果算法。

## 大缸标准流体能力

大缸容量为 64 桶，同一时间只能容纳一种流体。自动化模组应优先使用标准流体能力，不要直接访问 `BigVatBlockEntity` 的内部字段。

Forge 1.20.1：

```java
level.getBlockEntity(pos).getCapability(ForgeCapabilities.FLUID_HANDLER, side);
```

NeoForge 1.21.1：

```java
level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side);
```

标准能力支持原版水、本模组油类及其他模组注册的流体。实际传输量使用加载器标准流体单位。

## 兼容实现检查表

1. 确认 `kaleidoscope_grilling` 已加载后再引用 Java API。
2. 普通内容优先使用标签和 `grilling/*.json`。
3. 注册型 Java API 只调用一次，并在两端保持一致。
4. 所有世界和方块实体修改只在服务端执行。
5. 榨油容器的 `probe` 不得修改状态，`insert` 必须整批原子执行。
6. 自定义熟串必须先注册实际物品，再在数据或代码中返回它。
7. 为 Forge 与 NeoForge 分别放置正确目录形式的物品标签。
8. 使用 `/reload` 测试数据包更新，并检查日志中的 JSON 解析和未知物品 ID 报错。

## 稳定性说明

上述类和数据入口是计划保留的公开兼容面。当前项目仍处于 v0.4 开发阶段；发布前若必须调整签名，应同步更新本文并在版本说明中标记破坏性变更。第三方模组不应调用未在本文列出的内部类或直接读写本模组私有 NBT/数据组件键。
