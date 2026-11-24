#!/usr/bin/env bash
# Draws the project icon from plain geometry.
#
# There is no generative model involved: every coordinate below was written by
# hand, so anyone can re-run this and get the same pixels. That is the point —
# Modrinth's content rules do not allow AI-generated page images, and this
# script is the provenance for the icon that ships with the project.
#
# Usage: ./tools/make-icon.sh [output.png]

set -euo pipefail

out="${1:-icon.png}"

background="#232B33"
shield_fill="#17423C"
shield_edge="#7FD1E0"
glyph="#79C08F"
pixel="#4E8F6B"

# Hexagonal shield, pointy top and bottom, centred on a 1024x1024 canvas.
hexagon="512,132 841,322 841,702 512,892 183,702 183,322"

# Small squares biting into the left edge, as if the shield is eroding there.
pixels=(
	"196,430 222,456"
	"236,404 258,426"
	"244,478 264,498"
	"214,522 234,542"
	"186,368 204,386"
	"248,556 266,574"
)

draw_pixels=""
for box in "${pixels[@]}"; do
	draw_pixels+="rectangle ${box} "
done

# A serif face for the glyph; fall back to Times if Liberation is missing.
font="/usr/share/fonts/liberation-serif-fonts/LiberationSerif-Bold.ttf"
[ -f "${font}" ] || font="Times-Bold"

magick -size 1024x1024 "xc:${background}" \
	-fill "${shield_fill}" -stroke "${shield_edge}" -strokewidth 26 \
	-draw "polygon ${hexagon}" \
	-stroke none \
	-fill "${pixel}" \
	-draw "${draw_pixels}" \
	-font "${font}" -pointsize 430 -fill "${glyph}" \
	-gravity center -annotate +0+8 "A" \
	"${out}"

echo "wrote ${out}"
