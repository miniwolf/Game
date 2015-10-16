package rendering;

import texture.ModelTexture;

import java.util.HashMap;
import java.util.Map;

/**
 * @author miniwolf
 */
public class Material {
    private Map<String, ModelTexture> textureMap;

    public Material() {
        textureMap = new HashMap<>();
    }

    public void addTexture(String name, ModelTexture texture) {
        textureMap.put(name, texture);
    }

    public ModelTexture getTexture(String name) {
        return textureMap.get(name);
    }
}
