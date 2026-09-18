# Kaleidoscope Grilling Fabric 26.2 port status

## Current state

- Branch: `fabric-26.2-port`
- Target: Minecraft 26.2 + Fabric Loader 0.19.3 + Java 25
- Fabric Loom: `net.fabricmc.fabric-loom` 1.17.2 (non-remapping Loom for unobfuscated Minecraft 26.2)
- Fabric API: 0.160.0+26.2
- Required dependency: Kaleidoscope Cookery Refabricated 1.4.1.5 for Minecraft 26.2
- CI: GitHub Actions reaches `:fabric-26.2:compileJava` successfully.

## First real compile result

The build system and Java 25 environment are working. The first source compile stops at 100 compiler errors because the 1.21.1 NeoForge sources are intentionally being compiled as a migration baseline.

The errors currently fall into two main groups:

1. NeoForge APIs that must be replaced with Fabric equivalents:
   - lifecycle/event bus (`FMLCommonSetupEvent`, `EventBusSubscriber`, `SubscribeEvent`)
   - client events (`ClientTickEvent`, menu/screens, colors, reload listeners, GUI layers)
   - item capabilities (`IItemHandler`)
   - fluids/capabilities (`FluidStack`, `FluidTank`, `IFluidHandler`)
   - entity/player events (`LivingEntityUseItemEvent`, `PlayerEvent`)

2. Minecraft 1.21.1 -> 26.2 API/name changes:
   - `ResourceLocation`
   - `InteractionResultHolder`
   - `UseAnim`
   - `GuiGraphics`
   - render/model APIs such as `RenderType`, `ItemProperties`, `ModelResourceLocation`

## Port plan

1. Replace the temporary full NeoForge source-set with a Fabric-native source tree.
2. Port common registries and the mod bootstrap first.
3. Port items/blocks/recipes and Cookery integration.
4. Port block entities, item storage and fluid storage using Fabric APIs.
5. Port networking and gameplay events.
6. Port client rendering, screens, HUD and sounds.
7. Re-enable optional compatibility modules after the core mod builds and starts.
8. Produce a test JAR through GitHub Actions, then fix runtime issues.

This file is updated as the port progresses.
