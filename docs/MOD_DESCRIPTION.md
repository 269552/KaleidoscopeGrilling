**一句话描述：** 为《森罗物语：厨房》扩展热食、风味油料、新食材与炭火烧烤，让每一道料理都带上温度与烟火气。

**In one sentence:** An expansion for Kaleidoscope Cookery that adds hot food, flavorful oils, new ingredients, and charcoal grilling to bring warmth and smoky character to every meal.

# 🔥 森罗物语：烟火（Kaleidoscope Grilling）

**不只是在炭火上烤一串食物，更是让油有风味、料理有温度、田野里长出更多值得端上桌的食材。**

《森罗物语：烟火》是《森罗物语：厨房》的综合料理扩展，以 **烧烤、热食、油料与新食材** 为四条核心主线。你可以种植油菜、洋葱、红薯与折耳根，在森林中寻找花椒树；也可以研磨食材、压榨菜籽油、调制不同风味的辣椒油，让炒锅、汤锅、熔炉和烧烤架做出的食物真正带上温度。烧烤是最直观的入口，但从田间采集、食材加工到热气腾腾地端上桌，才是烟火所扩展的完整厨房生活。

---

## 📋 模组概览

- **模组名称**：森罗物语：烟火
- **英文名称**：Kaleidoscope Grilling
- **核心玩法**：种植与采集 → 加工新食材 → 榨油与调味 → 烧烤或料理 → 趁热享用
- **特色系统**：热食与多种油料体系、新作物与联动料理、动态秘制烤串、16 种固定串、签谱与烤串餐盘、调料瓶、高级厨具架、趣味 Buff 与完整动作表现

---

## 🎮 烧烤玩法

### 手持穿串

不需要打开合成界面：

1. 副手拿木棍，主手拿食材。
2. 右键将食材逐个穿到木签上。
3. 按固定顺序放入正确食材，可以完成固定配方生串。
4. 自由组合最多三种食材，则可以制作带有玩家署名的秘制烤串。

潜行右键可以拆签，一次返还木棍和全部食材；
秘制串会从食材贴图中提取颜色，动态生成各不相同的串签模型。

### 固定配方烤串

目前已包含 16 种烤串：

- 牛肉串、五花肉串、鸡皮串、中翅串
- 鱿鱼须串、鱼串、苕皮串、土豆片串
- 猪儿虫串、蘑菇串、馒头片串、末影珍珠串
- 丸子串、粘液串、骨肉相连串、煎蛋串

每种烤串都有独立的食材顺序、饱食属性、模型和效果。

### 随意搭配烤串（秘制烤串）

秘制烤串允许自由串入最多三种食材，并记录制作者名称。它的食物属性不是固定数值，而是根据实际食材动态计算：

- **熟串饱食度**：`（全部可食用食材饱食度之和 - 最低的一项）× 系数`，向下取整且最低为 1 点。
- **组合系数**：食材全部不同时为 `0.6`；存在重复食材时为 `0.5`。
- **熟串饱和倍率**：食材平均饱和倍率乘以组合系数。
- **生串折减**：生串的饱食度与饱和倍率均按熟串的 `50%` 计算，饱食度最低仍为 1 点。
- **BUFF继承**：无论生串、熟串始终继承食物本身的BUFF，如金苹果。


秘制串会结合不同食材模型生成外观。食材、顺序与模型组合不同，成品就会呈现不同颜色。配置项 `enableSkewerGuiCache` 默认为 `true`：开启后会以 `64×64` 高精度缓存 GUI 图标，在保留清晰成像的同时降低客户端 FPS 压力；关闭后则改为实时直接渲染完整模型，但同时渲染多个串（例如打开创造模式物品栏的模组标签页）会明显掉帧。

### 烧烤架

烧烤架一次可以放置 3 串食物。使用打火石点燃炭火后，完整流程为：

**放入烤串 → 使用油壶刷油 → 翻面 4 次 → 使用特制调料撒料 → 取出成品**

- 拖延太久会得到“迷之烤串”或“黑暗烧烤”。

---

## 🧂 调料瓶

类似于《森罗物语：酒馆》的调酒玩法。空调料瓶可以直接放置在地面，成为可见的调制容器。向瓶中加入材料时，瓶内会逐层显示对应颜色。

