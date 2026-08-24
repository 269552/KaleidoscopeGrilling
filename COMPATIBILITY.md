# 森罗物语：烟火 - 模组兼容开发指南

本文面向希望与“森罗物语：烟火”联动的模组或数据包作者，汇总当前公开 Java API、数据包格式、物品标签和标准能力接口。

## 基本信息

- Mod ID：`kaleidoscope_grilling`
- Java 包：`cn.breezeth.kaleidoscope_grilling`
- Forge：Minecraft 1.20.1
- NeoForge：Minecraft 1.21.1
- 当前接口文档版本：v0.8

普通兼容优先使用物品标签和数据包。只有需要读取 NBT、数据组件或运行时状态时，才建议调用 Java API。

### 洋葱通用标签

本模组洋葱已加入农夫乐事洋葱使用的平台通用标签。配方应引用标签而不是直接引用任一模组的物品 ID，从而在只安装其中一个模组时仍能正常工作。

Forge 1.20.1：

- `#forge:crops/onion`
- `#forge:vegetables/onion`

NeoForge 1.21.1：

- `#c:crops/onion`
- `#c:foods/onion`
- `#c:foods/vegetable`

这些标签由数据包提供，不会使农夫乐事成为硬依赖。

如果“森罗物语：烟火”是可选依赖，请先通过对应加载器的 `ModList` 判断 `kaleidoscope_grilling` 是否已加载，并将直接引用 API 类的代码放在独立兼容类中，避免缺少模组时触发类加载错误。

## API 总览

| 能力 | 接口或数据入口 | 用途 |
| --- | --- | --- |
| 热属性 | `HotFoodApi` | 标记热食、查询热状态、注册默认热时长、复制调料 |
| 烧烤架自动化 | `GrillAutomationApi` | 模拟或执行放串、刷油、翻面、撒料和取出 |
| 榨油进度 | `OilPressApi` | 由其他机械或交互向榨油器提交进度 |
| 榨油容器 | `OilPressContainerApi` | 注册可接收榨油器菜籽油输出的容器 |
| 串类扩展 | `SkewerCompatApi` | 注册特殊原料判定和自定义熟制结果 |
| 固定串 | `data/<namespace>/grilling/*.json` | 声明原料、生串和熟串映射 |
| 调料材料 | `data/<namespace>/grilling/*.json` | 将第三方物品映射到已有调料效果种类 |
| 大缸自动化 | 标准 `IFluidHandler` | 通过管道、泵或其他流体容器输入输出 |

## Touhou Little Maid 可选兼容

烟火在 Forge 1.20.1 与 NeoForge 1.21.1 均内置“烧烤”女仆任务。Touhou Little Maid 是可选依赖，未安装时不会加载相关任务、网络消息、Jade 组件或 Mixin。

- 女仆只使用启用“烧烤模式”的隙间存放熟串，不再区分进货与出货；目标可以是普通箱子、木桶、下单了冰箱或高级厨具架。
- 生串、油壶、特制调料和打火石优先从女仆背包获取，不足时会主动搜索 24 格内的上述容器；仍有可烤制生串时会保留可继续使用的工具，批次结束后再归还。
- 女仆调用公开的 `GrillAutomationApi`，与其他自动化共享烧烤架租约，不伪造玩家右键。
- 服务器可使用 `/kg grill unlockall` 清理残留租约；玩家可注视烧烤架使用 `/kg grill unlock`。
- Forge 开发依赖版本为 `1.5.3-forge+mc1.20.1`；NeoForge 开发依赖版本为 `1.5.3-neoforge+mc1.21.1`。

## 调用时机

所有 `register...` 方法都应只调用一次，建议在物品注册完成后的公共初始化阶段执行。

涉及客户端交互预测或 HUD 探测的接口必须在客户端与服务端都注册相同规则：

- `HotFoodApi.registerHeatDuration`
- `OilPressContainerApi.register`
- `SkewerCompatApi.registerIngredientRule`
- `SkewerCompatApi.registerCookingRule`

