package mini.material;

import mini.shaders.VarType;

/**
 * <code>MatParamOverride</code> is a mechanism by which
 * {@link MatParam material parameters} can be overridden on the scene graph.
 * <p>
 * A scene branch which has a <code>MatParamOverride</code> applied to it will
 * cause all material parameters with the same name and type to have their value
 * replaced with the value set on the <code>MatParamOverride</code>. If those
 * parameters are mapped to a define, then the define will be overridden as well
 * using the same rules as the ones used for regular material parameters.
 * <p>
 * <code>MatParamOverrides</code> are applied to a {@link Spatial} via the
 * {@link Spatial#addMatParamOverride(mini.material.MatParamOverride)}
 * method. They are propagated to child <code>Spatials</code> via
 * {@link Spatial#updateGeometricState()} similar to how lights are propagated.
 * <p>
 * Example:<br>
 * <pre>
 * {@code
 *
 * Geometry box = new Geometry("Box", new Box(1,1,1));
 * Material mat = new Material(assetManager, "MatDefs/Misc/Unshaded.j3md");
 * mat.setColor("Color", ColorRGBA.Blue);
 * box.setMaterial(mat);
 * rootNode.attachChild(box);
 *
 * // ... later ...
 * MatParamOverride override = new MatParamOverride(Type.Vector4f, "Color", ColorRGBA.Red);
 * rootNode.addMatParamOverride(override);
 *
 * // After adding the override to the root node, the box becomes red.
 * }
 * </pre>
 *
 * @author miniwolf
 * @see Spatial#addMatParamOverride(MatParamOverride)
 * @see Spatial#getWorldMatParamOverrides()
 */
public class MatParamOverride extends MatParam {
    private boolean enabled = true;
    /**
     * Create a new <code>MatParamOverride</code>.
     *
     * Overrides are created enabled by default.
     *
     * @param type The type of parameter.
     * @param name The name of the parameter.
     * @param value The value to set the material parameter to.
     */
    public MatParamOverride(VarType type, String name, Object value) {
        super(type, name, value);
    }

    /**
     * Determine if the <code>MatParamOverride</code> is enabled or disabled.
     *
     * @return true if enabled, false if disabled.
     * @see #setEnabled(boolean)
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable this <code>MatParamOverride</code>.
     *
     * When disabled, the override will continue to propagate through the scene
     * graph like before, but it will have no effect on materials. Overrides are
     * enabled by default.
     *
     * @param enabled Whether to enable or disable this override.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
