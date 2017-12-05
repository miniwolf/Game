package mini.asset.cache;

import mini.asset.AssetKey;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <code>SimpleAssetCache</code> is an asset cache that caches assets without any automatic removal
 * policy. The user is expected to manually call {@link #deleteFromCache}
 */
public class SimpleAssetCache implements AssetCache {
    private final ConcurrentMap<AssetKey, Object> keyToAssetMap = new ConcurrentHashMap<>();

    @Override
    public <T> void addToCache(AssetKey<T> key, T obj) {
        keyToAssetMap.put(key, obj);
    }

    @Override
    public <T> T getFromCache(AssetKey<T> key) {
        return (T) keyToAssetMap.get(key);
    }

    @Override
    public <T> void registerAssetClone(AssetKey<T> key, T clone) {
    }

    @Override
    public boolean deleteFromCache(AssetKey key) {
        return keyToAssetMap.remove(key) != null;
    }
}
