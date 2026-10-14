"""Read Ars Nouveau's armor textures and report which pixels are dyed cloth and
what colours make up the fixed parts (trim, metal, runes, outlines).

Ars ships each armor texture in all 16 dye colours. A pixel that changes between
those variants is dyed cloth; a pixel that never changes is a fixed detail.

Usage: python3 tools/analyze_ars_palette.py <ars_nouveau textures/item dir>
"""
import colorsys
import sys
from collections import Counter
from pathlib import Path

from PIL import Image

DYES = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]
ARMORS = ["light_armor", "medium_armor", "heavy_armor"]


def load_variants(root: Path, armor: str):
    return [Image.open(root / armor / f"{armor}_{dye}.png").convert("RGBA") for dye in DYES]


def classify(variants):
    """Returns per-pixel labels: 'empty', 'cloth' or 'fixed'."""
    w, h = variants[0].size
    px = [v.load() for v in variants]
    labels = {}
    for y in range(h):
        for x in range(w):
            colours = {p[x, y] for p in px}
            if all(c[3] == 0 for c in colours):
                labels[x, y] = "empty"
            elif len(colours) > 1:
                labels[x, y] = "cloth"
            else:
                labels[x, y] = "fixed"
    return labels


def family(rgb):
    """Coarse colour family for a fixed pixel."""
    h, l, s = colorsys.rgb_to_hls(*(c / 255 for c in rgb))
    if l < 0.18:
        return "outline/dark"
    if s < 0.18:
        return "grey/silver"
    deg = h * 360
    if 20 <= deg < 65:
        return "gold/yellow"
    if 65 <= deg < 170:
        return "green"
    if 170 <= deg < 260:
        return "cyan/blue (runes?)"
    if 260 <= deg < 330:
        return "purple/magenta"
    return "red/brown"


def main(root: Path):
    for armor in ARMORS:
        variants = load_variants(root, armor)
        labels = classify(variants)
        purple = variants[DYES.index("purple")].load()
        counts = Counter(labels.values())
        print(f"\n== {armor} {variants[0].size}  cloth={counts['cloth']} fixed={counts['fixed']} empty={counts['empty']}")
        fam = Counter()
        top = Counter()
        for pos, lab in labels.items():
            if lab == "fixed":
                rgb = purple[pos][:3]
                fam[family(rgb)] += 1
                top[rgb] += 1
        for name, n in fam.most_common():
            print(f"   {name:22s} {n:5d}")
        print("   most common fixed colours:", ", ".join(f"#{r:02x}{g:02x}{b:02x}x{n}" for (r, g, b), n in top.most_common(8)))


if __name__ == "__main__":
    main(Path(sys.argv[1]))
