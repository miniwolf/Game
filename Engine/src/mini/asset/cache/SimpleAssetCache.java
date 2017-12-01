package mini.asset.cache;

import mini.asset.AssetKey;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
}
