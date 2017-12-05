package mini.asset.cache;

import mini.asset.AssetKey;
import mini.asset.CloneableSmartAsset;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <code>WeakRefCloneAssetCache</code> caches cloneable assets in a weak-key cache, allowing them to
 * be collected when memory is low. The cache stores weak references to the asset keys, so that when
 * all clones of the original asset are collected, will cause the asset to be automatically removed
 * from the cache.
 */
public class WeakRefCloneAssetCache implements AssetCache {

    @Override
    public <T> void registerAssetClone(AssetKey<T> key, T clone) {
        List<AssetKey> loadStack = assetLoadStack.get();
        ((CloneableSmartAsset) clone).setKey(loadStack.remove(loadStack.size() - 1));
    }

    @Override
    public boolean deleteFromCache(AssetKey key) {
        List<AssetKey> loadStack = assetLoadStack.get();
        if (!loadStack.isEmpty()) {
            throw new UnsupportedOperationException(
                    "Cache cannot be modified while assets are being"
                    + " loaded");
        }

        return smartCache.remove(key) != null;
    }

    private final ConcurrentMap<AssetKey, AssetRef> smartCache = new ConcurrentHashMap<>();
    private final ReferenceQueue<AssetKey> referenceQueue = new ReferenceQueue<>();
    private final ThreadLocal<List<AssetKey>> assetLoadStack =
            ThreadLocal.withInitial(ArrayList::new);

    private void removeCollectedAssets() {
        for (KeyRef ref; (ref = (KeyRef) referenceQueue.poll()) != null; ) {
            smartCache.remove(ref.clonedKey);
        }
    }

    @Override
    public <T> void addToCache(AssetKey<T> originalKey, T obj) {
        // Make remove for new assets
        removeCollectedAssets();

        CloneableSmartAsset asset = (CloneableSmartAsset) obj;

        // Remove circular references, since the original asset is strongly referenced.
        // We don't want the key strongly referenced.
        asset.setKey(null);

        // Start tracking the collection of originalKey (add KeyRef to ReferenceQueue)
        KeyRef ref = new KeyRef(originalKey, referenceQueue);

        smartCache.put(ref.clonedKey, new AssetRef(asset, originalKey));

        // Push the original key used to load the asset so that it can be set on the clone later.
        List<AssetKey> loadStack = assetLoadStack.get();
        loadStack.add(originalKey);
    }

    @Override
    public <T> T getFromCache(AssetKey<T> key) {
        AssetRef smartInfo = smartCache.get(key);

        if (smartInfo == null) {
            return null;
        } else {
            AssetKey keyForTheClone = smartInfo.get();
            if (keyForTheClone == null) {
                // was collected by GC between here and smartCache.get
                return null;
            }

            List<AssetKey> loadStack = assetLoadStack.get();
            loadStack.add(keyForTheClone);
            return (T) smartInfo.asset;
        }
    }

    private static final class KeyRef extends PhantomReference<AssetKey> {
        AssetKey clonedKey;

        /**
         * Creates a new phantom reference that refers to the given object and
         * is registered with the given queue.
         *
         * @param referent the object the new phantom reference will refer to
         * @param q        the queue with which the reference is to be registered,
         */
        public KeyRef(AssetKey originalKey, ReferenceQueue<? super AssetKey> referenceQueue) {
            super(originalKey, referenceQueue);
            clonedKey = originalKey.clone();
        }
    }

    /**
     * Stores the original key and original asset. The asset info contains a cloneable asset (e.g.
     * the original, from which all clones are made). Also a weak reference to the original key
     * which is used when the clones are produced.
     */
    private static final class AssetRef extends WeakReference<AssetKey> {
        CloneableSmartAsset asset;

        public AssetRef(CloneableSmartAsset originalAsset, AssetKey originalKey) {
            super(originalKey);
            this.asset = originalAsset;
        }
    }
}
