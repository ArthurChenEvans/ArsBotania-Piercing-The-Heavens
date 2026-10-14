"""Generate Divine textures by recolouring Ars Nouveau's armor.

  dyed cloth  -> pearl white with a faint opal sheen (keeps Ars's shading)
  gold trim   -> an animated rainbow shimmer (keeps the trim's shading)
  everything else (outlines, grey metal) is left as Ars drew it.

Cloth is found the same way analyze_ars_palette.py does: Ars ships each armor
texture in 16 dye colours, and any pixel that differs between them is cloth.

Divine wings are Botania's Flügel wing (read from the Botania jar) recoloured the same
way: pearl feathers with an animated rainbow edge.

Usage: python3 tools/divine_recolor.py <ars textures/item dir> <ars models/item dir> <mod resources dir> [botania jar]
"""
import colorsys
import json
import math
import io
import sys
import zipfile
from pathlib import Path

from PIL import Image

from analyze_ars_palette import ARMORS, DYES, classify, family, load_variants

FRAMES = 16
FRAME_TIME = 3          # ticks per frame: a full shimmer cycle every 2.4 s
RAINBOW_PERIOD = 24     # pixels per full hue cycle across the trim
CLASS_FOR_ARMOR = {"light_armor": "sorcerer", "medium_armor": "arcanist", "heavy_armor": "battlemage"}
SLOTS = ["hood", "robes", "leggings", "boots"]


def luminance(rgb):
    r, g, b = (c / 255 for c in rgb)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def normaliser(values):
    lo, hi = min(values, default=0), max(values, default=1)
    span = (hi - lo) or 1
    return lambda v: (v - lo) / span


def pearl(shade, x, y):
    """Pearl white: bright, nearly unsaturated, with a faint opal hue drifting across the cloth."""
    hue = ((x * 0.7 + y * 0.3) / 48.0) % 1.0
    lightness = 0.76 + 0.22 * shade
    r, g, b = colorsys.hls_to_rgb(hue, lightness, 0.10)
    return int(r * 255), int(g * 255), int(b * 255)


def rainbow(shade, x, y, frame):
    hue = ((x + y) / RAINBOW_PERIOD + frame / FRAMES) % 1.0
    lightness = 0.42 + 0.38 * shade
    r, g, b = colorsys.hls_to_rgb(hue, lightness, 0.85)
    return int(r * 255), int(g * 255), int(b * 255)


def write_animation(path: Path, frames):
    w, h = frames[0].size
    strip = Image.new("RGBA", (w, h * len(frames)))
    for i, f in enumerate(frames):
        strip.paste(f, (0, i * h))
    path.parent.mkdir(parents=True, exist_ok=True)
    strip.save(path)
    path.with_name(path.name + ".mcmeta").write_text(
        json.dumps({"animation": {"frametime": FRAME_TIME, "interpolate": True}}, indent=2) + "\n")


def armor_textures(item_dir: Path, out_dir: Path):
    for armor in ARMORS:
        variants = load_variants(item_dir, armor)
        labels = classify(variants)
        white = variants[DYES.index("white")].load()
        w, h = variants[0].size

        cloth_shade = normaliser([luminance(white[p][:3]) for p, l in labels.items() if l == "cloth"])
        gold = {p for p, l in labels.items() if l == "fixed" and family(white[p][:3]) == "gold/yellow"}
        gold_shade = normaliser([luminance(white[p][:3]) for p in gold])

        frames = []
        for frame in range(FRAMES):
            img = Image.new("RGBA", (w, h))
            out = img.load()
            for (x, y), label in labels.items():
                r, g, b, a = white[x, y]
                if label == "cloth":
                    out[x, y] = (*pearl(cloth_shade(luminance((r, g, b))), x, y), a)
                elif (x, y) in gold:
                    out[x, y] = (*rainbow(gold_shade(luminance((r, g, b))), x, y, frame), a)
                elif label == "fixed":
                    out[x, y] = (r, g, b, a)
            frames.append(img)
        write_animation(out_dir / "textures/armor" / f"divine_{armor}.png", frames)
        print(f"armor  {armor}: {len(gold)} trim px, {sum(l == 'cloth' for l in labels.values())} cloth px")


