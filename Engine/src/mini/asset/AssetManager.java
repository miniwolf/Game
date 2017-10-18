package mini.asset;

import mini.asset.plugins.ClasspathLocator;
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

    public <T> T loadAsset(AssetKey<T> key) {
        AssetInfo info = handler.tryLocate(key);
        return loadLocatedAsset(key,
                                info); // TODO: Add to cache that we can later retrieve things from
    }

    public <T> T loadLocatedAsset(AssetKey<T> key, AssetInfo info) {
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

    public Spatial loadModel(String name) {
        return loadAsset(new ModelKey(name));
    }
}