重复使用同一个 `ResourceLocation` 注册处理器会抛出 `IllegalArgumentException`。

## 烧烤架自动化 API

类：`cn.breezeth.kaleidoscope_grilling.GrillAutomationApi`

该接口是烧烤架的无玩家动作层，供机械动力机械手、其他自动化方块或服务端逻辑调用。玩家右键也使用同一套状态规则，但玩家动画、音效、聊天提示和成就由交互层单独处理，因此接入自动化不会改变手动玩法。

```java
GrillAutomationApi.Result inserted =
        GrillAutomationApi.insertSkewer(level, grillPos, input, false);

GrillAutomationApi.Result oiled =
        GrillAutomationApi.brushOil(level, grillPos, oilPot, false);

GrillAutomationApi.Result flipped =
        GrillAutomationApi.flip(level, grillPos, false);

GrillAutomationApi.Result seasoned =
        GrillAutomationApi.season(level, grillPos, seasoning, false);

GrillAutomationApi.Result extracted =
        GrillAutomationApi.extract(level, grillPos, false);
```

自动化控制器还可以通过快照判断当前步骤，并在跨 tick 工作时申请独占租约：

```java
UUID controllerId = machineController.getUUID();
if (GrillAutomationApi.acquire(level, grillPos, controllerId)) {
    GrillAutomationApi.Snapshot state = GrillAutomationApi.snapshot(level, grillPos);
    // 工作期间至少每 10 秒调用一次；默认租约超时为 200 tick。
    GrillAutomationApi.heartbeat(level, grillPos, controllerId);

    // 批次结束或主动取消时释放。
    GrillAutomationApi.release(level, grillPos, controllerId);
}
```

- `snapshot` 返回点燃、阶段、翻面次数、占用槽位、翻面冷却、调料、失败状态和当前自动化持有者。
- `ignite` 与 `extinguish` 提供不伪造玩家的点火/熄火入口；执行版本仍会播放对应世界声音并正确损耗打火石。
- 租约只协调自动化控制器，不限制玩家手动右键。第三方自动化若跨多个 tick 操作，必须使用 `acquire`、`heartbeat` 和 `release`，避免与女仆或其他机器争抢。
- `forceUnlock`/`forceUnlockAll` 仅供管理和故障恢复，不应作为普通工作流程的一部分。

所有动作均保留烧烤架原有顺序和限制，不能绕过点火、刷油、翻面次数、翻面冷却、撒料或完成状态直接修改阶段。

### 模拟与资源事务

- `simulate = true`：只检查当前动作是否可执行，不修改烧烤架、输入堆、油壶或调料瓶。
- `simulate = false`：真正执行动作，只允许在逻辑服务端成功。
- `insertSkewer`、`brushOil`、`season` 的四参数简写会在成功时自动扣除输入资源。
- 上述三个方法另有 `consumeInput`、`consumeOil`、`consumeSeasoning` 参数，可由拥有独立储罐或库存事务的机器设为 `false`，并在自身事务中扣除资源。
- `extract` 不会自行塞入玩家或机器库存；成功时必须接收 `Result.output()`，确认目标库存可容纳后再正式调用。
- 调料耗尽时 `Result.shouldReplaceHeld()` 为 `true`，调用方必须把原调料槽替换为 `Result.heldReplacement()` 返回的空调料瓶。

推荐采用“先模拟、确认输出空间或资源事务、再执行”的顺序：

```java
GrillAutomationApi.Result preview =
        GrillAutomationApi.extract(level, grillPos, true);
if (preview.success() && machineInventory.canAccept(preview.output())) {
    GrillAutomationApi.Result result =
            GrillAutomationApi.extract(level, grillPos, false);
    if (result.success()) machineInventory.insert(result.output());
}
```

### 返回状态

