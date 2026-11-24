# Modrinth publication — `rpguard` 1.0.0

Ready to fill in at https://modrinth.com/mod/create. This file belongs to the repository and is
not bundled into the jar.

## Project fields

| Field | Value |
|---|---|
| Name | Resource Pack Guard |
| Slug | `resource-pack-guard` |
| Summary (max 256) | Stops resource packs from using a malicious font file to run code on your computer. Client-side, Minecraft 26.3. |
| Project type | Mod |
| Environment | Client only (required) |
| License | MIT |
| Categories | Utility, Management |
| Loaders | Fabric |
| Game versions | 26.3 (only) |
| Source link | https://github.com/Milanv2l/resourcepack-guard |
| Issue tracker | https://github.com/Milanv2l/resourcepack-guard/issues |
| Icon | `icon.png` (1024×1024) |

## Description

> Paste this as the project description.

```markdown
A resource pack should change how your game looks, not what your game runs.

**Resource Pack Guard** is a small client-side mod that closes a hole in how Minecraft handles
fonts. Resource packs can ship their own font files, and those files are handed to a system library
to be drawn. A deliberately broken font can make that library write into memory it should not
touch, and the *minecraft2shell* proof of concept turns that into running a program on the player's
computer. You don't have to click anything: it happens the moment a single character in that font
appears on screen — an actionbar message is enough.

## What the mod does

It drops custom TrueType fonts from resource packs before they are ever opened. Everything else in
the pack keeps working: textures, models, sounds, the interface. Only the font files are removed,
and each removal is noted in the log.

The mod runs on your client only. Servers don't need it, other players aren't affected, and
gameplay stays exactly the same.

## Installation

1. Minecraft **26.3** with Fabric Loader 0.20.1 or newer and Java 25.
2. Put the jar in your `mods` folder.
3. Start the game.

No Fabric API, no other dependencies.

## Important: 26.3 only

This release is built for Minecraft 26.3 and nothing else. If Minecraft updates, the mod
deliberately refuses to start instead of quietly losing its protection. If your game won't launch
after an update, that is the mod telling you it no longer applies — remove it or wait for a newer
release.

## Settings

`config/rpguard.json` is created on first start:

- **`blockTrueTypeFonts`** (on by default) — the actual protection. Turning this off disables the
  mod.
- **`blockServerResourcePacks`** (off by default) — refuse server resource packs entirely. Only
  useful if you never need a server's pack; servers that require one won't let you join.
- **`scanLocalPacksOnStartup`** (on by default) — on startup, note which packs in `resourcepacks/`
  contain font files. Information only, nothing is deleted.

## Limits

- Only TrueType fonts are handled; other font types are left alone.
- This is a stopgap for one known attack path, not a fix for the underlying library.
- Tested on Windows x64 with 26.3. The blocking happens before the font is opened, so other
  platforms should behave the same, but I have not verified them.

Technical details, source code and the full test results live on GitHub:
<https://github.com/Milanv2l/resourcepack-guard>

In testing against the public proof of concept: without the mod, 4 of 5 runs let the attacker run
code and 1 crashed the game; with the mod, 4 runs across two delivery routes (a server pack and a
pack installed locally) blocked all 144 malicious fonts, with no code execution and no crash.
```

## Release 1.0.0

| Field | Value |
|---|---|
| Version name | 1.0.0 |
| Channel | Release |
| Game versions | 26.3 |
| Loader | Fabric |
| File | `rpguard-1.0.0.jar` (13,501 bytes) |
| SHA-256 | `544339c7711e8a88722031b9279eccc9539bdbdeb6703e951d77bb04e4426733` |
| Dependencies | none (Fabric Loader ≥ 0.20.1, Java ≥ 25, Minecraft 26.3) |

Changelog:

```markdown
First release.

- Strips every `ttf` glyph provider from resource pack font definitions before FreeType loads the
  file, via a return injection on `FontManager#loadResourceStack`.
- Optional server pack rejection (`blockServerResourcePacks`, off by default).
- Startup scan that logs local packs containing `.ttf`/`.otf`/`.ttc`.
- Config at `config/rpguard.json`; a missing or broken file falls back to the defaults without
  crashing the client.
- Minecraft 26.3 only, client side, no Fabric API.
```

## Before you press publish

- [x] GitHub repository created and the source link filled in above.
- [ ] `icon.png` uploaded as the project icon.
- [ ] Game version **26.3 only** selected, not "all versions".
- [ ] The uploaded jar is byte-identical to the tested build: SHA-256 `544339c7…6733`. Check it
      with `sha256sum`/`certutil -hashfile` before uploading.
- [ ] Upload the tested jar, not your own build output: a fresh build is `8c3af10e…aeb`
      (same code, plus `LICENSE_rpguard`).
- [ ] MIT selected as the license (required by Modrinth).
