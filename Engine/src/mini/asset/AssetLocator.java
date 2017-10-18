package mini.asset;

public interface AssetLocator {
    /**
     * Request to locate an asset. The asset key contains a name identifying the asset.
     * If an asset was not found, null should be returned.
     * The {@link AssetInfo} implementation provided should have a proper return value for its
     * {@link AssetInfo#openStream()} method.
     *
     * @param manager assetManager for loading the asset.
     * @param key     contains the name identifying the asset.
     * @return The {@link AssetInfo} that was located, or null if not found.
     */
    AssetInfo locate(AssetManager manager, AssetKey key);
}