`Result.status()` 可能为 `SUCCESS`、`INVALID_TARGET`、`CLIENT_SIDE`、`NOT_LIT`、`INVALID_INPUT`、`FULL`、`EMPTY`、`WRONG_PHASE`、`COOLDOWN`、`INSUFFICIENT_RESOURCE` 或 `NOT_READY`。成功时 `affected()` 表示本次覆盖的烤串数量；取出成功时为 `1`。

自动化兼容应直接调用本接口，不建议伪造玩家右键。伪玩家路径会额外触发手部动画、聊天提示或成就，而且某些机械只检测物品数量变化，不能可靠识别油壶存量和调料耐久变化。

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
| `id` | 是 | 已注册的生串物品完整 ID；仅使用 `threading_result` 时也可以是未注册但唯一的配方 ID |
| `cooked_result` | 推荐必填 | 已注册的熟串物品完整 ID |
| `threading_result` | 可选 | 木棍穿串完成后直接输出的外部物品完整 ID |
| `ingredients` | 是 | 1 至 3 个原料槽 |

每个原料槽是候选选择器数组，支持物品 ID 和以 `#` 开头的物品标签。声明在固定串数据中的 `id` 会自动被烧烤架识别，即使没有加入 `raw_skewers` 标签。

### 木棍穿串后转换为外部物品（Forge 1.20.1 与 NeoForge 1.21.1）

固定串数据还可以声明 `threading_result`，用于只兼容穿串配方的模式。玩家仍然使用木棍逐份穿串，过程中显示烟火的动态串外观；满足全部食材后，物品直接转换为指定的其他模组物品。转换完成后烟火不会写入串数据，也不会修改该物品的模型、贴图或食用逻辑。

```json
{
  "skewers": [
    {
      "id": "kaleidoscope_grilling:external_example_threading",
      "ingredients": [
        ["example_mod:raw_meat"],
        ["minecraft:redstone"],
        ["example_mod:raw_meat"]
      ],
      "threading_result": "example_mod:cooked_meat_skewer"
    }
  ]
}
```

`threading_result` 必须指向服务器中已经注册的物品；如果物品不存在，配方会回退到普通动态串流程。

在 Forge 1.20.1 与 NeoForge 1.21.1 中，安装 KubeJS 后都可以直接在 `server_scripts` 使用同一能力。进入世界时自动加载，修改后执行 `/reload` 即可，无需重启客户端：

```js
Grilling.threadingRecipe(
  'example_mod:cooked_meat_skewer',
  [
    'example_mod:raw_meat',
    'minecraft:redstone',
    'example_mod:raw_meat'
  ]
)
```

普通新增无需填写配方 ID，烟火会根据结果和食材生成稳定 ID。需要固定 ID，以便长期覆盖或管理时，可以额外填写：

```js
Grilling.threadingRecipe(
  'example_mod:meat_skewer_threading',
  'example_mod:cooked_meat_skewer',
  [
    'example_mod:raw_meat',
    'minecraft:redstone',
    'example_mod:raw_meat'
  ]
)
```

上面的 `threadingRecipe` 是模式 4：它只负责穿串转换。生串过程仍由烟火动态渲染，完成后返回外部物品原样。

### KubeJS 五种串类模式（Forge 1.20.1 与 NeoForge 1.21.1）

物品注册必须放在 `startup_scripts`，串配方放在 `server_scripts`。这样进入世界时会自动加载，修改配方后执行 `/reload` 即可同步到客户端和 JEI；新增或删除物品仍然需要重启游戏。

#### 模式 1：生串和熟串物品都不存在

先用烟火提供的 KubeJS 物品类型注册生、熟串。默认由烟火根据配方食材生成两者的模型，熟串默认使用烟火食用动画：

```js
// kubejs/startup_scripts/grilling_items.js
StartupEvents.registry('item', event => {
  event.create('kubejs:apple_carrot_raw', 'kaleidoscope_grilling:raw_skewer')
    .displayName('苹果胡萝卜生串')

  event.create('kubejs:apple_carrot_cooked', 'kaleidoscope_grilling:cooked_skewer')
    .displayName('苹果胡萝卜熟串')
    .effect('minecraft:speed', 20)
    .animation('THREE')
})
```