### 调制流程

1. 放置空调料瓶。
2. 加入花椒、洋葱粉、绿辣椒粉三种基础辅材。
3. 调料瓶总容量为 8 份；基础辅材齐全后，还能加入最多 5 份材料决定额外效果。
4. 取回“待摇晃的调料”，持续摇晃 4 秒。
5. 声音和粒子出现后，获得可使用 16 次的特制调料。

调料 Buff 只会在食物仍处于“热气腾腾”状态时触发，基础持续时间为 3 分钟：

| 材料 | 提供效果 | 叠加规则 |
| --- | --- | --- |
| 红石粉 | 迅捷 | 1～3 份为 I 级，4 份以上为 II 级 |
| 火药 | 力量 | 1～3 份为 I 级，4 份以上为 II 级 |
| 折耳根粉 | 延长其他调料效果 | 1～3 份使时长翻倍，4 份以上使时长变为 4 倍 |
| 不死图腾粉 | 重金属 | 4 份以上为 II 级；致命伤害时以 1 点生命复活，随后获得 10 分钟“重金属中毒”，冷却期间不能再次触发 |
| 龙蛋粉 | 龙血 | 1～3 份临时增加 6 点（3 颗心）生命上限，4 份以上临时增加 10 点（5 颗心） |
| 花椒 | 麻了 | 瓶内累计至少 4 份花椒才会触发，持续 45 秒；时长同样受折耳根粉影响 |

三种基础辅材（花椒、洋葱粉、绿辣椒粉）是完成调制的必要条件；其中花椒同时计入“麻了”的 4 份触发门槛。

---

## 🛢️ 榨油与油壶

烟火加入了一套从种植到压榨的菜籽油生产流程。

### 菜籽油生产

1. 种植并收获油菜籽。
2. 将油菜籽研磨为菜籽粉。
3. 使用 8 份菜籽粉和 1 个小麦制作油饼。
4. 向榨油器放满 4 个油饼。
5. 使用铁砧反复砸击；前期也可以用石头代替。
6. 在附近放置大缸，承接压榨出的菜籽油。

榨油共有 16 点进度：铁砧每次增加 4 点，石头每次增加 1 点。压榨完成后会自动产出油渣。

### 三种油
除了原版油脂，新增三种菜油：
- **菜籽油**：基础烧烤用油。
- **辣椒油**：提供更长时间的热食效果。
- **熔岩辣椒油**：更强的高级油，拥有发光流体与火焰粒子表现。

森罗物语：厨房的油壶最多储存 64 点油，1 点可以刷 1 串或者为炒锅添加一次油用量。
三种油同样可以用于森罗物语：厨房的炒锅，并为炒锅食物提供不同时长的热食buff。

---

## ♨️ 热气腾腾

刚完成的食物会带有“热气腾腾”状态，并随世界时间自然冷却。

- 热食会延长食物自身的正面效果。
- 辣椒油和熔岩辣椒油可以显著延长热状态。
- 可以在配置文件中设置食物处于“热气腾腾”状态时的饱和度倍率，默认为1.25倍。
- 可选配置允许熔炉和烟熏炉产出的食物也获得热状态。
- 生、熟和热食在 Tooltip 与物品描边中拥有清晰区分。

---

## 📜 签谱与烤串餐盘

### 签谱

将空白菜谱与生串放入合成栏，可以得到对应的签谱。签谱能够：

- 副手木棍右键签谱 或 直接用木棍右键墙上的签谱，自动从背包扣除材料并完成串签。
- 通过合成清除记录，重新变回空签谱。
- 固定串和玩家自制的秘制串都支持签谱记录。

### 烤串餐盘

主手持任意烤串潜行右键方块上表面，可以摆下一只餐盘。每个餐盘最多叠放 5 串：

- 空手右键按顺序拿回烤串。
- 手持餐盘右键可以直接食用饱食度最高的一串。
- 潜行右键可以重新摆盘。

---

## 🔪 高级厨具架

高级厨具架不只是装饰，而是一套面向厨房工作的快捷装备系统，功能类似于机械动力模组中的工具箱。

