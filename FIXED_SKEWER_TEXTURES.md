# 固定烤串模型 → 贴图 清单

所有贴图位于 `assets/kaleidoscope_grilling/textures/item/fixed_skewers/`。
每个模型除下方列出的食物贴图外，都引用共用的木签贴图 `skewer_stick.png`。

## 模型命名规则（每种串一套）

| 模型 | 作用 | 贴图 |
|---|---|---|
| `{name}_skewer.json` | 生串完整 | `{name}_skewer_raw` |
| `{name}_skewer_cooked.json` | 熟串完整 | `{name}_skewer_cooked` |
| `{name}_skewer_burnt.json` | 焦串 | `{name}_skewer_burnt` |
| `{name}_skewer_stage_1/2/3.json` | 烤制中间阶段 1/2/3 | `{name}_skewer_stage_1/2/3` |
| `{name}_skewer_bite_1..N.json` | 熟串咬 N 口（几何体裁剪，贴图同 cooked） | `{name}_skewer_cooked` |
| `{name}_skewer_raw_bite_1..N.json` | 生串咬 N 口（贴图同 raw） | `{name}_skewer_raw` |
| `{name}_skewer_piece_N.json` | 食用动画中分离的食物块（贴图同 cooked） | `{name}_skewer_cooked` |
| `{name}_skewer_raw_piece_N.json` | 生串分离块（贴图同 raw） | `{name}_skewer_raw` |

咬痕/分离块**不需要独立贴图**，模型靠几何体（元素裁剪）实现，贴图复用 raw/cooked。

## 各串所需贴图文件

### beef（牛肉串）
- beef_skewer_raw.png
- beef_skewer_cooked.png
- beef_skewer_burnt.png
- beef_skewer_stage_1.png
- beef_skewer_stage_2.png
- beef_skewer_stage_3.png

### bun_slice（馒头片串）
- bun_slice_skewer_raw.png
- bun_slice_skewer_cooked.png
- bun_slice_skewer_burnt.png
- bun_slice_skewer_stage_1.png
- bun_slice_skewer_stage_2.png
- bun_slice_skewer_stage_3.png

### caterpillar（猪儿虫串）
- caterpillar_skewer_raw.png
- caterpillar_skewer_cooked.png
- caterpillar_skewer_burnt.png
- caterpillar_skewer_stage_1.png
- caterpillar_skewer_stage_2.png
- caterpillar_skewer_stage_3.png

### chicken_skin（鸡皮串）
- chicken_skin_skewer_raw.png
- chicken_skin_skewer_cooked.png
- chicken_skin_skewer_burnt.png
- chicken_skin_skewer_stage_1.png
- chicken_skin_skewer_stage_2.png
- chicken_skin_skewer_stage_3.png

### ender_pearl（末影珍珠串）
- ender_pearl_skewer_raw.png
- ender_pearl_skewer_cooked.png
- ender_pearl_skewer_burnt.png
- ender_pearl_skewer_stage_1.png
- ender_pearl_skewer_stage_2.png
- ender_pearl_skewer_stage_3.png

### fish（鱼串）
- fish_skewer_raw.png
- fish_skewer_cooked.png
- fish_skewer_burnt.png
- fish_skewer_stage_1.png
- fish_skewer_stage_2.png
- fish_skewer_stage_3.png

### fried_egg（煎蛋串）
- fried_egg_skewer_raw.png
- fried_egg_skewer_cooked.png
- fried_egg_skewer_burnt.png
- fried_egg_skewer_stage_1.png
- fried_egg_skewer_stage_2.png
- fried_egg_skewer_stage_3.png

### golden（黄金串）
- golden_skewer_raw.png
- golden_skewer_cooked.png
- golden_skewer_burnt.png
- golden_skewer_stage_1.png
- golden_skewer_stage_2.png
- golden_skewer_stage_3.png

### lamb（羊肉串）
- lamb_skewer_raw.png
- lamb_skewer_cooked.png
- lamb_skewer_burnt.png
- lamb_skewer_stage_1.png
- lamb_skewer_stage_2.png
- lamb_skewer_stage_3.png

