package dev.milanv.rpguard.mixin;

import dev.milanv.rpguard.RpGuard;
import dev.milanv.rpguard.RpGuardConfig;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.resources.server.ServerPackManager;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optional hard stop for server-pushed resource packs ({@code blockServerResourcePacks}).
 *
 * <p>Off by default: servers that require a pack become unusable when packs are declined, and
 * the font filter already removes the dangerous providers from whatever pack is loaded.</p>
 */
@Mixin(DownloadedPackSource.class)
public abstract class DownloadedPackSourceMixin {
	@Inject(method = "configureForServerControl", at = @At("TAIL"))
	private void rpguard$declineServerPacks(Connection connection, ServerPackManager.PackPromptStatus status,
			CallbackInfo callback) {
		if (!RpGuardConfig.get().blockServerResourcePacks) {
			return;
		}

		((DownloadedPackSource) (Object) this).rejectServerPacks();
		RpGuard.LOGGER.info("Declined the server resource pack (blockServerResourcePacks=true)");
	}
}
