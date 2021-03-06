package mini.asset;

import mini.asset.cache.AssetCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Manages the asset loader and asset locator implementations. This is done by keeping an instance
 * of each asset loader in a thread local.
 */
public class ImplHandler {
    private final ThreadLocal<AssetKey> parentAssetKey = new ThreadLocal<>();
    private final ConcurrentMap<Class<? extends AssetCache>, AssetCache> classToCacheMap =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends AssetProcessor>, AssetProcessor> classToProcessMap =
            new ConcurrentHashMap<>();
    private Map<String, ImplThreadLocal<? extends AssetLoader>> extensionsToLoaderMap
            = new HashMap<>();
    private List<ImplThreadLocal<AssetLocator>> locatorList = new ArrayList<>();
    private AssetManager assetManager;
    private final ConcurrentMap<Class<? extends AssetLoader>, ImplThreadLocal> classToLoaderMap
            = new ConcurrentHashMap();

    public ImplHandler(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    /**
     * Returns the AssetLoader registered for the given extension of the current thread.
     *
     * @return AssetLoader registered with addLoader.
     */
    public <T> AssetLoader<T> acquireLoader(AssetKey<T> key) {
        ImplThreadLocal<? extends AssetLoader> local = extensionsToLoaderMap
                .get(key.getExtension());
        if (local == null) {
            throw new RuntimeException("No loader registered for type \"" + key.getExtension()
                                       + "\"");
        }
        return local.get();
    }

    public void addLoader(Class<? extends AssetLoader> loaderType, String... extensions) {
        ImplThreadLocal<? extends AssetLoader> local = new ImplThreadLocal<>(loaderType,
                                                                             extensions);
        for (String extension : extensions) {
            extension = extension.toLowerCase();

            classToLoaderMap.put(loaderType, local);
            extensionsToLoaderMap.put(extension, local);
        }
    }

    public void addLocator(Class<? extends AssetLocator> locatorType, String rootPath) {
        locatorList.add(new ImplThreadLocal<>(locatorType, rootPath));
    }

    public AssetKey getParentKey() {
        return parentAssetKey.get();
    }

    public void removeLocator(Class<? extends AssetLocator> locatorClass, String rootPath) {
        List<ImplThreadLocal<AssetLocator>> localsToRemove =
                locatorList.stream()
                           .filter(local -> local.getPath().equals(rootPath) &&
                                            local.getType().equals(locatorClass))
                           .collect(Collectors.toList());
        locatorList.removeAll(localsToRemove);
    }

    /**
     * Attempts to locate the given resource name.
     *
     * @param key The full name of the resource
     * @return The {@link AssetInfo} containing resource information required for access, or null
     * if not found.
     */
    public AssetInfo tryLocate(AssetKey key) {
        for (ImplThreadLocal<AssetLocator> local : locatorList) {
            AssetInfo info = local.get().locate(assetManager, key);
            if (info != null) {
                return info;
            }
        }
        System.err.println("Warning: "
                           + "There are no locators currently registered to support: " + key);
        return null;
    }

    /**
     * Establishes the asset key that is used for tracking dependent assets that have failed to load.
     * When set, the {@link AssetManager} gets a hint that it should suppress {@link Exception}s and
     * instead call the listener callback (if set).
     *
     * @param parentKey The parent key
     */
    public <T> void establishParentKey(AssetKey<T> parentKey) {
        if (parentAssetKey.get() == null) {
            parentAssetKey.set(parentKey);
        }
    }

    public <T> void releaseParentKey(AssetKey<T> parentKey) {
        if (parentAssetKey.get() == parentKey) {
            parentAssetKey.set(null);
        }
    }

    public void clearCache(){
        // The iterator of the values collection is thread safe
        synchronized (classToCacheMap) {
            for (AssetCache cache : classToCacheMap.values()){
                cache.clearCache();
            }
        }
    }

    public <T extends AssetCache> T getCache(Class<T> cacheClass) {
        if (cacheClass == null) {
            return null;
        }

        T cache = (T) classToCacheMap.get(cacheClass);
        if (cache != null) {
            return cache;
        }

        try {
            cache = cacheClass.newInstance();
            classToCacheMap.put(cacheClass, cache);
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException("The cache class cannot be created, ensure it has an"
                                               + "empty constructor", ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("The cache class cannot be accessed", ex);
        }
        return cache;
    }

    public <T extends AssetProcessor> T getProcessor(Class<T> processorType) {
        if (processorType == null) {
            return null;
        }

        T process = (T) classToProcessMap.get(processorType);
        if (process != null) {
            return process;
        }

        try {
            process = processorType.newInstance();
            classToProcessMap.put(processorType, process);
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException("The processor class cannot be created, ensure it "
                                               + "has an empty constructor", ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("The processor class cannot be accessed", ex);
        }
        return process;
    }

    private static class ImplThreadLocal<T> extends ThreadLocal<T> {
        private final Class<? extends T> type;
        private final String path;
        private final String[] extension;

        ImplThreadLocal(Class<? extends T> type, String[] extension) {
            this.type = type;
            this.extension = extension;
            this.path = null;
        }

        ImplThreadLocal(Class<? extends T> type, String path) {
            this.type = type;
            this.path = path;
            this.extension = null;
        }

        public String getPath() {
            return path;
        }

        public String[] getExtension() {
            return extension;
        }

        public Class<? extends T> getType() {
            return type;
        }

        @Override
        protected T initialValue() {
            try {
                T obj = type.newInstance();
                if (path != null) {
                    ((AssetLocator) obj).setRootPath(path);
                }
                return obj;
            } catch (InstantiationException | IllegalAccessException e) {
                System.err.println("Cannot create locator of type " + type.getName()
                                   + "does the class have an empty and public constructor?");
            }
            return null;
        }
    }
}
