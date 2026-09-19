#!/usr/bin/env python3
"""Deterministic source migration applied in CI for the faithful Fabric port.

This script is deliberately limited to mechanical changes whose behavior is known:
Minecraft namespace renames and adapting the upstream supplier-style item table to a
Fabric registrar that assigns 26.2 ResourceKeys before Item construction. Gameplay
logic, events, networking, block entities, fluids and rendering are ported explicitly
in Java source rather than replaced with preview stand-ins.
"""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1] / "fabric-26.2" / "src" / "main" / "java"

for path in ROOT.rglob("*.java"):
    text = path.read_text(encoding="utf-8")
    original = text

    # Minecraft 26.2 renamed ResourceLocation to Identifier.
    text = text.replace("net.minecraft.resources.ResourceLocation", "net.minecraft.resources.Identifier")
    text = re.sub(r"\bResourceLocation\b", "Identifier", text)

    # The original item registry is a large, declarative DeferredRegister table. Keep every
    # original item factory and ordering intact, but route registration through our Fabric
    # registrar. Its ThreadLocal key lets each original factory build keyed Item.Properties,
    # which Minecraft 26.2 requires before Item construction.
    if path.as_posix().endswith("/registry/ModItems.java"):
        text = text.replace(
            "import net.neoforged.neoforge.registries.DeferredHolder;\n",
            "import cn.breezeth.kaleidoscope_grilling.fabric.registry.RegistryRef;\n"
        )
        text = text.replace(
            "import net.neoforged.neoforge.registries.DeferredRegister;\n",
            "import cn.breezeth.kaleidoscope_grilling.fabric.registry.FabricItemRegistrar;\n"
        )
        text = text.replace("DeferredRegister<Item>", "FabricItemRegistrar")
        text = text.replace("DeferredHolder<Item, Item>", "RegistryRef<Item>")
        text = re.sub(
            r"DeferredRegister\.create\(BuiltInRegistries\.ITEM,\s*KaleidoscopeGrilling\.MOD_ID\)",
            "new FabricItemRegistrar(KaleidoscopeGrilling.MOD_ID)",
            text,
        )
        text = text.replace("new Item.Properties()", "ITEMS.properties()")

    if text != original:
        path.write_text(text, encoding="utf-8")
