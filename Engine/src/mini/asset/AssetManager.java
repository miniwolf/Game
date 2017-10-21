package mini.asset;

import mini.asset.plugins.ClasspathLocator;
import mini.material.Material;
import mini.material.plugins.MiniLoader;
import mini.material.plugins.ShaderNodeDefinitionLoader;
import mini.scene.Spatial;
import mini.scene.plugins.MTLLoader;
import mini.scene.plugins.OBJLoader;
import mini.scene.plugins.fbx.FBXLoader;
import mini.shaders.plugins.GLSLLoader;
import mini.textures.Texture;
import mini.textures.plugins.AWTLoader;

import java.io.IOException;

public class AssetManager {
    private ImplHandler handler = new ImplHandler(this);

    public AssetManager() {
        registerLoader(OBJLoader.class, "obj");
        registerLoader(MiniLoader.class, "mini", "minid");
        registerLoader(GLSLLoader.class, "frag", "vert");
        registerLoader(ShaderNodeDefinitionLoader.class, "minisn");
        registerLoader(MTLLoader.class, "mtl");
        registerLoader(FBXLoader.class, "fbx", "FBX");
        registerLoader(AWTLoader.class, "png", "jpg");
        registerLocator(ClasspathLocator.class, "/");
    }

    private <T> T loadLocatedAsset(AssetKey<T> key, AssetInfo info) {
        AssetLoader<T> loader = handler.acquireLoader(key);
        T obj;
        try {
            obj = loader.load(info);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("An exception has occurred while loading asset: " + key);
        }
        return obj;
    }

    /**
     * Load an asset from a key, the asset will be located by one of the {@link AssetLocator}
     * implementations provided in the {@link #registerLocator(Class, String)} call. If located
     * successfully, it will be loaded via the appropriate {@link AssetLocator} implementation based
     * on the file's extension as specified in the call {@link #registerLoader(Class, String...)}.
     *
     * @param key The AssetKey
     * @param <T> The object type that will be loaded from the AssetKey instance.
     * @return The loaded asset
     */
    public <T> T loadAsset(AssetKey<T> key) {
        AssetInfo info = handler.tryLocate(key);
        // TODO: Add to cache that we can later retrieve things from
        return loadLocatedAsset(key, info);
    }

    /**
     * Load an asset by name, calling this method is the same as calling
     * <code>loadAsset(new AssetKey(name))</code>.
     *
     * @param name The name of the asset to load.
     * @return The loaded asset, or null if failed to be loaded.
     */
    public Object loadAsset(String name) {
        return loadAsset(new AssetKey<>(name));
    }

    /**
     * Loads texture file, supported types are BMP, JPG, PNG, GIF
     *
     * @param key The {@link TextureKey} to use for loading.
     * @return The loaded texture, or null if failed to be loaded.
     * @see AssetManager#loadAsset(mini.asset.AssetKey)
     */
    public Texture loadTexture(TextureKey key) {
        return loadAsset(key);
    }

    /**
     * Loads texture file, supported types are BMP, JPG, PNG, GIF
     *
     * @param name The name of the texture to load.
     * @return The texture that was loaded
     * @see AssetManager#loadAsset(mini.asset.AssetKey)
     */
    public Texture loadTexture(String name) {
        TextureKey key = new TextureKey(name, true);
        key.setGenerateMips(true);
        return loadTexture(key);
    }

    /**
     * Loads a 3D model. Models can be FBX and OBJ files.
     *
     * @param name Asset name of the model to load
     * @return The model that was loaded
     * @see AssetManager#loadAsset(mini.asset.AssetKey)
     */
    public Spatial loadModel(String name) {
        return loadAsset(new ModelKey(name));
    }

    /**
     * Registers the given locator class for locating assets with this <code>AssetManager</code>.
     * {@link AssetLocator}s are invoked in the order they were registered, to locate the asset by
     * the {@link AssetKey}. Once an {@link AssetLocator} returns a non-null AssetInfo, it is sent
     * to the {@link AssetLoader} to load the asset.
     * Once a locator is registered, it can be removed via {@link #unregisterLocator(Class, String)}.
     *
     * @param locatorClass The class type of the {@link AssetLocator} to register.
     * @param rootPath     Specifies the root path from which to locate assets for the given {@link AssetLocator}.
     *                     The purpose of this parameter depends on the type of the {@link AssetLocator}.
     */
    public void registerLocator(Class<? extends AssetLocator> locatorClass, String rootPath) {
        handler.addLocator(locatorClass, rootPath);
    }

    /**
     * Unregisters the given locator class. This essentially undoes the operation done by
     * {@link #registerLocator(Class, String)}.
     *
     * @param locatorClass The locator class to unregister.
     * @param rootPath     Should be the name as the root path specified in {@link #registerLocator(Class, String)}.
     * @see #registerLocator(Class, String)
     */
    public void unregisterLocator(Class<? extends AssetLocator> locatorClass, String rootPath) {
        handler.removeLocator(locatorClass, rootPath);
    }

    /**
     * Register an {@link AssetLoader} by using a class object.
     *
     * @param loaderClass The loader class to register.
     * @param extensions  Which extensions this loader is responsible for loading, if there are
     *                    already other loaders registered for that extension, they will be
     *                    overwritten - there should only be one loader for each extension.
     */
    public void registerLoader(Class<? extends AssetLoader> loaderClass, String... extensions) {
        handler.addLoader(loaderClass, extensions);
    }

    /**
     * Load a material instance (mini) file.
     *
     * @param name Asset name of the material to load
     * @return The material that was loaded
     * @see #loadAsset(AssetKey)
     */
    public Material loadMaterial(String name) {
        return loadAsset(new MaterialKey(name));
    }
}
