package mini.scene;

import mini.material.Material;
import mini.math.Matrix4f;
import mini.openglObjects.VAO;

public class Geometry extends Spatial {
    private Mesh mesh;
    private VAO vao;
    private Material material;
    protected transient Matrix4f cachedWorldMat = new Matrix4f();

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
     * Returns this geometry's mesh vertex count.
     *
     * @return this geometry's mesh vertex count.
     * @see Mesh#getVertexCount()
     */
    @Override
    public int getVertexCount() {
        return mesh.getVertexCount();
    }

    /**
     * Returns this geometry's mesh triangle count.
     *
     * @return this geometry's mesh triangle count.
     * @see Mesh#getTriangleCount()
     */
    @Override
    public int getTriangleCount() {
        return mesh.getTriangleCount();
    }

    /**
     * A {@link Matrix4f matrix} that transforms the {@link Geometry#getMesh() mesh}
     * from model space to world space. This matrix is computed based on the
     * {@link Geometry#getWorldTransform() world transform} of this geometry.
     * In order to receive updated values, you must call {@link Geometry#computeWorldMatrix() }
     * before using this method.
     *
     * @return Matrix to transform from local space to world space
     */
    public Matrix4f getWorldMatrix() {
        return cachedWorldMat;
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
