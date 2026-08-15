# 固定烤串模型重构方案（父模型 + 子文件夹）

## 一、目标

把 `models/item/fixed_skewers/` 下平铺的 200+ 个模型文件：
1. **按串分子文件夹**：`models/item/fixed_skewers/{串名}/`（如 `beef/`、`bun_slice/`）
2. **用父模型消除重复几何**：raw/cooked/burnt/stage_1..3 共享一个几何父模型；bite/raw_bite 共享"档位几何父模型"
3. **同步更新全部引用**（物品模型 JSON + Java 代码）

## 二、当前文件清单（18 主串 + slime）

主串（`X_skewer.json` 存在）：beef, bun_slice, caterpillar, chicken_skin, ender_pearl, fish, fried_egg, golden, lamb, meat_and_bone, meatball, mid_wing, mushroom, ordinary, pork_belly, potato_slice, squid_tentacle, sweet_potato_sheet

每个主串的文件族（数量因 bite 档位而异，10~16 个）：
- `X_skewer.json`（生串主模型）
- `X_skewer_cooked.json` / `X_skewer_burnt.json` / `X_skewer_stage_1..3.json`（状态）
- `X_skewer_bite_1..N.json` + `X_skewer_raw_bite_1..N.json`（咬合，N=2~4）
- `X_skewer_piece_K.json` + `X_skewer_raw_piece_K.json`（食用动画分离块，K=1 或 3）

**slime 特殊**（`slime_skewer_0..4.json` + `slime_skewer_{N}_bite_1/2` + `slime_skewer_bite_1/2` + `slime_skewer_raw_bite_1/2`）：5 帧动画模型，全部共用一张贴图 `slime_skewer.png`，无 raw/cooked/burnt/stage 区分。可整组移入 `slime/` 子文件夹，是否做父模型可选（建议至少移文件夹，父模型收益小）。

## 三、目标结构

```
models/item/fixed_skewers/
  beef/
    beef_skewer_geo.json            ← 几何父（主模型几何+display+gui_light，无具体食物贴图或带默认）
    beef_skewer.json                ← {"parent":".../beef/beef_skewer_geo","textures":{"food":"...beef_skewer_raw"}}
    beef_skewer_cooked.json         ← parent geo + food=...cooked
    beef_skewer_burnt.json          ← parent geo + food=...burnt
    beef_skewer_stage_1.json        ← parent geo + food=...stage_1
    beef_skewer_stage_2.json
    beef_skewer_stage_3.json
    beef_skewer_bite_1_geo.json     ← 档位几何父（主几何去掉 food3 元素）
    beef_skewer_bite_1.json         ← parent bite_1_geo + food=...cooked
    beef_skewer_raw_bite_1.json     ← parent bite_1_geo + food=...raw
    beef_skewer_bite_2_geo.json     ← 去掉 food3+food2_upper
    beef_skewer_bite_2.json
    beef_skewer_raw_bite_2.json
    beef_skewer_bite_3_geo.json     ← 去掉 food3+food2_upper+food1
    beef_skewer_bite_3.json
    beef_skewer_raw_bite_3.json
    beef_skewer_bite_4_geo.json     ← 只剩 wood
    beef_skewer_bite_4.json
    beef_skewer_raw_bite_4.json
    beef_skewer_piece_3_geo.json    ← 只剩 food1（piece 几何）
    beef_skewer_piece_3.json        ← parent piece_3_geo + food=...cooked
    beef_skewer_raw_piece_3.json    ← parent piece_3_geo + food=...raw
  bun_slice/ ...（同类）
  ...
  slime/（整组平移）
```

**父模型继承规则**：
- 子模型写 `"parent": "kaleidoscope_grilling:item/fixed_skewers/{串名}/{几何父名}"`
- 子模型 `textures` 与父模型**合并**：只写要覆盖的键（如 `food`），`stick`、`particle` 继承
- 几何父模型必须保留 `particle` 键（原模型是 `"particle": "#food"` 或具体贴图）——注意**不能用 `#food` 引用自身不存在的键**，父模型要么写具体贴图值，要么确保键存在
- `display`、`gui_light`、`ambientocclusion` 都放几何父模型

**关键验证（已确认）**：同一串所有状态模型的 `display` 与 `gui_light` 完全一致（beef 实测 displayHash 相同、gui_light=front），piece 的 display 也与主模型一致——所以几何父可以放心共享。

## 四、各串纹理键模式（生成时必须按此映射）

