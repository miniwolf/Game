package mini.asset;

/**
 * <code>AssetEventListener</code> is an interface for listening to various events happening inside
 * {@link AssetManager}. For now, it is possible to receive an event when an asset has been
 * requested (one of the AssetManager.load(***) methods were called), or when an asset has been
 * loaded.
 */
public interface AssetEventListener {
    /**
     * Called when an asset dependency cannot be found for an asset. When an asset is loaded, each
     * of its dependent assets that have failed to load due to a {@link AssetNotFoundException},
     * will cause an invocation of this callback.
     *
     * @param parentKey         The key of the parent asset that is being loaded from within the user
     *                          application.
     * @param dependentAssetKey The asset key of the dependent asset that has failed to load.
     */
    void assetDependencyNotFound(AssetKey parentKey, AssetKey dependentAssetKey);

    /**
     * Called when an asset has been automatically loaded (e.g. loaded from file system and parsed).
     *
     * @param key the <code>AssetKey</code> for the asset loaded.
     */
    <T> void assetLoaded(AssetKey<T> key);
}
