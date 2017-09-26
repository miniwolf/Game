package mini.shaders;

/**
 * Created by miniwolf on 16-04-2017.
 */
public class ShaderVariable {
    public static final int LOC_UNKNOWN = -2, LOC_NOT_DEFINED = -1;

    // if -2, location not known
    // if -1, not defined in shader
    // if >= 0, uniform defined and available.
    protected int location = LOC_UNKNOWN;

    /**
     * Name of the uniform as was declared in the shader.
     * E.g name = "g_WorldMatrix" if the declaration was
     * "uniform mat4 g_WorldMatrix;".
     */
    protected String name = null;

    /**
     * True if the shader value was changed.
     */
    protected boolean updateNeeded = true;

    public void setLocation(int location){
        this.location = location;
    }

    public int getLocation(){
        return location;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
