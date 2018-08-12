package mini.scene;

import mini.bounding.BoundingSphere;
import mini.bounding.BoundingVolume;
import mini.collision.Collidable;
import mini.collision.CollisionResults;
import mini.material.Material;
import mini.math.Matrix4f;
import mini.renderer.Camera;
import mini.utils.TempVars;
import mini.utils.clone.Cloner;

/**
 * <code>Geometry</code> defines a leaf node of the scene graph. The leaf node
 * contains the geometric data for rendering objects. It manages all rendering
 * information such as a {@link Material} object to define how the surface
 * should be shaded and the {@link Mesh} data to contain the actual geometry.
 */
public class Geometry extends Spatial {

    // Version #1: removed shared meshes.
    // models loaded with shared mesh will be automatically fixed.
    public static final int SAVABLE_VERSION = 1;
    protected Mesh mesh;
    protected transient int lodLevel = 0;
    protected Material material;
    /**
     * When true, the geometry's transform will not be applied.
     */
    protected boolean ignoreTransform = false;
    protected transient Matrix4f cachedWorldMat = new Matrix4f();

    /**
     * Specifies which {@link GeometryGroupNode} this <code>Geometry</code>
     * is managed by.
     */
    protected GeometryGroupNode groupNode;

    /**
     * The start index of this <code>Geometry's</code> inside
     * the {@link GeometryGroupNode}.
     */
    protected int startIndex = -1;

    /**
     * Serialization only. Do not use.
     */
    public Geometry() {
        this(null);
    }

