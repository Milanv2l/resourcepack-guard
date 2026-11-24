package dev.milanv.rpguard;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Startup scan of {@code resourcepacks/} for packs that ship font files.
 *
 * <p>This only logs: nothing is deleted and no pack is disabled. It exists so a user can see
 * which packs carry .ttf/.otf/.ttc payloads, which are only reachable through a {@code ttf}
 * glyph provider (already filtered by the mixin).</p>
 */
public final class LocalPackScanner {
	private static final List<String> FONT_SUFFIXES = List.of(".ttf", ".otf", ".ttc");
	private static final int MAX_REPORTED = 5;

	private LocalPackScanner() {
	}

	public static void scan() {
		Path directory;

		try {
			directory = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
		} catch (Throwable throwable) {
			RpGuard.LOGGER.warn("Could not resolve the resourcepacks directory: {}", throwable.toString());
			return;
		}

		if (!Files.isDirectory(directory)) {
			RpGuard.LOGGER.info("No resourcepacks directory at {}, skipping local pack scan", directory);
			return;
		}

		List<Path> packs;

		try (Stream<Path> stream = Files.list(directory)) {
			packs = stream.filter(LocalPackScanner::isPackEntry)
					.sorted(Comparator.comparing(path -> path.getFileName().toString()))
					.toList();
		} catch (IOException exception) {
			RpGuard.LOGGER.warn("Could not list {}: {}", directory, exception.toString());
			return;
		}

		int flagged = 0;

		for (Path pack : packs) {
			List<String> fonts = Files.isDirectory(pack) ? findInDirectory(pack) : findInArchive(pack);

			if (fonts.isEmpty()) {
				continue;
			}

			flagged++;
			RpGuard.LOGGER.warn("Resource pack '{}' ships {} font file(s): {}{}", pack.getFileName(), fonts.size(),
					summarize(fonts), fonts.size() > MAX_REPORTED ? ", ..." : "");
		}

		if (flagged == 0) {
			RpGuard.LOGGER.info("Local resource pack scan: no font files in {} pack(s) under {}", packs.size(), directory);
		} else {
			RpGuard.LOGGER.warn("Local resource pack scan: {} of {} pack(s) ship font files. They are only dangerous "
					+ "through a 'ttf' glyph provider; blockTrueTypeFonts={}", flagged, packs.size(),
					RpGuardConfig.get().blockTrueTypeFonts);
		}
	}

	/** True for the pack layouts Minecraft accepts: a directory, a .zip or a .jar. */
	private static boolean isPackEntry(Path path) {
		if (Files.isDirectory(path)) {
			return true;
		}

		String name = path.getFileName().toString().toLowerCase(Locale.ROOT);

		return Files.isRegularFile(path) && (name.endsWith(".zip") || name.endsWith(".jar"));
	}

	private static List<String> findInDirectory(Path root) {
		List<String> found = new ArrayList<>();

		try (Stream<Path> stream = Files.walk(root)) {
			stream.filter(Files::isRegularFile).map(path -> root.relativize(path).toString()).filter(LocalPackScanner::isBlockedFontResource)
					.sorted().forEach(found::add);
		} catch (IOException exception) {
			RpGuard.LOGGER.warn("Could not walk {}: {}", root, exception.toString());
		}

		return found;
	}

	private static List<String> findInArchive(Path archive) {
		List<String> found = new ArrayList<>();

		try (ZipFile zip = new ZipFile(archive.toFile())) {
			Enumeration<? extends ZipEntry> entries = zip.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();

				if (!entry.isDirectory() && isBlockedFontResource(entry.getName())) {
					found.add(entry.getName());
				}
			}
		} catch (IOException exception) {
			RpGuard.LOGGER.warn("Could not read archive {}: {}", archive, exception.toString());
		}

		found.sort(Comparator.naturalOrder());

		return found;
	}

	/** True when the resource path ends in a font-file suffix. */
	public static boolean isBlockedFontResource(String path) {
		String lower = path.toLowerCase(Locale.ROOT);

		for (String suffix : FONT_SUFFIXES) {
			if (lower.endsWith(suffix)) {
				return true;
			}
		}

		return false;
	}

	private static String summarize(List<String> fonts) {
		return String.join(", ", fonts.subList(0, Math.min(MAX_REPORTED, fonts.size())));
	}
}
