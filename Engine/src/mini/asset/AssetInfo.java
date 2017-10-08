package mini.asset;

import java.io.InputStream;

/**
 * The result of location an asset through an AssetKey. Provides a means to read the asset data
 * through an InputStream
 */
public abstract class AssetInfo {
    private AssetManager manager;
    private AssetKey key;

    public AssetInfo(AssetManager manager, AssetKey key) {
        this.manager = manager;
        this.key = key;
    }

    public AssetManager getManager() {
        return manager;
    }

    public AssetKey getKey() {
        return key;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[key=" + key + ']';
    }

    /**
     * Implementations of this method should return an {@link InputStream} allowing access to the
     * data represented by the {@link AssetKey}.
     * <p>
     * Each invocation of this method should return a new stream to the asset data, starting at the
     * beginning of the file.
     *
     * @return The asset data.
     */
    public abstract InputStream openStream();
}
