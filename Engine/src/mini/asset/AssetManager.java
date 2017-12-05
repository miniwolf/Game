package mini.asset;

import mini.asset.cache.AssetCache;
import mini.asset.plugins.ClasspathLocator;
import mini.material.Material;
import mini.material.plugins.MiniLoader;
import mini.material.plugins.ShaderNodeDefinitionLoader;
import mini.scene.plugins.MTLLoader;
import mini.scene.plugins.OBJLoader;
import mini.shaders.plugins.GLSLLoader;
import mini.textures.Texture;
import mini.textures.plugins.AWTLoader;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AssetManager {
    private ImplHandler handler = new ImplHandler(this);

    private List<AssetEventListener> eventListeners = new CopyOnWriteArrayList<>();

    public AssetManager() {
        registerLoader(OBJLoader.class, "obj");
        registerLoader(AWTLoader.class, "jpg", "png", "gif", "bmp", "jpeg");
        registerLoader(MiniLoader.class, "mini");
        registerLoader(MiniLoader.class, "minid");
        registerLoader(GLSLLoader.class, "frag", "vert", "glsl", "glsllib");
        registerLoader(ShaderNodeDefinitionLoader.class, "minisn");
        registerLoader(MTLLoader.class, "mtl");
        registerLocator(ClasspathLocator.class, "/");
    }

    public <T> T loadAsset(AssetKey<T> key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        AssetCache cache = handler.getCache(key.getCacheType());
        AssetProcessor processor = handler.getProcessor(key.getProcessorType());

        T obj = cache != null ? cache.getFromCache(key) : null;
        if (obj == null) {
            AssetInfo info = handler.tryLocate(key);
            if (info == null) {
                if (handler.getParentKey() != null) {
                    // Inform event listener that an asset has failed to load.
                    for (AssetEventListener listener : eventListeners) {
                        listener.assetDependencyNotFound(handler.getParentKey(), key);
                    }
                }
                throw new AssetNotFoundException(key.toString());
            }
            obj = loadLocatedAsset(key, info, processor, cache);
        }

        if (obj instanceof CloneableSmartAsset) {
            obj = registerAndCloneSmartAsset(key, obj, processor, cache);
        }

        return obj;
    }

    /**
     * Clones the asset using the given processor and registers the clone with the cache.
     *
     * @param key       The asset key
     * @param obj       The asset to clone / register, must implement {@link CloneableSmartAsset}.
     * @param processor The processor which will generate the clone, cannot be null.
     * @param cache     The cache to register the clone with, cannot be null.
     * @param <T>       The asset type.
     * @return The cloned asset, cannot be the same as the given asset since it's a clone.
     */
    private <T> T registerAndCloneSmartAsset(AssetKey<T> key, T obj, AssetProcessor processor,
                                             AssetCache cache) {
        if (processor == null) {
            throw new IllegalStateException("Asset implements CloneableSmartAsset but doesn't have"
                                            + "processor to handle cloning");
        } else {
            T clone = processor.createClone(obj);
            if (cache != null && clone != obj) {
                cache.registerAssetClone(key, clone);
            } else {
                throw new IllegalStateException("Asset implements CloneSmartAsset but doesn't have"
                                                + "cache or was not cloned");
            }
            return clone;
        }
    }

    public <T> T loadLocatedAsset(AssetKey<T> key, AssetInfo info, AssetProcessor processor,
                                  AssetCache cache) {
        AssetLoader<T> loader = handler.acquireLoader(key);
        T obj;
        try {
            handler.establishParentKey(key);
            obj = loader.load(info);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("An exception has occurred while loading asset: " + key);
        } finally {
            handler.releaseParentKey(key);
        }
        if (obj == null) {
            throw new RuntimeException("Error occurred while loading asset \"" + key + "\" using "
                                       + loader.getClass().getSimpleName());
        } else {
            // Do caching with type T
            if (cache != null) {
                cache.addToCache(key, obj);
            }
            return obj;
        }
    }

    public Object loadAsset(String name) {
        return loadAsset(new AssetKey<>(name));
    }

    public void registerLoader(Class<? extends AssetLoader> loaderClass, String... extensions) {
        handler.addLoader(loaderClass, extensions);
    }

    public void registerLocator(Class<? extends AssetLocator> locatorClass, String rootPath) {
        handler.addLocator(locatorClass, rootPath);
    }

    public Texture loadTexture(TextureKey textureKey) {
        return loadAsset(textureKey);
    }

    public Texture loadTexture(String name) {
        TextureKey key = new TextureKey(name, true);
        key.setGenerateMips(true);
        return loadTexture(key);
    }

    public Material loadMaterial(String name) {
        return loadAsset(new MaterialKey(name));
    }
}
