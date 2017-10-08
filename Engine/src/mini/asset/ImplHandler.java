package mini.asset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the asset loader and asset locator implementations. This is done by keeping an instance
 * of each asset loader in a thread local.
 */
public class ImplHandler {
    private Map<String, ImplThreadLocal<? extends AssetLoader>> extensionsToLoaderMap
            = new HashMap<>();
    private List<ImplThreadLocal<AssetLocator>> locatorList = new ArrayList<>();
    private AssetManager assetManager;

    public ImplHandler(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public <T> AssetLoader<T> acquireLoader(AssetKey<T> key) {
        ImplThreadLocal<? extends AssetLoader> local = extensionsToLoaderMap
                .get(key.getExtension());
        return local.get();
    }

    public void addLoader(Class<? extends AssetLoader> loaderType, String... extensions) {
        ImplThreadLocal<? extends AssetLoader> local = new ImplThreadLocal<>(loaderType,
                                                                             extensions);
        for (String extension : extensions) {
            extension = extension.toLowerCase();
            extensionsToLoaderMap.put(extension, local);
        }
    }

    public void addLocator(Class<? extends AssetLocator> locatorType, String rootPath) {
        ImplThreadLocal<AssetLocator> implThreadLocal = new ImplThreadLocal<>(locatorType,
                                                                              rootPath);
        locatorList.add(implThreadLocal);
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

        return null;
    }

    class ImplThreadLocal<T> extends ThreadLocal<T> {
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

        @Override
        protected T initialValue() {
            try {
                return type.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                System.err.println("Cannot create locator of type " + type.getName()
                                   + "does the class have an empty and public constructor?");
            }
            return null;
        }
    }
}
