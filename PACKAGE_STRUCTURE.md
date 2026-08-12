# Package structure

New implementation classes must be placed in the package that owns their behavior. Do not add
ordinary classes directly to `cn.breezeth.kaleidoscope_grilling`.

| Package | Responsibility |
| --- | --- |
| `bootstrap` | Common initialization and event wiring |
| `registry` | Blocks, items, effects, menus, fluids, sounds, and other registries |
| `grill` | Grill blocks, block entities, rendering, and interaction logic |
| `skewer` | Skewer items, recipes, placement, rendering, and eating behavior |
| `seasoning` | Seasoning bottles, seasoning data, effects, and interaction logic |
| `oil` | Oil fluids, oil pots, the oil press, the big vat, and related rendering |
| `rack` | Advanced kitchen rack storage, menus, screens, and shortcuts |
| `food` | Food state, tooltips, heat handling, and food item implementations |
| `world` | Crops, pepper trees, loot hooks, and world-generation behavior |
| `effect` | Runtime effect handlers and custom effect implementations |
| `client` | Client-only setup, HUDs, sounds, and presentation logic |
| `network` | Network registration and payload handling |
| `data` | Reloadable grilling data and parsers |
| `jei` | JEI plugins, categories, and display adapters |
| `compat` | Optional-mod integrations, grouped by mod |
| `mixin` | Mixin classes and accessors |

The root package is reserved for `KaleidoscopeGrilling` and published compatibility APIs whose
class paths must remain stable for third-party mods. Both builds run `verifyPackageStructure`
before Java compilation and fail with the unexpected file names when this rule is violated.
