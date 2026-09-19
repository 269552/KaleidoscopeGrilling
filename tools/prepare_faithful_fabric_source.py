#!/usr/bin/env python3
"""Mechanical source migration applied in CI before compiling the faithful Fabric port.

Only syntax/API renames that are semantics-preserving are handled here. Loader behavior
(registries, events, networking, capabilities, fluids, rendering) is ported explicitly in Java.
"""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1] / "fabric-26.2" / "src" / "main" / "java"

for path in ROOT.rglob("*.java"):
    text = path.read_text(encoding="utf-8")
    original = text

    # Minecraft 26.2 renamed ResourceLocation to Identifier. The static factory names used
    # by the upstream source are retained by Identifier in 26.2.
    text = text.replace("net.minecraft.resources.ResourceLocation", "net.minecraft.resources.Identifier")
    text = re.sub(r"\bResourceLocation\b", "Identifier", text)

    if text != original:
        path.write_text(text, encoding="utf-8")
