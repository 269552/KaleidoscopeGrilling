#!/usr/bin/env python3
from pathlib import Path
import re
import shutil
import sys

src = Path(sys.argv[1])
dst = Path(sys.argv[2])
if dst.exists():
    shutil.rmtree(dst)
dst.mkdir(parents=True, exist_ok=True)

SKIP = {
    "cn/breezeth/kaleidoscope_grilling/KaleidoscopeGrilling.java",
    "cn/breezeth/kaleidoscope_grilling/bootstrap/CommonSetup.java",
}

REPLACEMENTS = {
    "import net.neoforged.neoforge.registries.DeferredHolder;":
        "import cn.breezeth.kaleidoscope_grilling.fabric.registry.DeferredHolder;",
    "import net.neoforged.neoforge.registries.DeferredRegister;":
        "import cn.breezeth.kaleidoscope_grilling.fabric.registry.DeferredRegister;",
    "import net.minecraft.resources.ResourceLocation;":
        "import net.minecraft.resources.Identifier;",
    "ResourceLocation": "Identifier",
}

for file in src.rglob("*.java"):
    rel = file.relative_to(src).as_posix()
    if rel in SKIP:
        continue
    text = file.read_text(encoding="utf-8")
    for old, new in REPLACEMENTS.items():
        text = text.replace(old, new)

    # Loader-side annotations have no meaning on Fabric. Their methods remain and
    # are wired to Fabric events incrementally during the port.
    text = re.sub(r"^import net\.neoforged\.api\.distmarker\..*;\s*$", "", text, flags=re.M)
    text = re.sub(r"^import net\.neoforged\.bus\.api\.SubscribeEvent;\s*$", "", text, flags=re.M)
    text = re.sub(r"^import net\.neoforged\.fml\.common\.EventBusSubscriber;\s*$", "", text, flags=re.M)
    text = re.sub(r"^\s*@OnlyIn\([^\n]*\)\s*$", "", text, flags=re.M)
    text = re.sub(r"^\s*@SubscribeEvent\s*$", "", text, flags=re.M)

    # Remove single- or multi-line @EventBusSubscriber(...) annotations by balancing
    # parentheses line-by-line, without touching the class declaration that follows.
    lines = text.splitlines(True)
    out = []
    skipping = False
    depth = 0
    for line in lines:
        if not skipping and "@EventBusSubscriber" in line:
            skipping = True
            depth = line.count("(") - line.count(")")
            if depth <= 0:
                skipping = False
            continue
        if skipping:
            depth += line.count("(") - line.count(")")
            if depth <= 0:
                skipping = False
            continue
        out.append(line)
    text = "".join(out)

    target = dst / rel
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(text, encoding="utf-8")

print(f"Generated Fabric port sources: {sum(1 for _ in dst.rglob('*.java'))} files")