### meat_and_bone（骨肉相连串）
- meat_and_bone_skewer_raw.png
- meat_and_bone_skewer_cooked.png
- meat_and_bone_skewer_burnt.png
- meat_and_bone_skewer_stage_1.png
- meat_and_bone_skewer_stage_2.png
- meat_and_bone_skewer_stage_3.png

### meatball（丸子串）
- meatball_skewer_raw.png
- meatball_skewer_cooked.png
- meatball_skewer_burnt.png
- meatball_skewer_stage_1.png
- meatball_skewer_stage_2.png
- meatball_skewer_stage_3.png

### mid_wing（中翅串）⚠️ 双贴图
中翅串模型同时引用牛肉串贴图（beef_*）与中翅贴图（mid_wing_*），修改时两者都要重画。
- mid_wing_skewer_raw.png
- mid_wing_skewer_cooked.png
- mid_wing_skewer_burnt.png
- mid_wing_skewer_stage_1.png
- mid_wing_skewer_stage_2.png
- mid_wing_skewer_stage_3.png
- （复用）beef_skewer_raw.png / beef_skewer_cooked.png / beef_skewer_burnt.png / beef_skewer_stage_1..3.png

### mushroom（蘑菇串）
- mushroom_skewer_raw.png
- mushroom_skewer_cooked.png
- mushroom_skewer_burnt.png
- mushroom_skewer_stage_1.png
- mushroom_skewer_stage_2.png
- mushroom_skewer_stage_3.png

### ordinary（“普通”串）⚠️ 单贴图
全部模型（含 bite）只用一个贴图，无 raw/cooked/burnt/stage 区分。
- ordinary_skewer.png

### pork_belly（五花肉串）
- pork_belly_skewer_raw.png
- pork_belly_skewer_cooked.png
- pork_belly_skewer_burnt.png
- pork_belly_skewer_stage_1.png
- pork_belly_skewer_stage_2.png
- pork_belly_skewer_stage_3.png

### potato_slice（土豆片串）⚠️ 双贴图、独立阶段
生串同时使用 `potato_slice_skewer_raw_1` 与 `potato_slice_skewer_raw_2`；烤制阶段为 `potato_slice_skewer_1_stage_N` 与 `potato_slice_skewer_2_stage_N`（每阶段两张）。cooked/burnt 阶段两张都指向同一贴图。
- potato_slice_skewer_raw_1.png
- potato_slice_skewer_raw_2.png
- potato_slice_skewer_cooked.png
- potato_slice_skewer_burnt.png
- potato_slice_skewer_1_stage_1.png / potato_slice_skewer_2_stage_1.png
- potato_slice_skewer_1_stage_2.png / potato_slice_skewer_2_stage_2.png
- potato_slice_skewer_1_stage_3.png / potato_slice_skewer_2_stage_3.png

### slime（黏液串）⚠️ 单贴图 + 帧动画
`slime_skewer_0..4.json` 五个模型引用同一贴图 `slime_skewer.png`（实体动画帧由代码切换），所有 bite 模型也复用该贴图。
- slime_skewer.png

### squid_tentacle（鱿鱼须串）
- squid_tentacle_skewer_raw.png
- squid_tentacle_skewer_cooked.png
- squid_tentacle_skewer_burnt.png
- squid_tentacle_skewer_stage_1.png
- squid_tentacle_skewer_stage_2.png
- squid_tentacle_skewer_stage_3.png

### sweet_potato_sheet（苕皮串）
- sweet_potato_sheet_skewer_raw.png
- sweet_potato_sheet_skewer_cooked.png
- sweet_potato_sheet_skewer_burnt.png
- sweet_potato_sheet_skewer_stage_1.png
- sweet_potato_sheet_skewer_stage_2.png
- sweet_potato_sheet_skewer_stage_3.png

## 共用贴图

- skewer_stick.png（木签，所有串共用）

## 说明

- 贴图尺寸统一为 16×16，食物区域集中在模型元素 UV 对应的网格（参考各模型 elements/faces UV 坐标）。
- 磁盘上存在但**无模型引用**的遗留文件（重画时可忽略）：
  - potato_slice_skewer_1_burnt.png、potato_slice_skewer_2_burnt.png（burnt 阶段实际复用单张 potato_slice_skewer_burnt.png）
- GUI 16×16 图标另位于 `textures/item/fixed_skewer_gui_16/`，与模型贴图相互独立。
