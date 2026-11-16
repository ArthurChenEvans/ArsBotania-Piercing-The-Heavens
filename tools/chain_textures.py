"""Textures for the crafting-chain items.

  virtue runes          Botania's sin runes, recoloured pearl and gold (same rune shapes)
  rune of life & death  Botania's mana rune, split black and white
  divine ichor          Botania's mana bottle, liquid turned to an animated rainbow
  yin-yang, space component, time component, spacetime fabric: drawn here

Usage: python3 tools/chain_textures.py <botania jar> <mod resources dir>
"""
import colorsys
import io
import json
import math
import sys
import zipfile
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).parent))
from divine_recolor import FRAMES, gold, luminance, normaliser, pearl, rainbow, write_animation  # noqa: E402

SINS = ["lust", "gluttony", "greed", "sloth", "wrath", "envy", "pride"]
VIRTUES = ["chastity", "temperance", "charity", "diligence", "patience", "kindness", "humility"]


def botania(jar, name):
    return Image.open(io.BytesIO(jar.read(f"assets/botania/textures/item/{name}.png"))).convert("RGBA")


def opaque(img):
    px = img.load()
    return [(x, y) for y in range(img.size[1]) for x in range(img.size[0]) if px[x, y][3] > 0]


def virtue(jar, sin):
    src = botania(jar, f"rune_of_{sin}")
    px = src.load()
    pts = opaque(src)
    shade = normaliser([luminance(px[p][:3]) for p in pts])
    out = Image.new("RGBA", src.size)
    o = out.load()
    for x, y in pts:
        s = shade(luminance(px[x, y][:3]))
        o[x, y] = (*(gold(s) if s < 0.45 else pearl(s, x, y)), px[x, y][3])
    return out


def life_and_death(jar):
    src = botania(jar, "rune_of_mana")
    px = src.load()
    pts = opaque(src)
    shade = normaliser([luminance(px[p][:3]) for p in pts])
    out = Image.new("RGBA", src.size)
    o = out.load()
    for x, y in pts:
        s = shade(luminance(px[x, y][:3]))
        v = int(255 * (0.75 + 0.25 * s)) if x >= 8 else int(255 * (0.05 + 0.25 * s))
        o[x, y] = (v, v, v, px[x, y][3])
    return out


def yin_yang():
    img = Image.new("RGBA", (16, 16))
    o = img.load()
    c, r = 7.5, 6.6
    for y in range(16):
        for x in range(16):
            dx, dy = x - c, y - c
            d = math.hypot(dx, dy)
            if d > r + 0.6:
                continue
            if d > r - 0.4:
                o[x, y] = (40, 30, 60, 255)  # outline
                continue
            upper = math.hypot(dx, dy + r / 2)
            lower = math.hypot(dx, dy - r / 2)
            white = dx > 0
            if upper < r / 2:
                white = True
            if lower < r / 2:
                white = False
            if upper < r / 6:
                white = False
            if lower < r / 6:
                white = True
            o[x, y] = (245, 242, 235, 255) if white else (25, 22, 30, 255)
    return img


def space_component():
    img = Image.new("RGBA", (16, 16))
    o = img.load()
    stars = {(5, 5), (10, 4), (8, 9), (4, 10), (11, 11), (7, 3), (12, 7)}
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d > 7:
                continue
            t = d / 7
            r, g, b = colorsys.hls_to_rgb(0.72 - 0.08 * t, 0.12 + 0.25 * (1 - t), 0.7)
            col = (int(r * 255), int(g * 255), int(b * 255))
            if (x, y) in stars:
                col = (240, 240, 255)
            if d > 6.2:
                col = (120, 90, 200)
            o[x, y] = (*col, 255)
    return img


def time_component():
    img = Image.new("RGBA", (16, 16))
    o = img.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 8)
            if d <= 6.8:
                ring = d > 5.6
                o[x, y] = (*gold(0.85 if ring else 0.2), 255) if ring else (250, 244, 225, 255)
    for i in range(1, 5):  # minute hand up
        o[8, 8 - i] = (40, 30, 30, 255)
    for i in range(1, 4):  # hour hand right
        o[8 + i, 8] = (40, 30, 30, 255)
    o[7, 0] = o[8, 0] = (*gold(0.6), 255)  # crown
    o[7, 1] = o[8, 1] = (*gold(0.9), 255)
    return img


def spacetime_fabric():
    img = Image.new("RGBA", (16, 16))
    o = img.load()
    stars = {(3, 4), (11, 3), (7, 8), (13, 10), (4, 12), (9, 13)}
    for y in range(2, 14):
        for x in range(1, 15):
            wave = math.sin((x + y * 0.6) * 0.8) * 0.5 + 0.5
            h = 0.62 + 0.12 * wave
            r, g, b = colorsys.hls_to_rgb(h, 0.22 + 0.18 * wave, 0.75)
            col = (int(r * 255), int(g * 255), int(b * 255))
            if (x, y) in stars:
                col = (255, 250, 230)
            if y in (2, 13) or x in (1, 14):
                col = tuple(int(c * 0.6) for c in col)
            o[x, y] = (*col, 255)
    return img


def divine_ichor(jar):
    src = botania(jar, "mana_in_a_bottle")
    px = src.load()
    pts = opaque(src)
    hls = {p: colorsys.rgb_to_hls(*(c / 255 for c in px[p][:3])) for p in pts}
    liquid = [p for p in pts if 0.45 <= hls[p][0] <= 0.75 and hls[p][2] > 0.35]
    shade = normaliser([luminance(px[p][:3]) for p in liquid])
    frames = []
    for f in range(FRAMES):
        out = src.copy()
        o = out.load()
        for x, y in liquid:
            o[x, y] = (*rainbow(0.35 + 0.65 * shade(luminance(px[x, y][:3])), x, y, f), px[x, y][3])
        frames.append(out)
    return frames


def save(out_dir, name, img):
    path = out_dir / "textures/item" / f"{name}.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    if isinstance(img, list):
        write_animation(path, img)
    else:
        img.save(path)
    model = {"parent": "minecraft:item/generated", "textures": {"layer0": f"arsbotania:item/{name}"}}
    (out_dir / "models/item" / f"{name}.json").write_text(json.dumps(model, indent=2) + "\n")


def main(jar_path, resources):
    out = Path(resources) / "assets/arsbotania"
    with zipfile.ZipFile(jar_path) as jar:
        for sin, v in zip(SINS, VIRTUES):
            save(out, f"rune_of_{v}", virtue(jar, sin))
        save(out, "rune_of_life_and_death", life_and_death(jar))
        save(out, "divine_ichor", divine_ichor(jar))
    save(out, "yin_yang", yin_yang())
    save(out, "space_component", space_component())
    save(out, "time_component", time_component())
    save(out, "spacetime_fabric", spacetime_fabric())
    print("chain textures written")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
