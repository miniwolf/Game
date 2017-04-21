package mini.material;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes a miniMD (Material definition).
 *
 * @author miniwolf
 */
public class MaterialDef {
    private Map<String, MatParam> matParams = new HashMap<>();

    /**
     * Returns the material parameter with the given name.
     *
     * @param name The name of the parameter to retrieve
     *
     * @return The material parameter, or null if it does not exist.
     */
    public MatParam getMaterialParam(String name){
        return matParams.get(name);
    }
}