def recolour_icon_layer(src: Path, kind: str):
    """Ars icons are a greyscale 'tint' layer (the dyed cloth) plus a 'gold' layer."""
    img = Image.open(src).convert("RGBA")
    px = img.load()
    w, h = img.size
    opaque = [(x, y) for y in range(h) for x in range(w) if px[x, y][3] > 0]
    shade = normaliser([luminance(px[p][:3]) for p in opaque])
    frames = []
    for frame in range(FRAMES if kind == "gold" else 1):
        out_img = Image.new("RGBA", (w, h))
        out = out_img.load()
        for x, y in opaque:
            r, g, b, a = px[x, y]
            s = shade(luminance((r, g, b)))
            out[x, y] = (*(rainbow(s, x, y, frame) if kind == "gold" else pearl(s, x, y)), a)
        frames.append(out_img)
    return frames


def item_icons(item_dir: Path, models_dir: Path, out_dir: Path):
    for armor, cls in CLASS_FOR_ARMOR.items():
        for slot in SLOTS:
            ars_model = json.loads((models_dir / f"{cls}_{slot}.json").read_text())
            layers = {}
            for layer, tex in sorted(ars_model["textures"].items()):
                src = item_dir / (tex.split(":", 1)[1].removeprefix("item/") + ".png")
                kind = "gold" if tex.endswith("_gold") else "tint"
                name = f"divine/{cls}_{slot}_{'trim' if kind == 'gold' else 'base'}"
                frames = recolour_icon_layer(src, kind)
                target = out_dir / "textures/item" / f"{name}.png"
                if len(frames) > 1:
                    write_animation(target, frames)
                else:
                    target.parent.mkdir(parents=True, exist_ok=True)
                    frames[0].save(target)
                layers[layer] = f"arsbotania:item/{name}"
            model = {"parent": "minecraft:item/generated", "textures": layers}
            (out_dir / "models/item" / f"divine_{cls}_{slot}.json").write_text(json.dumps(model, indent=2) + "\n")
            print(f"icon   divine_{cls}_{slot}: {', '.join(layers)}")


def divine_wings(botania_jar: Path, out_dir: Path):
    """Flügel wing: feathers -> pearl, the outermost ring of feathers -> rainbow."""
    with zipfile.ZipFile(botania_jar) as jar:
        img = Image.open(io.BytesIO(jar.read("assets/botania/textures/item/tiara_wing_1.png"))).convert("RGBA")
    px = img.load()
    w, h = img.size
    opaque = {(x, y) for y in range(h) for x in range(w) if px[x, y][3] > 0}
    edge = {(x, y) for x, y in opaque
            if any((x + dx, y + dy) not in opaque for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))}
    shade = normaliser([luminance(px[p][:3]) for p in opaque])
    frames = []
    for frame in range(FRAMES):
        out_img = Image.new("RGBA", (w, h))
        out = out_img.load()
        for x, y in opaque:
            r, g, b, a = px[x, y]
            s = shade(luminance((r, g, b)))
            out[x, y] = (*(rainbow(s, x, y, frame) if (x, y) in edge else pearl(s, x, y)), a)
        frames.append(out_img)
    write_animation(out_dir / "textures/item/divine_wings.png", frames)
    model = {"parent": "minecraft:item/generated", "textures": {"layer0": "arsbotania:item/divine_wings"}}
    (out_dir / "models/item/divine_wings.json").write_text(json.dumps(model, indent=2) + "\n")
    print(f"wings  divine_wings: {len(edge)} edge px, {len(opaque) - len(edge)} feather px")


def main(item_dir: Path, models_dir: Path, out_dir: Path, botania_jar: Path | None):
    out_dir = out_dir / "assets/arsbotania"
    armor_textures(item_dir, out_dir)
    item_icons(item_dir, models_dir, out_dir)
    if botania_jar:
        divine_wings(botania_jar, out_dir)


if __name__ == "__main__":
    main(Path(sys.argv[1]), Path(sys.argv[2]), Path(sys.argv[3]), Path(sys.argv[4]) if len(sys.argv) > 4 else None)