- 上方 5 个分类仓用于调料瓶、油壶等调味容器。
- 下方 4 个分类仓用于菜刀、锅铲、打火石等厨房工具。
- 长按 Caps Lock 打开两排式快捷界面，松开后切换到最终选中的物品。
- 支持一键回收背包中的匹配物品。

其他模组可以通过公开物品标签和 API，将自己的调味容器与厨房工具加入高级厨具架。

---

## 🌱 作物、采集与料理

这些料理与《森罗物语：厨房》的炒锅、汤锅、砧板和研磨系统联动，并支持对应的模糊烹饪配方。

### 新增自然资源

- **花椒树**：生成于森林，较为稀有，尝试获得树苗后在家种植。可采摘花椒；村庄战利品箱中也有概率找到花椒。
- **折耳根**：会出现在下界要塞战利品箱及下界疣种植区域，可以种植在耕地或灵魂沙上面。
- **油菜**：成熟后收获油菜籽，用于菜籽油生产；佩戴《森罗物语：厨房》的草帽打草也有机会获得。
- **洋葱**：像胡萝卜一样直接种植洋葱本体，是基础调料必备物；佩戴《森罗物语：厨房》的草帽打草也有机会获得。
- **红薯**：可种植、烤制、研磨并进一步制作苕皮；佩戴《森罗物语：厨房》的草帽打草也有机会获得。

### 新增料理

新增凉拌菜、炒菜、炖菜：
- 凉拌折耳根、糖拌番茄、花椒蜂蜜
- 折耳根炒肉、青椒炒鱿鱼须、红烧鸡翅
- 土豆炖牛肉、红苕稀饭
- 烤红薯、烤鸡翅及多种食材加工品



---


## 🔧 模组适配与前置

- **必须前置**：森罗物语：厨房（Kaleidoscope Cookery）


---

## 🌐 English Version

# 🔥 Kaleidoscope Grilling

**More than grilling over charcoal: give oils distinct flavors, keep meals genuinely hot, and bring a wider range of ingredients from the field into the kitchen.**

Kaleidoscope Grilling is a broad cooking expansion for Kaleidoscope Cookery built around four connected themes: **grilling, hot food, cooking oils, and new ingredients**. Grow canola, onions, sweet potatoes, and houttuynia; discover Sichuan pepper trees; process ingredients, press canola oil, blend chili oils, and bring real heat to foods prepared on grills, in cookware, and in furnaces. Grilling is its most visible feature, but the complete experience runs from cultivation and processing to serving a hot meal.

## 📋 Overview

- **Current Version**: 0.8.0
- **Core Loop**: Grow and gather → Process ingredients → Press oils and blend seasoning → Grill or cook → Serve hot
- **Key Features**: Hot-food and multi-oil systems, new crops and integrated dishes, dynamic custom skewers, 16 fixed skewer recipes, skewer plates, seasoning bottles, an advanced kitchen rack, animated interactions, special effects, and advancements
- **Designed For**: A broad expansion of Kaleidoscope Cookery, with grilling as its signature feature and deeper systems for heat, oils, crops, ingredient processing, pots, and pans

## 🎮 Main Features

### Hand-Assembled Skewers

Hold a stick in your off hand and ingredients in your main hand, then right-click to add them one by one. Follow a fixed recipe to create one of 16 standard skewers, or freely combine up to three ingredients to make a player-signed custom skewer.

Custom-skewer food values are calculated from the ingredients. Cooked nutrition is the sum of all edible ingredients minus the lowest nutrition value, multiplied by `0.6` when all ingredients differ or `0.5` when any ingredient repeats. The result is rounded down with a minimum of 1. Its saturation modifier uses the ingredient average with the same coefficient and is capped at `0.8`. Raw custom skewers receive 50% of both values.

When a custom skewer is eaten, every edible ingredient runs its original consumption behavior so potion effects, modded Buffs, and container remainders are inherited. Ingredient hunger and saturation gains are then restored, preventing them from being counted twice. Ingredient textures also provide the colors used to generate the skewer model, allowing a wide range of appearances. The `enableSkewerGuiCache` option defaults to `true`; it caches skewer icons at `64×64` resolution to retain clear image quality while reducing repeated model rendering and FPS pressure when many skewers are visible in a GUI. Disabling it switches completed skewer icons back to direct full-model rendering.

