package mini.asset;

import mini.scene.Spatial;

/**
 * Used to load model files, such as OBJ or Blender models.
 * This uses cloneable smart asset management, so that when all clones of
 * this model become unreachable, the original asset is purged from the cache,
 * allowing textures, materials, shaders, etc referenced by the model to
 * become collected.
 */
public class ModelKey extends AssetKey<Spatial> {
    public ModelKey(String name) {
        super(name);
    }

    public ModelKey() {
        super();
    }
}
