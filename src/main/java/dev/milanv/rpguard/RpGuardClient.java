package dev.milanv.rpguard;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class RpGuardClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		RpGuardConfig config = RpGuardConfig.get();
		String version = FabricLoader.getInstance()
				.getModContainer(RpGuard.MOD_ID)
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("unknown");

		RpGuard.LOGGER.info(
				"Resource Pack Guard {} active (blockTrueTypeFonts={}, blockServerResourcePacks={}, scanLocalPacksOnStartup={})",
				version, config.blockTrueTypeFonts, config.blockServerResourcePacks, config.scanLocalPacksOnStartup);

		if (config.scanLocalPacksOnStartup) {
			LocalPackScanner.scan();
		}
	}
}
