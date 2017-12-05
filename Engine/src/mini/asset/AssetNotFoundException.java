package mini.asset;

/**
 * <code>AssetNotFoundException</code> is thrown when the {@link AssetManager} is unable to locate
 * the requested asset using any of the registered {@link AssetLocator}s.
 */
public class AssetNotFoundException extends RuntimeException {
    public AssetNotFoundException(String message) {
        super(message);
    }

    public AssetNotFoundException(String message, Exception ex) {
        super(message, ex);
    }
}