### Interactive Grilling

The grill holds three skewers. Light it with flint and steel, brush the skewers with oil, flip them four times, add seasoning, and take them out before they burn. The grill includes animated flipping, cooking stages, particles, sounds, contextual messages, and separate mysterious or ruined results for failed cooking.

### Custom Seasoning

Place an empty seasoning bottle in the world and fill it with ingredients. Each bottle holds 8 portions in total. Sichuan pepper, onion powder, and green chili powder form the required base, leaving up to five additional portions for effect ingredients. Shake the filled bottle for four seconds to create a seasoning blend with 16 uses.

Seasoning Buffs only activate while the food is still hot. Their base duration is three minutes:

| Ingredient | Effect | Stacking rule |
| --- | --- | --- |
| Redstone Dust | Speed | Level I with 1–3 portions; Level II with 4+ |
| Gunpowder | Strength | Level I with 1–3 portions; Level II with 4+ |
| Houttuynia Powder | Extends other seasoning effects | Doubles duration with 1–3 portions; quadruples it with 4+ |
| Totem Powder | Heavy Metal | Level II with 4+; prevents one death at 1 health, followed by a ten-minute Heavy Metal Poisoning cooldown |
| Dragon Egg Powder | Dragon Blood | Adds 6 maximum health, or 10 with 4+, and fills the newly added health |
| Sichuan Pepper | Numb | Requires at least 4 portions and lasts 45 seconds; Houttuynia Powder also extends it |

The three base ingredients are required to complete the blend. The required Sichuan pepper also counts toward the four-portion Numb threshold.

### Oil Production

Grow canola, grind it into powder, craft oil cakes, fill the oil press, and strike it with an anvil or stone. A nearby vat collects the resulting canola oil. Canola oil can then be upgraded into Chili Oil and Magma Chili Oil, each with distinct visuals and hot-food duration.

### Hot Food

Freshly cooked food remains hot for a limited time. Hot food can extend its built-in effects, while its saturation multiplier can be configured from 100% to 200%. Optional configuration can also make furnace and smoker outputs hot.

### Skewer Recipes and Plates

Record a raw skewer in a recipe item to create a wall-mountable skewer recipe. Use it from your hands or right-click the mounted recipe with a stick to consume ingredients from your inventory and assemble the skewer automatically. Skewers can also be arranged on plates in stacks of up to five, carried together, placed again, or eaten directly from the plate.

### Advanced Kitchen Rack

The Advanced Kitchen Rack has five seasoning slots and four tool slots. Hold Caps Lock to open its quick-selection overlay, swap kitchen equipment, and return matching items with one action. Stored contents and remembered slot types survive block pickup and replacement. Public tags and APIs allow other mods to register compatible tools and seasoning containers.

### Crops, Foods, and Effects

The mod adds Sichuan pepper trees, houttuynia, canola, onions, sweet potatoes, multiple processed ingredients, cold dishes, stir-fries, stews, porridge, and grilled foods. Special ingredients can cause unusual effects such as Numb, Heavy Metal, and Dragon Blood, complete with visual feedback and third-person animations.

## 🖥️ HUD and Compatibility

- Contextual HUD panels are available for the grill, oil press, vat, seasoning bottle, oil pot, and cooking pot.
- Use `/kg hud on` or `/kg hud off` to control the HUD. It is disabled by default.
- Optional Jade integration displays machine progress, stored fluids, skewer states, and recipe requirements.
- Configuration options cover furnace hot food, hot duration, hot-food saturation, and `enableSkewerGuiCache`, which is enabled by default.

## 🔧 Requirements

- **Required Dependency**: Kaleidoscope Cookery
- **Forge**: Minecraft 1.20.1, Forge 47.4.0+, Kaleidoscope Cookery 1.4.1+
- **NeoForge**: Minecraft 1.21.1, NeoForge 21.1+, Kaleidoscope Cookery 1.4.1+
- **Optional Integration**: Jade
- **Installation Side**: Required on both client and server

Compatibility APIs are available for custom skewer ingredients, oil-press tools, canola-oil containers, seasoning materials, and Advanced Kitchen Rack categories. See `COMPATIBILITY.md` for developer documentation.
