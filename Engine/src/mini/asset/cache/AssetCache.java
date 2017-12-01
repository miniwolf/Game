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
    <T> void addToCache(AssetKey<T> key, T obj);

    /**
     * Retreive an asset from the cache. It is possible to add an asset to the cache using
     * {@link #addToCache(AssetKey, Object)}. The asset may be removed from the cache automatically
     * even if it was added previously, in that case, this method will return null.
     * <p><font color="red">Thread-Safe</font></p>
     *
     * @param key The key used to lookup the asset.
     * @param <T> The type of the asset to retrieve.
     * @return The asset that was previously cached, or null if not found.
     */
    <T> T getFromCache(AssetKey<T> key);
}
