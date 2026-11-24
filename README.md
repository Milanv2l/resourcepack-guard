# Resource Pack Guard (`rpguard`)

A client-side Fabric mod that stops resource packs from handing crafted TrueType fonts to the
native FreeType library. It exists because of the "minecraft2shell" proof of concept: a pack can
ship a `.ttf` whose hinting writes out of bounds inside FreeType, which is turned into a ROP chain
and code execution. Minecraft loads such a font the first time a glyph from it is drawn — an
actionbar title is enough.

This is a stopgap. The durable fix belongs upstream, in FreeType or in the game's native
libraries; this mod only closes the one path that is known to be exploitable today.

## What it blocks

Every `ttf` glyph provider is removed from the font definitions **before** FreeType ever sees the
file. The mod does not reject packs: the rest of a pack still applies, only the TrueType providers
are dropped. The mixin sits on `FontManager#loadResourceStack` (return injection), which is the
single place where `assets/<namespace>/font/*.json` is parsed, so both server packs and local packs
go through it.

Each removal is logged:

```
Removed 1 TrueType font provider(s) for font 'ctf:deferred-000' before FreeType could load them
```

A font stack may mix provider types, so a font that has a `space` provider and a `ttf` provider keeps
the `space` provider and loses only the `ttf` one.

On startup the mod also scans `resourcepacks/` and logs local packs that ship `.ttf`, `.otf` or
`.ttc` files. That scan is informational only; nothing is deleted.

## Installation

- Minecraft **26.3** with Fabric Loader **0.20.1** or newer and **Java 25**.
- Client side only; on a server the mod does nothing.
- No Fabric API dependency.

Drop `rpguard-1.0.0.jar` into `mods/` and start the game. The config file
`config/rpguard.json` is created on first start.

## Download

| | |
|---|---|
| Version | 1.0.0 (Minecraft 26.3, Fabric) |
| File | `rpguard-1.0.0.jar` (13.501 bytes) |
| SHA-256 | `544339c7711e8a88722031b9279eccc9539bdbdeb6703e951d77bb04e4426733` |

That checksum is not decoration: this exact jar is the build the guard was tested with. Verify it
with `sha256sum rpguard-1.0.0.jar` (Linux) or `certutil -hashfile rpguard-1.0.0.jar SHA256`
(Windows) before you install it.

A fresh build from this tree yields
`8c3af10e9c2be8de2d1f0b0c70b3733da973c89625c5d7721d767c3a69e16aeb`. The only difference from the
published jar is the `LICENSE_rpguard` entry that `build.gradle` bundles; all seven class files are
byte-identical. The published jar was built before the `LICENSE` file existed, so the license text
ships in this repository instead of inside that jar. If you want the license inside the jar, build
it yourself — the class bytes are unchanged.

## Minecraft 26.3 only

This build targets Minecraft 26.3 and nothing else:

- It is **not remapped**. 26.3 ships with Mojang names, so there was no mapping step and the jar
  carries 26.3 class names directly.
- The mixin targets a **`private static`** method (`FontManager.loadResourceStack(List,
  Identifier)`). A signature change in a future Minecraft version is not something a mixin can
  paper over.
- The mixin config sets `required: true` and `defaultRequire: 1`, so a mismatch is a **hard startup
  failure** instead of a silent loss of protection. If the game refuses to start after a Minecraft
  update, that is the guard telling you it no longer applies — check the mixin target before
  disabling anything.

There is no Fabric API dependency.

## Configuration

`config/rpguard.json` is created on first start. A missing file is written with the defaults; an
unreadable or malformed file leaves the file untouched, logs a warning and falls back to the
defaults. The mod never crashes because of its own config.

| Key | Default | Meaning |
|---|---|---|
| `blockTrueTypeFonts` | `true` | Strip every `ttf` glyph provider before FreeType loads it. Turning this off disables the actual protection. |
| `blockServerResourcePacks` | `false` | Refuse server resource packs outright (`DownloadedPackSource#configureForServerControl` → `rejectServerPacks()`). Off by default: servers that require a pack will not let you play while this is on. |
| `scanLocalPacksOnStartup` | `true` | Log local packs in `resourcepacks/` that contain `.ttf`, `.otf` or `.ttc` files. |

## Known gaps

- **Only `ttf` providers are covered.** Bitmap, unihex and any other glyph provider type are left
  alone; a future exploit through another provider type is not addressed.
- **Server-pack rejection is opt-in** and is a blunt instrument: it drops the whole pack and, on
  servers with `require-resource-pack`, the connection.
- **Tested on Windows x64 with Minecraft 26.3 only.** The vulnerable code path is native FreeType,
  so behaviour on other platforms and versions is unverified. The blocking itself happens before
  the native call and is platform independent; what is untested is whether the exploit detonates
  on Linux or macOS.
- **The guard filters fonts, it does not sanitise them.** The mod is a mitigation for one known
  attack path, not a fix for FreeType itself; the durable fix belongs upstream in the game's native
  libraries.

## Test evidence

The mod was built and detonated against the public proof of concept, on Minecraft 26.3, Fabric
Loader 0.20.1, Java 25, Windows x64:

| Configuration | Runs | Code execution (Calculator) | Native FreeType crash |
|---|---|---|---|
| Guard off | 5 | 4 | 1 |
| Guard on | 4 | 0 | 0 |

The four guard runs cover two different attack routes: a server push (`require-resource-pack`) and
the same pack installed locally in `resourcepacks/`. Every guard run removed 144 TrueType font
providers and produced no native error. With the guard off, the local route detonated as soon as
the first glyphs were requested.

## Building

```
./gradlew build
```

The jar lands in `build/libs/`. Gradle 9.x, Loom 1.19.4, JDK 25, no mappings (26.3 is
unobfuscated).

## Links

- Source and issue tracker: <https://github.com/Milanv2l/resourcepack-guard>
- Modrinth: not published yet

## AI disclosure

The mod's source code and this documentation were written with an AI coding assistant. The
vulnerability analysis, the detonation tests against the proof of concept, and the verification of
every claim above were done by a human on real hardware. No AI-generated images are used:
`icon.png` is drawn from a hand-written pixel map in `tools/make-icon.py`.

## License

MIT — see [LICENSE](LICENSE).