| 串 | stick 键 | 食物键 | 备注 |
|---|---|---|---|
| beef | `stick` | `food` | |
| bun_slice | `3` | `8` | |
| caterpillar | `stick` | `food` | |
| chicken_skin | `stick` | `food` | |
| ender_pearl | `3` | `5` | |
| fish | `3` | `1` | |
| fried_egg | `3` | `4` | |
| golden | `stick` | `food` | |
| lamb | `stick` | `food` | |
| meat_and_bone | `3` | `5` | |
| meatball | `3` | `10` | |
| mid_wing | `3` | `1`(牛肉贴图), `4`(中翅贴图) | 双食物键：1=beef_*, 4=mid_wing_* |
| mushroom | `0` | `3` | stick 键是 `0`！ |
| ordinary | `stick` | `food` | 单贴图 `ordinary_skewer`（无 raw/cooked 后缀），全部状态同一贴图 |
| pork_belly | `3` | `5` | |
| potato_slice | `stick` | `food_1`, `food_2` | 双贴图特殊（见下） |
| squid_tentacle | `3` | `1` | |
| sweet_potato_sheet | `stick` | `food` | |

**potato_slice 特殊**（双贴图）：
- 生串：`food_1=potato_slice_skewer_raw_1`, `food_2=potato_slice_skewer_raw_2`
- cooked/burnt：两键都指向 `potato_slice_skewer_cooked` / `..._burnt`
- stage_N：`food_1=potato_slice_skewer_1_stage_N`, `food_2=potato_slice_skewer_2_stage_N`
- bite：两键都指向 cooked

**mid_wing 特殊**（双食物键）：bite/stage 时 `1` 键跟随 beef 贴图（beef_skewer_cooked/stage_N/raw），`4` 键跟随 mid_wing 贴图（mid_wing_skewer_cooked/stage_N/raw）。

**ordinary 特殊**：所有状态（含 bite）都用 `ordinary_skewer` 一张贴图，无后缀。

## 五、bite 档位几何（已实测每串 bite 的元素删减）

生成规则：`bite_N` 几何 = 主模型几何去掉 bite_1..N 依次消失的元素。实测结果：

- beef: bite_1 去 food3, bite_2 去 food2_upper, bite_3 去 food1, bite_4 只剩 wood
- bun_slice: bite_1 去一个食物, bite_2 只剩 wood
- caterpillar: bite_1 去 food4, bite_2 只剩 wood
- chicken_skin: bite_1..4 依次去 food1（5 个同名 food1 元素逐个删）
- ender_pearl: bite_1 去 food3(第一个), bite_2 去 food2, bite_3 只剩 wood
- fish: bite_1 去一个, bite_2 只剩 wood
- fried_egg: bite_1 去部分, bite_2 只剩 wood
- golden: bite_1 去 carrot 系列, bite_2 去 totem_body, bite_3 只剩 wood
- lamb: bite_1 去 fat_upper+lamb_upper, bite_2 去 oil_center, bite_3 只剩 wood
- meat_and_bone: bite_1 去 food3_model3(第一个), bite_2 去 food1_model1, bite_3 只剩 wood
- meatball: bite_1..3 依次去
- mid_wing: bite_1 去部分, bite_2 去, bite_3 只剩 wood
- mushroom: bite_1 去 1-1(等), bite_2 去 2-2, bite_3 只剩 wood
- ordinary: bite_1 去 puffer 系列, bite_2 去 spider_eye, bite_3 只剩 wood
- pork_belly: bite_1..4 依次去
- potato_slice: bite_1 去 2 个 food1, bite_2 去 2 个, bite_3 只剩 wood
- squid_tentacle: bite_1 去 2 个 food1, bite_2 去 1 个, bite_3 只剩 wood
- sweet_potato_sheet: bite_1 去 2 个 food1, bite_2 只剩 wood

**最稳妥做法**：不手工推导，直接**读取每个 bite_N.json 的 elements 数组，原样作为 bite_N_geo 的 elements**——bite 模型本身就是"删好元素"的现成几何。

## 六、piece 几何

`X_skewer_piece_K.json` 的 elements 原样作为 `X_skewer_piece_K_geo.json`。piece 几何父只被 piece 状态文件引用。

## 七、引用同步清单（必须全部改）

