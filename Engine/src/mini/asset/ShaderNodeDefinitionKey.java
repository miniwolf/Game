package mini.asset;

import mini.shaders.ShaderNodeDefinition;

import java.util.List;

public class ShaderNodeDefinitionKey extends AssetKey<List<ShaderNodeDefinition>> {
    private boolean loadDocumentation = false;

    /**
     * creates a ShaderNodeDefinitionKey
     *
     * @param name the name of the asset to load
     */
    public ShaderNodeDefinitionKey(String name) {
        super(name);
    }

    /**
     * creates a ShaderNodeDefinitionKey
     */
    public ShaderNodeDefinitionKey() {
        super();
    }

    /**
     * @return true if the asset loaded with this key will contain its
     * documentation
     */
    public boolean isLoadDocumentation() {
        return loadDocumentation;
    }

    /**
     * sets to true to load the documentation along with the
     * ShaderNodeDefinition
     *
     * @param loadDocumentation true to load the documentation along with the
     *                          ShaderNodeDefinition
     */
    public void setLoadDocumentation(boolean loadDocumentation) {
        this.loadDocumentation = loadDocumentation;
    }
}