    /**
     * Create a geometry node without any mesh data.
     * Both the mesh and the material are null, the geometry
     * cannot be rendered until those are set.
     *
     * @param name The name of this geometry
     */
    public Geometry(String name) {
        super(name);

        // For backwards compatibility, only clear the "requires
        // update" flag if we are not a subclass of Geometry.
        // This prevents subclass from silently failing to receive
        // updates when they upgrade.
        setRequiresUpdates(Geometry.class != getClass());
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

    @Override
    public boolean checkCulling(Camera cam) {
        if (isGrouped()) {
            setLastFrustumIntersection(Camera.FrustumIntersect.Outside);
            return false;
        }
        return super.checkCulling(cam);
    }

    /**
     * @return If ignoreTransform mode is set.
     * @see Geometry#setIgnoreTransform(boolean)
     */
    public boolean isIgnoreTransform() {
        return ignoreTransform;
    }

    /**
     * @param ignoreTransform If true, the geometry's transform will not be applied.
     */
    public void setIgnoreTransform(boolean ignoreTransform) {
        this.ignoreTransform = ignoreTransform;
    }

    /**
     * Sets the LOD level to use when rendering the mesh of this geometry.
     * Level 0 indicates that the default index buffer should be used,
     * levels [1, LodLevels + 1] represent the levels set on the mesh
     * with {@link Mesh#setLodLevels(mini.scene.VertexBuffer[]) }.
     *
     * @param lod The lod level to set
     */
    @Override
    public void setLodLevel(int lod) {
        if (mesh.getNumLodLevels() == 0) {
            throw new IllegalStateException("LOD levels are not set on this mesh");
        }

        if (lod < 0 || lod >= mesh.getNumLodLevels()) {
            throw new IllegalArgumentException("LOD level is out of range: " + lod);
        }

        lodLevel = lod;

        if (isGrouped()) {
            groupNode.onMeshChange(this);
        }
    }

    /**
     * Returns the LOD level set with {@link #setLodLevel(int) }.
     *
     * @return the LOD level set
     */
    public int getLodLevel() {
        return lodLevel;
    }

    /**
     * Returns this geometry's mesh vertex count.
     *
     * @return this geometry's mesh vertex count.
     * @see Mesh#getVertexCount()
     */
    public int getVertexCount() {
        return mesh.getVertexCount();
    }

    /**
     * Returns this geometry's mesh triangle count.
     *
     * @return this geometry's mesh triangle count.
     * @see Mesh#getTriangleCount()
     */
    public int getTriangleCount() {
        return mesh.getTriangleCount();
    }

    /**
     * Sets the mesh to use for this geometry when rendering.
     *
     * @param mesh the mesh to use for this geometry
     * @throws IllegalArgumentException If mesh is null
     */
    public void setMesh(Mesh mesh) {
        if (mesh == null) {
            throw new IllegalArgumentException();
        }

        this.mesh = mesh;
        setBoundRefresh();

        if (isGrouped()) {
            groupNode.onMeshChange(this);
        }
    }

    /**
     * Returns the mesh to use for this geometry
     *
     * @return the mesh to use for this geometry
     * @see #setMesh(mini.scene.Mesh)
     */
    public Mesh getMesh() {
        return mesh;
    }

    /**
     * Sets the material to use for this geometry.
     *
     * @param material the material to use for this geometry
     */
    @Override
    public void setMaterial(Material material) {
        this.material = material;

        if (isGrouped()) {
            groupNode.onMaterialChange(this);
        }
    }

    /**
     * Returns the material that is used for this geometry.
     *
     * @return the material that is used for this geometry
     * @see #setMaterial(mini.material.Material)
     */
    public Material getMaterial() {
        return material;
    }

    @Override
    //TODO: Documentation
    public void updateModelBound() {
        mesh.updateBound();
        setBoundRefresh();
    }

    /**
     * <code>updateWorldBound</code> updates the bounding volume that contains
     * this geometry. The location of the geometry is based on the location of
     * all this node's parents.
     *
     * @see Spatial#updateWorldBound()
     */
    @Override
    protected void updateWorldBound() {
        super.updateWorldBound();
        if (mesh == null) {
            throw new NullPointerException("Geometry: " + getName() + " has null mesh");
        }

        if (mesh.getBound() != null) {
            if (ignoreTransform) {
                // we do not transform the model bound by the world transform,
                // just use the model bound as-is
                worldBound = mesh.getBound().clone(worldBound);
            } else {
                worldBound = mesh.getBound().transform(worldTransform, worldBound);
            }
        }
    }

    @Override
    protected void updateWorldTransforms() {
        super.updateWorldTransforms();
        computeWorldMatrix();

        if (isGrouped()) {
            groupNode.onTransformChange(this);
        }

        // geometry requires lights to be sorted
        worldLights.sort(true);
    }

    @Override
    protected void updateWorldLightList() {
        super.updateWorldLightList();
        // geometry requires lights to be sorted
        worldLights.sort(true);
    }

    /**
     * Removes the {@link GeometryGroupNode} association from this
     * <code>Geometry</code>.
     * <p>
     * Should only be called by the parent {@link GeometryGroupNode}.
     */
    public void unassociateFromGroupNode() {
        if (groupNode != null) {
            // Once the geometry is removed
            // from the parent, the group node needs to be updated.
            groupNode.onGeometryUnassociated(this);
            groupNode = null;

            // change the default to -1 to make error detection easier
            startIndex = -1;
        }
    }

    @Override
    public boolean removeFromParent() {
        return super.removeFromParent();
    }

    @Override
    protected void setParent(Node parent) {
        super.setParent(parent);

        // If the geometry is managed by group node we need to unassociate.
        if (parent == null && isGrouped()) {
            unassociateFromGroupNode();
        }
    }

    /**
     * Recomputes the matrix returned by {@link Geometry#getWorldMatrix() }.
     * This will require a localized transform update for this geometry.
     */
    public void computeWorldMatrix() {
        // Force a local update of the geometry's transform
        checkDoTransformUpdate();

        // Compute the cached world matrix
        cachedWorldMat.loadIdentity();
        cachedWorldMat.setRotationQuaternion(worldTransform.getRotation());
        cachedWorldMat.setTranslation(worldTransform.getTranslation());

        TempVars vars = TempVars.get();
        Matrix4f scaleMat = vars.tempMat4;
        scaleMat.loadIdentity();
        scaleMat.scale(worldTransform.getScale());
        cachedWorldMat.multLocal(scaleMat);
        vars.release();
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

    @Override
    public int collideWith(Collidable other, CollisionResults results) {
        // Force bound to update
        checkDoBoundUpdate();
        // Update transform, and compute cache world matrix
        computeWorldMatrix();

        assert (refreshFlags & (RF_BOUND | RF_TRANSFORM)) == 0;

        if (mesh == null) {
            return 0;
        }

        int prevSize = results.size();
        int added = mesh.collideWith(other, cachedWorldMat, worldBound, results);
        int newSize = results.size();
        for (int i = prevSize; i < newSize; i++) {
            results.getCollisionDirect(i).setGeometry(this);
        }
        return added;
    }

    @Override
    public void depthFirstTraversal(SceneGraphVisitor visitor, DFSMode mode) {
        visitor.visit(this);
    }

    /**
     * Determine whether this <code>Geometry</code> is managed by a
     * {@link GeometryGroupNode} or not.
     *
     * @return True if managed by a {@link GeometryGroupNode}.
     */
    public boolean isGrouped() {
        return groupNode != null;
    }

    /**
     * @deprecated Use {@link #isGrouped()} instead.
     */
    @Deprecated
    public boolean isBatched() {
        return isGrouped();
    }

    /**
     * This version of clone is a shallow clone, in other words, the same mesh is referenced as the
     * original geometry.
     * Exception: if the mesh is marked as being a software animated mesh, (bind pose is set) then
     * the positions and normals are deep copied.
     */
    @Override
    public Geometry clone(boolean cloneMaterial) {
        return (Geometry) super.clone(cloneMaterial);
    }

    /**
     * This version of clone is a shallow clone, in other words, the same mesh is referenced as the
     * original geometry.
     * Exception: if the mesh is marked as being a software animated mesh, (bind pose is set) then
     * the positions and normals are deep copied.
     */
    @Override
    public Geometry clone() {
        return clone(true);
    }

    /**
     * Create a deep clone of the geometry. This creates an identical copy of
     * the mesh with the vertex buffer data duplicated.
     */
    @Override
    public Spatial deepClone() {
        return super.deepClone();
    }

    /**
     * Called internally by mini.utils.clone.Cloner. Do not call directly.
     */
    @Override
    public void cloneFields(Cloner cloner, Object original) {
        super.cloneFields(cloner, original);

        if (groupNode != null) {
            if (cloner.isCloned(groupNode)) {
                this.groupNode = cloner.clone(groupNode);
            } else {
                // We are on our own
                this.groupNode = null;
                this.startIndex = -1;
            }

            // If we were cloning the hierachy that contained the parent group then it would have
            // been shallow cloned before this child. Can't really be otherwise...
        }

        this.cachedWorldMat = cloner.clone(cachedWorldMat);

        // TODO: Consider animation here, special cloning stage

        this.mesh = cloner.clone(mesh);

        this.material = cloner.clone(material);
    }

    /**
     * @param modelBound
     */
    public void setModelBound(BoundingVolume modelBound) {
        this.worldBound = modelBound;
        mesh.setBound(modelBound);
        setBoundRefresh();
    }

    /**
     * @return The bounding volume of the mesh, in model space.
     */
    public BoundingVolume getModelBound() {
        return mesh.getBound();
    }
}