```js
// kubejs/server_scripts/grilling_recipes.js
Grilling.createdSkewerRecipe(
  'kubejs:apple_carrot_raw',
  'kubejs:apple_carrot_cooked',
  ['minecraft:apple', 'minecraft:carrot', 'minecraft:apple']
)
```

`effect` 和 `animation` 都可以省略。省略效果时没有额外 Buff；省略动画时使用三段串的默认动画。动画可填写 `ONE`、`TWO`、`THREE`、`THREE_ALT`、`THREE_RANDOM` 或 `FOUR`。

#### 模式 2：生串和熟串物品都已经注册

使用 `skewerRecipe` 将现有物品接入穿串、烧烤、JEI 和食用流程。已有模型时设为 `provided`；需要烟火按食材动态生成模型时设为 `generated`：

```js
Grilling.skewerRecipe(
  'example_mod:raw_meat_skewer',
  'example_mod:cooked_meat_skewer',
  ['example_mod:raw_meat', '#forge:vegetables/onion', 'example_mod:raw_meat'],
  {
    rawModel: 'provided',
    cookedModel: 'provided',
    eating: 'default',
    effect: 'minecraft:strength',
    effectSeconds: 30
  }
)
```

`rawModel` 与 `cookedModel` 相互独立，可填：

- `auto`：默认值；烟火自有串类型使用动态模型，其他模组物品保留原模型。
- `generated`：强制使用烟火按三份食材生成的串模型和 16×16 GUI 图标。
- `provided`：完全沿用物品已有模型、贴图和 GUI 图标。

`eating` 默认为 `default`，会按照食材数量选择烟火动画；也可指定上述动画名，或填 `provided`/`none` 保留物品原有食用方式。`effect` 未填写时不附加额外效果；物品自身的食物属性和效果仍然保留。

#### 模式 3：只有生串物品

若目标熟串物品已存在，直接声明烧烤映射。可选的食材数组只用于 JEI 展示和动态模型，不会新增木棍穿串配方：

```js
Grilling.cookingRecipe(
  'example_mod:raw_skewer',
  'example_mod:cooked_skewer',
  ['example_mod:raw_meat', 'minecraft:carrot', 'example_mod:raw_meat'],
  { rawModel: 'provided', cookedModel: 'provided', eating: 'default' }
)
```

如果没有熟串物品，可让它烤熟后变为烟火的通用动态熟串：

```js
Grilling.generatedCookingRecipe(
  'example_mod:raw_skewer',
  ['example_mod:raw_meat', 'minecraft:carrot', 'example_mod:raw_meat']
)
```

后一种写法必须提供 1 至 3 份展示食材；烧烤完成后，烟火会把这些食材写入通用熟串并据此生成模型、图标和默认食用动画。

#### 模式 4：只增加木棍穿串合成

继续使用前文的 `Grilling.threadingRecipe(result, ingredients)`。它不会把结果物品注册为烟火串，也不会接管该物品的烧烤、模型、Buff 或食用动画。

#### 模式 5：修改烟火已有的固定串

脚本可以直接调整烟火自带固定串的穿串配方、额外 Buff、持续时间、模型来源和食用动画。第一个参数填写生串物品 ID；没有填写的字段会沿用原设置：

```js
// kubejs/server_scripts/grilling_fixed_skewers.js
Grilling.modifyFixedSkewer('kaleidoscope_grilling:raw_beef_skewer', {
  ingredients: [
    ['minecraft:beef', 'minecraft:cooked_beef'],
    '#forge:crops/onion',
    'minecraft:beef'
  ],
  effect: 'minecraft:speed',
  effectSeconds: 60,
  eating: 'THREE'
})
```

`ingredients` 的每一项代表木棍上的一个位置。字符串表示只接受一种物品或标签，数组表示该位置可接受其中任意一种。配方需要 1～3 个位置。

