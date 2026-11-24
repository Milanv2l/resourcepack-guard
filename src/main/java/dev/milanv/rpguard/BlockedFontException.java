package dev.milanv.rpguard;

/**
 * Thrown when a resource pack tries to load something the guard refuses.
 *
 * <p>Note: the font mixin does not throw this. {@code FontManager.safeLoad} catches every
 * {@link Exception}, so throwing would be survivable, but silently dropping the provider keeps
 * the rest of the font stack usable instead of losing the whole font definition.</p>
 */
public class BlockedFontException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BlockedFontException(String message) {
		super(message);
	}
}
