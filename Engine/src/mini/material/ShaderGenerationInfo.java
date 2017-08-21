package mini.material;

import mini.shaders.ShaderNodeVariable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * this class is basically a struct that contains the ShaderNodes informations
 * in an appropriate way to ease the shader generation process and make it
 * faster.
 *
 * @author Nehon
 */
public class ShaderGenerationInfo implements Cloneable {

    /**
     * the list of attributes of the vertex shader
     */
    protected List<ShaderNodeVariable> attributes = new ArrayList<>();
    /**
     * the list of all the uniforms to declare in the vertex shader
     */
    protected List<ShaderNodeVariable> vertexUniforms = new ArrayList<>();
    /**
     * the global output of the vertex shader (to assign ot gl_Position)
     */
    protected ShaderNodeVariable vertexGlobal = null;
    /**
     * the list of varyings
     */
    protected List<ShaderNodeVariable> varyings = new ArrayList<>();
    /**
     * the list of all the uniforms to declare in the fragment shader
     */
    protected List<ShaderNodeVariable> fragmentUniforms = new ArrayList<>();
    /**
     * the list of all the fragment shader global outputs (to assign ot gl_FragColor or gl_Fragdata[n])
     */
    protected List<ShaderNodeVariable> fragmentGlobals = new ArrayList<>();
    /**
     * the unused node names of this shader (node whose output are never used)
     */
    protected List<String> unusedNodes = new ArrayList<>();

    /**
     * @return the attributes
     */
    public List<ShaderNodeVariable> getAttributes() {
        return attributes;
    }

    /**
     * @return the vertex shader uniforms
     */
    public List<ShaderNodeVariable> getVertexUniforms() {
        return vertexUniforms;
    }

    /**
     * @return the fragment shader uniforms
     */
    public List<ShaderNodeVariable> getFragmentUniforms() {
        return fragmentUniforms;
    }

    /**
     * @return the vertex shader global ouput
     */
    public ShaderNodeVariable getVertexGlobal() {
        return vertexGlobal;
    }

    /**
     * @return the fragment shader global outputs
     */
    public List<ShaderNodeVariable> getFragmentGlobals() {
        return fragmentGlobals;
    }

    /**
     * @return the varyings
     */
    public List<ShaderNodeVariable> getVaryings() {
        return varyings;
    }

    /**
     * sets the vertex shader global output
     *
     * @param vertexGlobal the global output
     */
    public void setVertexGlobal(ShaderNodeVariable vertexGlobal) {
        this.vertexGlobal = vertexGlobal;
    }

    /**
     * @return the list on unused node names
     */
    public List<String> getUnusedNodes() {
        return unusedNodes;
    }

    /**
     * the list of unused node names
     *
     * @param unusedNodes
     */
    public void setUnusedNodes(List<String> unusedNodes) {
        this.unusedNodes = unusedNodes;
    }

    /**
     * convenient toString method
     *
     * @return the informations
     */
    @Override
    public String toString() {
        return "ShaderGenerationInfo{" + "attributes=" + attributes + ", vertexUniforms="
               + vertexUniforms + ", vertexGlobal=" + vertexGlobal + ", varyings=" + varyings
               + ", fragmentUniforms=" + fragmentUniforms + ", fragmentGlobals=" + fragmentGlobals
               + '}';
    }

    @Override
    protected ShaderGenerationInfo clone() throws CloneNotSupportedException {
        ShaderGenerationInfo clone = (ShaderGenerationInfo) super.clone();

        for (ShaderNodeVariable attribute : attributes) {
            clone.attributes.add(attribute.clone());
        }

        for (ShaderNodeVariable uniform : vertexUniforms) {
            clone.vertexUniforms.add(uniform.clone());
        }

        clone.vertexGlobal = vertexGlobal.clone();

        for (ShaderNodeVariable varying : varyings) {
            clone.varyings.add(varying.clone());
        }

        for (ShaderNodeVariable uniform : fragmentUniforms) {
            clone.fragmentUniforms.add(uniform.clone());
        }

        for (ShaderNodeVariable globals : fragmentGlobals) {
            clone.fragmentGlobals.add(globals.clone());
        }

        clone.unusedNodes.addAll(unusedNodes);

        return clone;
    }
}
