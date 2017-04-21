package mini.scene;

import mini.material.Material;
import mini.openglObjects.VAO;

public class Geometry extends Spatial {
    private Mesh mesh;
    private VAO vao;
    private Material material;

    /**
     * Create a geometry node without any mesh data.
     * Both the mesh and the material are null, the geometry
     * cannot be rendered until those are set.
     *
     * @param name The name of this geometry
     */
    public Geometry(String name) {
        super(name);
    }

    /**
     * Create a geometry node with mesh data.
     * The material of the geometry is null, it cannot
     * be rendered until it is set.
     *
     * @param name The name of this geometry
     * @param mesh The mesh data for this geometry
     */
    public Geometry(String name, Mesh mesh) {
        this(name);

        if (mesh == null) {
            throw new IllegalArgumentException("mesh cannot be null");
        }

        this.mesh = mesh;
    }

    /**
     * Sets the material to use for this geometry.
     *
     * @param material the material to use for this geometry
     */
    public void setMaterial(Material material) {
        this.material = material;
    }

    /**
     * Returns the material that is used for this geometry.
     *
     * @return the material that is used for this geometry
     * @see #setMaterial(Material)
     */
    public Material getMaterial() {
        return material;
    }

    public VAO getVao() {
        return vao;
    }

    public void delete() {
        vao.delete();
    }

    public Mesh getMesh() {
        return mesh;
    }
}
