package mini.material;

import mini.shaders.VarType;
import mini.textures.image.ColorSpace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a miniMD (Material definition).
 *
 * @author miniwolf
 */
public class MaterialDef {
    private Map<String, MatParam> matParams = new HashMap<>();
    private Map<String, List<TechniqueDef>> techniques = new HashMap<>();
    private String name;
    private String assetName;

    /**
     * Creates a new material definition with the given name.
     *
     * @param name The debug name of the material definition
     */
    public MaterialDef(String name) {
        this.name = name;
    }

    /**
     * Returns the asset key name of the asset from which this material
     * definition was loaded.
     *
     * @return Asset key name of the j3md file
     */
    public String getAssetName() {
        return assetName;
    }

    /**
     * Set the asset key name.
     *
     * @param assetName the asset key name
     */
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    /**
     * Returns the material parameter with the given name.
     *
     * @param name The name of the parameter to retrieve
     * @return The material parameter, or null if it does not exist.
     */
    public MatParam getMaterialParam(String name) {
        return matParams.get(name);
    }

    /**
     * Returns a collection of all material parameters declared in this
     * material definition.
     * <p>
     * Modifying the material parameters or the collection will lead
     * to undefined results.
     *
     * @return All material parameters declared in this definition.
     */
    public Collection<MatParam> getMaterialParams() {
        return matParams.values();
    }

    /**
     * The debug name of the material definition.
     *
     * @return debug name of the material definition.
     */
    public String getName() {
        return name;
    }

    /**
     * Adds a new material parameter.
     *
     * @param type  Type of the parameter
     * @param name  Name of the parameter
     * @param value Default value of the parameter
     */
    public void addMaterialParam(VarType type, String name, Object value) {
        matParams.put(name, new MatParam(type, name, value));
    }

    /**
     * Adds a new technique definition to this material definition.
     *
     * @param technique The technique definition to add.
     */
    public void addTechniqueDef(TechniqueDef technique) {
        List<TechniqueDef> list = techniques
                .computeIfAbsent(technique.getName(), k -> new ArrayList<>());
        list.add(technique);
    }

    /**
     * Returns technique definitions with the given name.
     *
     * @param name The name of the technique definitions to find
     * @return The technique definitions, or null if cannot be found.
     */
    public List<TechniqueDef> getTechniqueDefs(String name) {
        return techniques.get(name);
    }

    /**
     * Adds a new material parameter.
     *
     * @param type       Type of the parameter
     * @param name       Name of the parameter
     * @param value      Default value of the parameter
     * @param ffBinding  Fixed function binding for the parameter
     * @param colorSpace the color space of the texture required by thiis texture param
     * @see ColorSpace
     */
    public void addMaterialParamTexture(VarType type, String name, ColorSpace colorSpace) {
        matParams.put(name, new MatParamTexture(type, name, null, colorSpace));
    }
}