可选的 `rawModel` 和 `cookedModel` 支持 `auto`、`generated`、`provided`。当前 `effect` 只表示一个 I 级额外效果；不填写时保留固定串原有效果，填写空字符串可以移除效果。脚本在 `/reload` 后同步到客户端并刷新 JEI。

烟火的正式 JAR 会包含桥接代码，但 KubeJS 仍是可选运行依赖：未安装 KubeJS 时不会加载桥接，也不影响烟火启动。开发环境需要连同 KubeJS 启动客户端时可使用 `-PwithKubeJS`。

服务器会在玩家进入时同步这些脚本穿串配方；执行 `/reload` 后也会重新下发，并刷新客户端 JEI 中的穿串条目。多人游戏只需要服务器与客户端安装相同版本的烟火，配方脚本放在服务器的 `server_scripts` 即可。

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

## 食材与料理标签

烟火同时维护平台通用标签和稳定的模组桥接标签。第三方模组应优先向平台通用标签追加内容；烟火自身配方引用 `#kaleidoscope_grilling:ingredients/*`，用于屏蔽 Forge 1.20.1 的 `forge:` 与 NeoForge 1.21.1 的 `c:` 命名差异。

| 食材语义 | 烟火稳定桥接标签 |
| --- | --- |
| 牛肉块 | `#kaleidoscope_grilling:ingredients/beef_chunks` |
| 鸡皮 | `#kaleidoscope_grilling:ingredients/chicken_skin` |
| 鸡翅 | `#kaleidoscope_grilling:ingredients/chicken_wings` |
| 鱿鱼须 | `#kaleidoscope_grilling:ingredients/squid_tentacles` |
| 折耳根 / 折耳根沫 | `#kaleidoscope_grilling:ingredients/houttuynia` / `minced_houttuynia` |
| 胡萝卜粒、土豆片、馒头片、生苕皮 | 对应 `ingredients/carrot_dice`、`potato_slices`、`raw_mantou_slices`、`raw_sweet_potato_sheets` |
| 洋葱、红薯、油菜籽 | 对应 `ingredients/onions`、`sweet_potatoes`、`canola_seeds` |

平台标签覆盖作物、种子、蔬菜和生肉分类。Forge 使用 `#forge:crops/*`、`#forge:seeds/*`、`#forge:vegetables/*`、`#forge:raw_*`；NeoForge 使用 `#c:crops/*`、`#c:seeds/*`、`#c:vegetables/*`、`#c:foods/raw_*` 与 `#c:raw_meats`。

以下料理分类也由烟火追加，不会覆盖森罗原有内容：

- `#kaleidoscope_cookery:meals`：烟火可食用基础食材、凉菜、联动料理及全部生熟烤串。
- `#kaleidoscope_grilling:raw_skewers`：烧烤架可接收的生串。
- `#kaleidoscope_grilling:grilled_skewers`：固定熟串集合。

不要为了扩大兼容而错误归类。例如鱿鱼须不是鱼类，不能加入鱼标签；成品、战利品输出和模型引用也应继续使用精确物品 ID。

## 大缸标准流体能力

大缸容量为 8 桶，同一时间只能容纳一种流体。自动化模组应优先使用标准流体能力，不要直接访问 `BigVatBlockEntity` 的内部字段。

Forge 1.20.1：

```java
level.getBlockEntity(pos).getCapability(ForgeCapabilities.FLUID_HANDLER, side);
```

NeoForge 1.21.1：

```java
level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side);
```

标准能力支持原版水、本模组油类及其他模组注册的流体。实际传输量使用加载器标准流体单位。

## 调料瓶自动化 API

类：`cn.breezeth.kaleidoscope_grilling.SeasoningAutomationApi`

该接口用于机械或其他模组复用烟火的调料容量、基础材料和成品数据规则。调用方应先把可用库存按稳定槽位顺序组成列表，再生成本轮计划：

```java
SeasoningAutomationApi.MixPlan plan =
        SeasoningAutomationApi.plan(machineInventorySnapshot);
if (plan != null && machineInventory.canExtract(plan.takes())) {
    ItemStack result = SeasoningAutomationApi.finish(plan, level.random);
    // 先确认输出空间，再按 plan.takes() 原子扣除输入并插入 result。
}
```

