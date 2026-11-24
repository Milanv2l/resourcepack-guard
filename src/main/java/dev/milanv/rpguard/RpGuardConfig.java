package dev.milanv.rpguard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gson-backed config at {@code config/rpguard.json}.
 *
 * <p>The file is created with these defaults when it does not exist. A file that cannot be read
 * or parsed never takes the client down: the defaults are used and a warning is logged. Such a
 * file is left untouched so the user can repair it by hand.</p>
 */
public final class RpGuardConfig {
	private static final String FILE_NAME = "rpguard.json";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/** Drop every {@code ttf} glyph provider before FreeType ever sees the font file. */
	public boolean blockTrueTypeFonts = true;
	/** Refuse server resource packs outright (default off: forced packs are common). */
	public boolean blockServerResourcePacks = false;
	/** Log which packs in {@code resourcepacks/} ship .ttf/.otf/.ttc files. */
	public boolean scanLocalPacksOnStartup = true;

	private static RpGuardConfig instance;

	public static synchronized RpGuardConfig get() {
		if (instance == null) {
			instance = load();
		}

		return instance;
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	private static RpGuardConfig load() {
		Path path;

		try {
			path = path();
		} catch (Throwable throwable) {
			// Loader or game directory not available (very early call): defaults, no file access.
			RpGuard.LOGGER.warn("Config path unavailable ({}), using built-in defaults", throwable.toString());
			return new RpGuardConfig();
		}

		try {
			if (Files.notExists(path)) {
				RpGuardConfig config = new RpGuardConfig();
				config.save();
				RpGuard.LOGGER.info("Created default config at {}", path);
				return config;
			}

			try (Reader reader = Files.newBufferedReader(path)) {
				RpGuardConfig parsed = GSON.fromJson(reader, RpGuardConfig.class);

				if (parsed == null) {
					RpGuard.LOGGER.warn("Config {} is empty, using defaults", path);
					return new RpGuardConfig();
				}

				return parsed;
			}
		} catch (Exception exception) {
			RpGuard.LOGGER.warn("Could not read {} ({}), using defaults; the file was left untouched", path, exception.toString());
			return new RpGuardConfig();
		}
	}

	public void save() {
		try {
			Path path = path();
			Files.createDirectories(path.getParent());

			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(this, writer);
			}
		} catch (Exception exception) {
			RpGuard.LOGGER.warn("Could not write config: {}", exception.toString());
		}
	}
}
