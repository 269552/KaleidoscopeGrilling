"""Generate intermediate cooking textures from raw and cooked skewer textures."""

from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image


STAGES = (
    ("stage_1", 0.28, (184, 78, 42), 0.05),
    ("stage_2", 0.56, (174, 67, 30), 0.08),
    ("stage_3", 0.82, (154, 53, 26), 0.06),
)


def mix_channel(raw: int, cooked: int, amount: float) -> int:
    return round(raw + (cooked - raw) * amount)


def generate_stage(
    raw: Image.Image,
    cooked: Image.Image,
    amount: float,
    warmth: tuple[int, int, int],
    warmth_amount: float,
) -> Image.Image:
    output = Image.new("RGBA", raw.size)
    pixels = []
    for raw_pixel, cooked_pixel in zip(raw.getdata(), cooked.getdata()):
        alpha = mix_channel(raw_pixel[3], cooked_pixel[3], amount)
        if alpha == 0:
            pixels.append((0, 0, 0, 0))
            continue

        color = [mix_channel(raw_pixel[i], cooked_pixel[i], amount) for i in range(3)]
        color = [
            round(channel + (warmth[i] - channel) * warmth_amount)
            for i, channel in enumerate(color)
        ]
        pixels.append((*color, alpha))

    output.putdata(pixels)
    return output


def generate_burnt(cooked: Image.Image) -> Image.Image:
    output = Image.new("RGBA", cooked.size)
    pixels = []
    burnt_tint = (43, 23, 16)
    for red, green, blue, alpha in cooked.getdata():
        if alpha == 0:
            pixels.append((0, 0, 0, 0))
            continue
        pixels.append(
            (
                round(red * 0.48 + burnt_tint[0] * 0.52),
                round(green * 0.48 + burnt_tint[1] * 0.52),
                round(blue * 0.48 + burnt_tint[2] * 0.52),
                alpha,
            )
        )
    output.putdata(pixels)
    return output


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("raw", type=Path)
    parser.add_argument("cooked", type=Path)
    parser.add_argument("output_dir", type=Path)
    parser.add_argument("--prefix", default="beef_skewer")
    args = parser.parse_args()

    raw = Image.open(args.raw).convert("RGBA")
    cooked = Image.open(args.cooked).convert("RGBA")
    if raw.size != cooked.size:
        raise ValueError(f"Texture sizes differ: raw={raw.size}, cooked={cooked.size}")

    args.output_dir.mkdir(parents=True, exist_ok=True)
    for suffix, amount, warmth, warmth_amount in STAGES:
        stage = generate_stage(raw, cooked, amount, warmth, warmth_amount)
        stage.save(args.output_dir / f"{args.prefix}_{suffix}.png")

    generate_burnt(cooked).save(args.output_dir / f"{args.prefix}_burnt.png")


if __name__ == "__main__":
    main()
