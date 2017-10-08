package mini.asset;

import mini.material.Material;

/**
 * Used for loading {@link Material materials} only (not material definitions!).
 * Material instances use cloneable smart asset management so that they and any
 * referenced textures will be collected when all instances of the material
 * become unreachable.
 */
public class MaterialKey extends AssetKey<Material> {

    public MaterialKey(String name) {
        super(name);
    }

    public MaterialKey() {
        super();
    }
}
