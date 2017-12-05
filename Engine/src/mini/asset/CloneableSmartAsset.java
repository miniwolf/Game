package mini.asset;

/**
 * Implementing the <code>CloneableAsset</code> interface allows use of cloneable smart asset
 * management.
 * <p>
 * Smart asset management requires cooperation from the {@link AssetKey}. In particular, the
 * AssetKey should return {@link mini.asset.cache.WeakRefCloneAssetCache} in its
 * {@link AssetKey#getCacheType()} method. Also smart assets MUST create a clone of the asset and
 * cannot return the same reference.
 * <p>
 * If the {@link AssetManager#loadAsset(AssetKey)} method is called twice with the same asset key
 * (equals() wise, not necessarily reference wise) then both assets will have the same asset key set
 * (reference wise) via {@link AssetKey#AssetKey()}, then this asset key is used to track all
 * instances of that asset. Once all clones of the asset are garbage collected, the shared asset key
 * becomes unreachable and at that point it is removed from the smart asset cache.
 */
public interface CloneableSmartAsset extends Cloneable {
    Object clone();

    /**
     * Set by the {@link AssetManager} to track this asset.
     * <p>
     * Only clones of the asset has this set, the original copy that was loaded has this key set to
     * null so that only the clones are tracked for garbage collection.
     *
     * @param key The AssetKey to set.
     */
    void setKey(AssetKey key);
}
