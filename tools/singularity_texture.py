"""Generate the Singularity's animated black-hole item texture.

A black event horizon ringed by a bright photon ring, inside a tilted accretion disk
whose spiral arms rotate frame to frame. Hot (white/orange) near the hole, cooling to
violet at the edge; the approaching side is brighter (Doppler beaming), and the far side
of the disk is lensed up over the top of the hole.

Usage: python3 tools/singularity_texture.py <mod resources dir>
"""
import json
import math
import sys
from pathlib import Path

from PIL import Image

SIZE = 32
FRAMES = 32
FRAME_TIME = 2           # ticks per frame: one full disk rotation every 3.2 s
HORIZON = 5.0            # event horizon radius, px
PHOTON_RING = 1.2        # photon ring thickness, px
DISK_IN, DISK_OUT = 5.5, 15.5
TILT = 0.42              # how flattened the disk looks (1 = face on)
ARMS = 3

RAMP = [  # (temperature 0..1, colour)
    (0.00, (40, 8, 70)),
    (0.30, (120, 30, 160)),
    (0.55, (230, 70, 120)),
    (0.75, (255, 150, 50)),
    (1.00, (255, 245, 220)),
]


def ramp(t):
    t = max(0.0, min(1.0, t))
    for (t0, c0), (t1, c1) in zip(RAMP, RAMP[1:]):
        if t <= t1:
            f = (t - t0) / (t1 - t0)
            return tuple(int(a + (b - a) * f) for a, b in zip(c0, c1))
    return RAMP[-1][1]


def disk_light(x, y, phase):
    """Brightness (0..1) and temperature of the disk at disk-plane coordinates."""
    rd = math.hypot(x, y)
    if not DISK_IN <= rd <= DISK_OUT:
        return 0.0, 0.0
    theta = math.atan2(y, x)
    arms = 0.55 + 0.45 * math.sin(ARMS * (theta - phase) + rd * 0.85)
    doppler = 1.0 + 0.45 * math.cos(theta)               # left side approaching
    edge = min(1.0, (rd - DISK_IN) / 1.2, (DISK_OUT - rd) / 3.5)
    temperature = 1.0 - (rd - DISK_IN) / (DISK_OUT - DISK_IN)
    return max(0.0, min(1.0, 1.2 * arms * doppler * edge * (0.35 + 0.65 * temperature))), temperature


def over(dst, colour, alpha):
    """Composite a colour with the given alpha over dst (r, g, b, a floats)."""
    r, g, b, a = dst
    out_a = alpha + a * (1 - alpha)
    if out_a == 0:
        return dst
    mix = lambda c, d: (c * alpha + d * a * (1 - alpha)) / out_a
    return (mix(colour[0], r), mix(colour[1], g), mix(colour[2], b), out_a)


def frame(index):
    """Layers back to front: far disk, lensed arc, hole + photon ring, near disk."""
    phase = 2 * math.pi * index / FRAMES
    img = Image.new("RGBA", (SIZE, SIZE))
    px = img.load()
    c = (SIZE - 1) / 2
    for py in range(SIZE):
        for px_ in range(SIZE):
            x, y = px_ - c, py - c
            r = math.hypot(x, y)
            pixel = (0.0, 0.0, 0.0, 0.0)

            # faint violet glow around everything
            if r < DISK_OUT:
                pixel = over(pixel, (70, 20, 120), 0.18 * (1 - r / DISK_OUT))

            # far half of the disk
            if y < 0:
                b, t = disk_light(x, y / TILT, phase)
                pixel = over(pixel, ramp(t), b * 0.8)

            # far side of the disk lensed into an arc over the top of the hole
            ring = r - (HORIZON + PHOTON_RING)
            if y < 1 and 0 <= ring < 3.2:
                theta = math.atan2(y, x)
                swirl = 0.6 + 0.4 * math.sin(ARMS * (theta + phase) * 1.5)
                b = swirl * (1 - ring / 3.2) * min(1.0, (1 - y) / 3)
                pixel = over(pixel, ramp(0.85 - ring * 0.15), b * 0.9)

            # event horizon and photon ring
            if r < HORIZON:
                pixel = over(pixel, (0, 0, 0), 1.0)
            elif r < HORIZON + PHOTON_RING:
                flicker = 0.85 + 0.15 * math.sin(phase * 2 + math.atan2(y, x) * 4)
                pixel = over(pixel, ramp(0.97 * flicker), 1.0)

            # near half of the disk, crossing in front of the hole
            if y >= 0:
                b, t = disk_light(x, y / TILT, phase)
                pixel = over(pixel, ramp(t), b)

            if pixel[3] > 0.03:
                px[px_, py] = (int(pixel[0]), int(pixel[1]), int(pixel[2]), int(255 * min(1.0, pixel[3])))
    return img


def main(out_dir: Path):
    out = out_dir / "assets/arsbotania"
    frames = [frame(i) for i in range(FRAMES)]
    strip = Image.new("RGBA", (SIZE, SIZE * FRAMES))
    for i, f in enumerate(frames):
        strip.paste(f, (0, i * SIZE))
    tex = out / "textures/item/singularity.png"
    tex.parent.mkdir(parents=True, exist_ok=True)
    strip.save(tex)
    tex.with_name(tex.name + ".mcmeta").write_text(
        json.dumps({"animation": {"frametime": FRAME_TIME, "interpolate": True}}, indent=2) + "\n")
    model = {"parent": "minecraft:item/handheld", "textures": {"layer0": "arsbotania:item/singularity"}}
    (out / "models/item/singularity.json").write_text(json.dumps(model, indent=2) + "\n")
    print(f"singularity: {FRAMES} frames of {SIZE}x{SIZE}")


if __name__ == "__main__":
    main(Path(sys.argv[1]))
