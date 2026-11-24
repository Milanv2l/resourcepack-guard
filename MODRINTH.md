# Modrinth publication — `rpguard` 1.0.0

Ready to fill in at https://modrinth.com/mod/create. This file belongs to the repository and is
not bundled into the jar.

## Project fields

| Field | Value |
|---|---|
| Name | Resource Pack Guard |
| Slug | `resource-pack-guard` |
| Summary (max 256) | Blocks the minecraft2shell exploit: `ttf` glyph providers from resource packs are stripped before FreeType loads them. Stopgap mitigation, not a replacement for an upstream fix. |
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
**Resource Pack Guard** strips every `ttf` glyph provider from resource pack font definitions
before FreeType ever sees the file. It is a stopgap for the public *minecraft2shell* proof of
concept, where a malicious `.ttf` writes out of bounds inside FreeType and turns that into code
execution on the client. Minecraft loads such a font the first time a glyph from it is drawn — an
actionbar title is enough.

The mod does not reject packs: the rest of a pack still applies, only the TrueType providers are
dropped. Every removal is logged:

```
Removed 1 TrueType font provider(s) for font 'ctf:deferred-000' before FreeType could load them
```

The mixin sits on `FontManager#loadResourceStack`, the single place where
`assets/<namespace>/font/*.json` is parsed, so both server packs and local packs go through it.

### Minecraft 26.3 only

This build targets 26.3 and nothing else. It is not remapped (26.3 ships unobfuscated) and the
mixin targets a `private static` method. The mixin config sets `required: true` and
`defaultRequire: 1`: on a version mismatch the game **refuses to start** instead of silently
losing the protection. If your client will not start after a Minecraft update, that is the guard
telling you it no longer applies — check the mixin target before disabling anything.

### Installation

1. Minecraft 26.3 with Fabric Loader 0.20.1 or newer and Java 25.
2. Drop `rpguard-1.0.0.jar` into `mods/`.
3. Optional: adjust `config/rpguard.json` (created on first start).

No Fabric API needed. Client side only; the mod does nothing on a server.

### Configuration

| Key | Default | Meaning |
|---|---|---|
| `blockTrueTypeFonts` | `true` | Strip every `ttf` provider before FreeType loads it. Turning this off disables the protection. |
| `blockServerResourcePacks` | `false` | Refuse server packs outright. Off by default: servers with a required pack will not let you join while this is on. |
| `scanLocalPacksOnStartup` | `true` | Log which packs in `resourcepacks/` contain `.ttf`/`.otf`/`.ttc`. Informational only. |

### What it does not do

- **Only `ttf` providers.** Bitmap, unihex and other provider types are left alone.
- **Not a sanitiser.** It filters fonts; it does not fix FreeType. The real fix belongs upstream.
- **Tested on Windows x64 with 26.3.** The blocking happens before the native call and is therefore
  platform independent, but whether the exploit detonates on Linux or macOS is unverified.

Built and tested against a public proof of concept: without the guard 4× code execution and 1×
native crash, with the guard 4 runs across two attack routes (server pack and a locally installed
pack) with 144 removed providers per run, zero code execution and zero native errors.
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