- `plan` 优先处理列表中第一个有效的“待摇晃的调料”；否则选择第一个空调料瓶。
- `plan(stacks, targets)` 按目标列表顺序寻找第一份当前库存能够完整满足的调料配方；材料按物品 ID 和数量匹配，不要求放入顺序一致。
- `appendIngredient(bottle, ingredient)` 向空调料瓶或待摇晃调料中加入一份有效材料，并返回更新后的单个瓶子；容量已满或材料无效时返回空堆。集齐三种基础调料后会自动转换为待摇晃状态，但不会跳过最终摇晃步骤。
- 使用空瓶时，先锁定绿辣椒粉、花椒粉和洋葱粉各一份，再按槽位顺序选取其他有效材料，单瓶最多 8 份。
- 无效物品不参与计划，也不会阻止计划生成。超过容量的有效材料不进入本轮计划，应原样留在调用方库存中。
- `SlotTake` 指明应从哪个槽位扣除多少物品。调用方必须先模拟输出空间与全部扣除，再一次性提交，不能逐项失败后留下半成品事务。
- `finish` 生成携带完整材料、Buff 映射、随机内容物外观和 16 次初始用量的特制调料。
- `isValidIngredient` 和 `hasBase` 可用于机器过滤器、HUD 或状态提示。调料效果映射继续来自 `data/<namespace>/grilling/*.json`。

安装机械动力时，烟火已经内置工作盆与动力搅拌器实现：配方处理时长为 80 tick；无效材料及超过容量的有效材料保留在工作盆中。三种基础料各固定消耗一个，其他有效调料优先于多余基础料进入剩余容量。机械手持特制调料作用于烧烤架时，会进入 `GrillAutomationApi.season` 的统一事务规则。

列表过滤器可以进一步指定自动调制配方：将一瓶已经调制完成的特制调料作为虚影样品放入列表过滤器，设置为白名单并开启“匹配数据”，再把过滤器安装到工作盆。系统只比较样品保存的材料种类和数量，不比较调料瓶随机颜色、剩余使用次数或材料放入顺序。过滤器中可以放置多个样品，系统按照槽位顺序制作第一份材料齐全的配方；材料不足时不会启动或消耗物品，多余材料继续留在工作盆中。关闭“匹配数据”时仍使用原有的自动选择有效材料规则。

机械手也可以逐次装料：把空调料瓶或待摇晃调料放上传送带，让机械手依次手持所需调料材料作用于瓶子。每次动作只处理一瓶并加入一份材料；无效材料、满容量瓶和成品特制调料不会触发，也不会消耗机械手物品。装料完成后仍需由玩家摇晃，或送入工作盆使用动力搅拌器完成调制。

## 兼容实现检查表

1. 确认 `kaleidoscope_grilling` 已加载后再引用 Java API。
2. 普通内容优先使用标签和 `grilling/*.json`。
3. 注册型 Java API 只调用一次，并在两端保持一致。
4. 所有世界和方块实体修改只在服务端执行。
5. 榨油容器的 `probe` 不得修改状态，`insert` 必须整批原子执行。
6. 自定义熟串必须先注册实际物品，再在数据或代码中返回它。
7. 为 Forge 与 NeoForge 分别放置正确目录形式的物品标签。
8. 使用 `/reload` 测试数据包更新，并检查日志中的 JSON 解析和未知物品 ID 报错。
9. 发布前同时执行 Forge 与 NeoForge 的 `build` 任务，并在干净客户端核查资源重载、Jade、JEI 和存档迁移。

## 稳定性说明

上述类和数据入口是计划保留的公开兼容面。当前接口文档对应 v1.1 开发线；发布前若必须调整签名，应同步更新本文并在版本说明中标记破坏性变更。第三方模组不应调用未在本文列出的内部类或直接读写本模组私有 NBT/数据组件键。
