package mini.scene.plugins.fbx;

import mini.scene.plugins.fbx.file.FBXElement;

public interface FBXElementLoader<T> {
    /**
     * Load FBX type from the FBX elements.
     * The {@link FBXElement} can be loaded by use of {@link FBXLoader}
     *
     * @param element
     * @return
     */
    T load(FBXElement element);
}