### 1. `models/item/` 下的物品模型（37 个文件）
- `raw_*.json`（19 个）与 `grilled_*.json`（18 个）
- `ordinary_skewer.json`（1 个）
- 这些文件的 `parent` 和 `overrides[].model` 路径从 `kaleidoscope_grilling:item/fixed_skewers/X_skewer*` 改为 `kaleidoscope_grilling:item/fixed_skewers/X/X_skewer*`

### 2. `models/item/failure_skewers/`（32 个）
- 引用 `fixed_skewers/skewer_stick`（贴图，**不改**，贴图不动）
- 引用 `fixed_skewers/{串名}` 作为 parent（如 `dark_raw_beef_skewer.json` 的 `"parent":"kaleidoscope_grilling:item/fixed_skewers/beef_skewer"`）→ 改为 `fixed_skewers/beef/beef_skewer`
- 检查每个文件：凡 parent 或 model 指向 fixed_skewers 模型的都要加 `/串名/` 段

### 3. Java（Forge + NeoForge 各一份）
- `client/ClientSetup.java` `registerAdditionalModels`：模型注册路径 `"item/fixed_skewers/" + base + "_piece_1/3"` 等 → 加 `/base/` 段
  - 注意 mid_wing 的 base 是 `mid_wing_skewer` 等带 `_skewer` 后缀的串名，生成新路径时 `fixed_skewers/{base}/{base}_piece_N`
- `skewer/SkewerEatingPiece.java` `fixedModel`：运行时拼接 `"item/fixed_skewers/" + base + suffix + "_piece_" + group` → 改为 `"item/fixed_skewers/" + base + "/" + base + suffix + "_piece_" + group`
- 另外 `ClientSetup` 里 `ender_pearl_bite_piece`（特殊模型，位于 fixed_skewers 根）→ 需要决定放哪：建议 `ender_pearl/ender_pearl_bite_piece` 或保持根目录（保持根目录则路径不变）

### 4. 其他 Java 引用（需全局搜索确认）
- grep `fixed_skewers` 在 forge/neoforge 两个 java 目录
- 之前查到 forge 12 处、neoforge 类似，全部是 ClientSetup + SkewerEatingPiece

## 八、贴图不动

`textures/item/fixed_skewers/*.png` 全部保持原位（贴图路径不含模型子文件夹，纹理键值 `kaleidoscope_grilling:item/fixed_skewers/beef_skewer_raw` 不变）。

## 九、执行步骤

1. **生成新结构到临时目录**（如 `build/skewer_refactor/`）：
   - 对每个主串：读主模型 → 生成 `{串}_skewer_geo.json`（含 ambientocclusion/textures.particle/elements/gui_light/display）
   - 生成 6 个状态小文件（raw/cooked/burnt/stage_1..3）：`parent` geo + 按纹理键模式覆写食物贴图
   - 读每个 bite_N → 生成 `{串}_skewer_bite_N_geo.json` + `bite_N.json`（cooked）+ `raw_bite_N.json`（raw）
   - 读每个 piece_K → 生成 `piece_K_geo.json` + `piece_K.json` + `raw_piece_K.json`
   - slime 整组复制到 `slime/`
2. **校验**：抽查 beef 的生成文件，确认 parent 链存在、JSON 合法
3. **替换**：删掉 `fixed_skewers/` 下旧平铺文件，放入新子文件夹结构
4. **改引用**：物品模型 37 个 + failure_skewers 32 个 + Java 2 处（forge/neoforge 各 2 个文件）
5. **编译**：`gradlew compileJava`（两个加载器）
6. **启动 NeoForge 进游戏验证**：生串、熟串、烤制中、咬 1/2/3 口、食用动画分离块、普通串/黏液串帧动画

## 十、风险与注意

- **纹理键名不能猜**：必须从每个主模型读取（stick 键可能是 `3`/`0`，食物键可能是数字或 `food_1/food_2`）
- **particle 键**：几何父模型里 particle 用原值；若原值是 `#food` 且父模型 textures 里没有 `food` 键会解析失败 → 建议几何父保留主模型完整 textures（含默认 raw 贴图），子模型只覆盖想变的键
- **bite 几何**：直接复制 bite_N.json 的 elements，不要推导
- **mid_wing/potato_slice/ordinary** 三个特殊串单独处理
- **slime**：只移文件夹，父模型可选
- **ender_pearl_bite_piece**：决定放根目录还是 ender_pearl/ 子目录，并同步 Java 引用
- 改完后**先编译再进游戏**，重点看 bite 咬合顺序和 piece 分离块是否正常
