#!/usr/bin/env python3
"""Draws the project icon as Minecraft-style pixel art.

The whole icon is a 16x16 character map scaled up by a fixed factor: no
generative model is involved, and re-running this produces the same pixels.
Modrinth's content rules do not allow AI-generated page images, so this script
is the provenance for the icon that ships with the project.

Usage: python3 tools/make-icon.py [output.png]
"""

import subprocess
import sys
import tempfile
from pathlib import Path

GRID = 16
SCALE = 64

BACKGROUND = (43, 43, 43)
SHIELD = (240, 216, 120)
SHIELD_LIGHT = (255, 238, 160)
SHIELD_DARK = (196, 164, 72)
GLYPH = (58, 58, 58)

# '#' is shield, '.' is background.
SHIELD_MASK = [
    "................",
    ".....######.....",
    "....########....",
    "...##########...",
    "..############..",
    "..############..",
    "..############..",
    "..############..",
    "...##########...",
    "...##########...",
    "....########....",
    "....########....",
    ".....######.....",
    ".....######.....",
    "......####......",
    ".......##.......",
]

# Blocky capital A, the same idea as Minecraft's own bitmap font.
GLYPH_MASK = [
    "..#..",
    ".#.#.",
    "#...#",
    "#...#",
    "#####",
    "#...#",
    "#...#",
]
GLYPH_TOP = 4
GLYPH_LEFT = 5


def is_shield(row: int, col: int) -> bool:
    if not (0 <= row < GRID and 0 <= col < GRID):
        return False
    return SHIELD_MASK[row][col] == "#"


def is_border(row: int, col: int) -> bool:
    return is_shield(row, col) and not all(
        is_shield(row + dr, col + dc)
        for dr, dc in ((-1, 0), (1, 0), (0, -1), (0, 1))
    )


def build_grid() -> list[list[tuple[int, int, int]]]:
    grid = [[BACKGROUND for _ in range(GRID)] for _ in range(GRID)]

    for row in range(GRID):
        for col in range(GRID):
            if not is_shield(row, col):
                continue
            if is_border(row, col):
                grid[row][col] = SHIELD_DARK
            else:
                grid[row][col] = SHIELD

    # One lit row under the top edge, the way Minecraft's own item textures
    # suggest a surface that catches light.
    for col in range(GRID):
        if is_shield(1, col) and not is_border(1, col):
            grid[1][col] = SHIELD_LIGHT

    for dy, line in enumerate(GLYPH_MASK):
        for dx, char in enumerate(line):
            if char != "#":
                continue
            row, col = GLYPH_TOP + dy, GLYPH_LEFT + dx
            if is_shield(row, col) and not is_border(row, col):
                grid[row][col] = GLYPH

    return grid


def write_ppm(path: Path) -> None:
    with path.open("wb") as handle:
        handle.write(f"P6\n{GRID} {GRID}\n255\n".encode())
        for row in build_grid():
            for pixel in row:
                handle.write(bytes(pixel))


def main() -> int:
    out = Path(sys.argv[1] if len(sys.argv) > 1 else "icon.png")
    with tempfile.TemporaryDirectory() as tmp:
        ppm = Path(tmp) / "icon.ppm"
        write_ppm(ppm)
        subprocess.run(
            [
                "magick",
                str(ppm),
                "-filter",
                "point",
                "-resize",
                f"{GRID * SCALE}x{GRID * SCALE}",
                "-depth",
                "8",
                f"PNG24:{out}",
            ],
            check=True,
        )
    print(f"wrote {out}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
