package mini.asset;

/**
 * <code>AssetLoadException</code> is thrown when the {@link AssetManager} is able to find the
 * requested asset, but there was a problem while loading it.
 */
public class AssetLoadException extends RuntimeException {
    public AssetLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
