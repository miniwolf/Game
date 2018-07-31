package mini.asset;

import mini.asset.cache.AssetCache;
import mini.font.BitmapFont;
import mini.material.Material;
import mini.scene.Spatial;
import mini.system.ApplicationSystem;
import mini.textures.Texture;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AssetManager {
    private ImplHandler handler = new ImplHandler(this);

    private List<AssetEventListener> eventListeners = new CopyOnWriteArrayList<>();
    private List<ClassLoader> classLoaders = Collections.synchronizedList(new ArrayList<>());

    public AssetManager() {
        this(null);
    }

    public AssetManager(boolean usePlatformConfig) {
        this(usePlatformConfig ? ApplicationSystem.getPlatformAssetConfigURL() : null);
    }

    public AssetManager(URL configFile) {
        if (configFile != null) {
            loadConfigFile(configFile);
        }
    }

    private void loadConfigFile(URL configFile) {
        try {
            AssetConfig.loadText(this, configFile);
        } catch (IOException e) {
            System.err.println("Severe: Failed to load asset config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load an asset from a key, the asset will be located by one of the {@link AssetLocator}
     * implementations provided in the {@link AssetManager#registerLocator(Class, String)} call.
     * If located sucessfully, it will be loaded via the appropriate {@link AssetLoader}
     * implementation based on the file's extension, as specified in the call
     * {@link AssetManager#registerLoader(Class, String...)}.
     *
     * @param key The AssetKey
     * @param <T> The object type that will be loaded from the AssetKey instance.
     * @return The loaded asset.
     * @throws AssetNotFoundException If all registered locators have failed to locate the asset.
     * @throws AssetLoadException     If the {@link AssetLoader} has failed to load the asset due to an
     *                                {@link IOException} or another error.
     */
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
            T clone = (T) processor.createClone(obj);
            if (cache != null && clone != obj) {
                cache.registerAssetClone(key, clone);
            } else {
                throw new IllegalStateException("Asset implements CloneSmartAsset but doesn't have"
                                                + "cache or was not cloned");
            }
            return clone;
        }
    }

    private <T> T loadLocatedAsset(AssetKey<T> key, AssetInfo info, AssetProcessor processor,
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
            if (processor != null) {
                obj = processor.postProcess(key, obj);
            }

            // Do caching with type T
            if (cache != null) {
                cache.addToCache(key, obj);
            }

            for (AssetEventListener listener : eventListeners) {
                listener.assetLoaded(key);
            }
            return obj;
        }
    }

    /**
     * Load an asset by name, calling this method is the same as calling
     * {@link #loadAsset(AssetKey)}
     *
     * @param name The name of the asset to load.
     * @return The loaded asset, or null if failed to be loaded.
     * @see AssetManager#loadAsset(AssetKey)
     */
    public Object loadAsset(String name) {
        return loadAsset(new AssetKey<>(name));
    }

    /**
     * Adds a {@link ClassLoader} that is used to load {@link Class classes} that are needed for
     * finding and loading Assets.
     * This does <strong>not</strong> allow loading assets from that classpath, use registerLocator
     * for that.
     *
     * @param loader A ClassLoader that Classes in asset files can be loaded from.
     */
    public void addClassLoader(ClassLoader loader) {
        classLoaders.add(loader);
    }

    /**
     * Remove a {@link ClassLoader} from the list of registered ClassLoaders
     */
    public void removeClassLoader(ClassLoader loader) {
        classLoaders.remove(loader);
    }

    /**
     * Add an {@link AssetEventListener} to receive events from this <code>AssetManager</code>.
     *
     * @param listener The asset event listener to add
     */
    public void addAssetEventListener(AssetEventListener listener) {
        eventListeners.add(listener);
    }

    /**
     * Remove an {@link AssetEventListener} from receiving events from this <code>AssetManager</code>.
     *
     * @param listener The asset event listener to remove
     */
    public void removeAssetEventListener(AssetEventListener listener) {
        eventListeners.remove(listener);
    }

    /**
     * Register an {@link AssetLoader} by using a class object.
     *
     * @param loaderClass The loader class to register.
     * @param extensions  Which extensions this loader is reponsible for loading, if there are
     *                    already other loaders registered for that extension, they will be
     *                    overridden - there should be one loader for each extension.
     */
    public void registerLoader(Class<? extends AssetLoader> loaderClass, String... extensions) {
        handler.addLoader(loaderClass, extensions);
    }

    /**
     * Registers the given locator class for locating assets with this <code>AssetManager</code>.
     * {@link AssetLocator}s are invoked in the order they were registered, to locate the asset by
     * the {@link AssetKey}. Once an {@link AssetLocator} returns a non-null {@link AssetInfo}, it
     * is sent to the {@link AssetLoader} to load the asset.
     * Once a locator is registered, it can be removed via {@link #unregisterLocator(String, Class)}
     *
     * @param locatorClass The class type of the {@link AssetLocator} to register
     * @param rootPath     Specifies the root path from which to locate assets for the given
     *                     {@link AssetLocator}. The purpose of this parameter depends on the type of the
     *                     {@link AssetLocator}.
     * @see AssetLocator#setRootPath(String)
     * @see AssetLocator#locate(AssetManager, AssetKey)
     * @see #unregisterLocator(String, Class)
     */
    public void registerLocator(Class<? extends AssetLocator> locatorClass, String rootPath) {
        handler.addLocator(locatorClass, rootPath);
    }

    public void unregisterLocator(Class<? extends AssetLocator> locatorClass, String rootPath) {
        handler.removeLocator(locatorClass, rootPath);
    }

    /**
     * Loads texture file, supported types are BMP, JPG, PNG, GIF, TGA
     *
     * @param textureKey The {@link TextureKey} to use for loading.
     * @return The loaded texture, or null if failed to be loaded.
     */
    public Texture loadTexture(TextureKey textureKey) {
        return loadAsset(textureKey);
    }

    /**
     * Load texture file, supported types are BMP, JPG, PNG, GIF, TGA
     * <p>
     * The texture will be loaded with mip-mapping enabled.
     *
     * @param name The name of the texture to load.
     * @return The texture that was loaded
     * @see AssetManager#loadTexture(TextureKey)
     */
    public Texture loadTexture(String name) {
        TextureKey key = new TextureKey(name, true);
        key.setGenerateMips(true);
        return loadTexture(key);
    }

    /**
     * Load a material instance (mini) file.
     *
     * @param name Asset name of the material to load
     * @return The material that was loaded
     * @see AssetManager#loadAsset(AssetKey)
     */
    public Material loadMaterial(String name) {
        return loadAsset(new MaterialKey(name));
    }

    /**
     * Load a font file. Font files are in AngelCode text format, and are with the extension "fnt".
     *
     * @param name Asset name of the font to load
     * @return The font loaded
     */
    public BitmapFont loadFont(String name) {
        return loadAsset(new AssetKey<>(name));
    }

    /**
     * Loads a 3D model. Models can be OBJ files.
     *
     * @param name Asset name of the model to load
     * @return The model that was loaded
     */
    public <T extends Spatial> T loadModel(
            String name) { // TODO: Can we generify this to avoid the unchecked cast?
        return (T) (loadAsset(new ModelKey(name)));
    }

    public AssetInfo locateAsset(AssetKey<?> key) {
        AssetInfo info = handler.tryLocate(key);
        if (info == null) {
            System.err.println("Warning: Cannot locate resource: " + key);
        }
        return info;
    }

    public void clearCache() {
        handler.clearCache();
    }
}
