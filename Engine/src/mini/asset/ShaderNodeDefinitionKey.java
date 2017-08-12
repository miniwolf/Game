package mini.asset;

import mini.utils.MyFile;

public class ShaderNodeDefinitionKey extends AssetKey {
    private boolean loadDocumentation = false;

    /**
     * creates a ShaderNodeDefinitionKey
     *
     * @param name the name of the asset to load
     */
    public ShaderNodeDefinitionKey(String name) {
        super(new MyFile(name));
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
