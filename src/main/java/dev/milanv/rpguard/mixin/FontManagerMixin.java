package dev.milanv.rpguard.mixin;

import com.mojang.datafixers.util.Pair;
import dev.milanv.rpguard.RpGuard;
import dev.milanv.rpguard.RpGuardConfig;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes every {@code ttf} glyph provider from a font definition before the definition is
 * handed to the loader, so the .ttf file never reaches FreeType.
 *
 * <p>Target: {@code FontManager.loadResourceStack(List<Resource>, Identifier)}, the private
 * static method that parses every {@code assets/<ns>/font/*.json} into
 * {@code Pair<BuilderId, GlyphProviderDefinition.Conditional>} entries. Its only caller is
 * {@code prepare(...)}, which feeds the result into {@code UnresolvedBuilderBundle} and then
 * {@code resolveProviders(...)}; filtering here means the {@code GlyphProviderDefinition.Loader}
 * of a ttf provider is never invoked.</p>
 *
 * <p>{@code FontManager.BuilderId} is package-private, so the list is handled with its erased
 * type and each entry is unwrapped through {@link Pair#getSecond()}.</p>
 */
@Mixin(FontManager.class)
public abstract class FontManagerMixin {
	@Inject(method = "loadResourceStack", at = @At("RETURN"), cancellable = true)
	private static void rpguard$dropTrueTypeProviders(List<Resource> resources, Identifier fontId,
			CallbackInfoReturnable<List> callback) {
		if (!RpGuardConfig.get().blockTrueTypeFonts) {
			return;
		}

		List<?> loaded = callback.getReturnValue();

		if (loaded == null || loaded.isEmpty()) {
			return;
		}

		List<Object> kept = new ArrayList<>(loaded.size());
		int removed = 0;

		for (Object element : loaded) {
			GlyphProviderDefinition definition = definitionOf(element);

			if (definition != null && definition.type() == GlyphProviderType.TTF) {
				removed++;
				continue;
			}

			kept.add(element);
		}

		if (removed == 0) {
			return;
		}

		RpGuard.LOGGER.warn("Removed {} TrueType font provider(s) for font '{}' before FreeType could load them",
				removed, fontId);
		callback.setReturnValue(kept);
	}

	/** Unwraps a {@code Pair<BuilderId, Conditional>} entry to its glyph provider definition. */
	private static GlyphProviderDefinition definitionOf(Object element) {
		if (!(element instanceof Pair<?, ?> pair)) {
			return null;
		}

		if (pair.getSecond() instanceof GlyphProviderDefinition.Conditional conditional) {
			return conditional.definition();
		}

		return null;
	}
}
