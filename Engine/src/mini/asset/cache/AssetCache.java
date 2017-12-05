package mini.asset.cache;

import mini.asset.AssetKey;

/**
 * <code>AssetCache</code> is an interface for asset caches. Allowing storage of loaded resources in
 * order to improve their access time if they are requested again in a short period of time.
 * Depending on the asset type and how it is used, a specialized caching method can be selected that
 * is most appropriate for that asset type. The asset cache must be thread safe.
 * <p>
 * Some caches are used to manage cloneable assets, which track reachability based on a shared key
 * in all instances exposed in user code.
 */
public interface AssetCache {
    /**
     * Adds an asset to the cache. Once added, it should be possible to retrieve the asset by using
     * the {@link #getFromCache(AssetKey)} method. However, the caching criteria may at some point
     * choose that the asset should be removed from the cache to save memory, in that case,
     * {@link #getFromCache(AssetKey)} will return null.
     * <p><font color="red">Thread-Safe</font></p>
     *
     * @param key The asset key that can be used to look up the asset.
     * @param obj The asset data to cache.
     * @param <T> The type of the asset to cache.
     */
    <T> void addToCache(AssetKey<T> key, T obj);

    /**
     * Retrieve an asset from the cache. It is possible to add an asset to the cache using
     * {@link #addToCache(AssetKey, Object)}. The asset may be removed from the cache automatically
     * even if it was added previously, in that case, this method will return null.
     * <p><font color="red">Thread-Safe</font></p>
     *
     * @param key The key used to lookup the asset.
     * @param <T> The type of the asset to retrieve.
     * @return The asset that was previously cached, or null if not found.
     */
    <T> T getFromCache(AssetKey<T> key);

    /**
     * This should be called by the asset manager when it has successfully acquired a cached asset
     * (with {@link #getFromCache(AssetKey)} and cloned it for use.
     *
     * @param key   The asset key of the loaded asset (used to retrieve from cache).
     * @param clone The <strong>clone</strong> of the asset retrieved from the cache.
     * @param <T>   The type of asset to register.
     */
    <T> void registerAssetClone(AssetKey<T> key, T clone);

    /**
     * Deletes an asset from the cache.
     * <p><font color="red">Thread-Safe</font></p>
     *
     * @param key The asset key to find the asset to delete.
     * @return Whether the asset was successfully found in the cache and removed.
     */
    boolean deleteFromCache(AssetKey key);
}
