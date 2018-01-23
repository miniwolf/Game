package mini.scene;

import mini.asset.AssetKey;
import mini.asset.CloneableSmartAsset;
import mini.bounding.BoundingVolume;
import mini.collision.Collidable;
import mini.light.Light;
import mini.light.LightList;
import mini.material.MatParamOverride;
import mini.material.Material;
import mini.math.Matrix3f;
import mini.math.Matrix4f;
import mini.math.Quaternion;
import mini.math.Transform;
import mini.math.Vector3f;
import mini.renderer.Camera;
import mini.renderer.RenderManager;
import mini.renderer.ViewPort;
import mini.renderer.queue.RenderQueue;
import mini.scene.control.Control;
import mini.utils.TempVars;
import mini.utils.clone.Cloner;
import mini.utils.clone.IdentityCloneFunction;
import mini.utils.clone.MiniCloneable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <code>Spatial</code> defines the base class for scene graph nodes. It
 * maintains a link to a parent, it's local transforms and the world's
 * transforms. All other scene graph elements, such as {@link Node} and
 * {@link Geometry} are subclasses of <code>Spatial</code>.
 *
 * @author miniwolf
 */
public abstract class Spatial implements Cloneable, CloneableSmartAsset, MiniCloneable, Collidable {
    /**
     * Specifies how frustum culling should be handled by
     * this spatial.
     */
    public enum CullHint {

        /**
         * Do whatever our parent does. If no parent, default to {@link #Dynamic}.
         */
        Inherit,
        /**
         * Do not draw if we are not at least partially within the view frustum
         * of the camera. This is determined via the defined
         * Camera planes whether or not this Spatial should be culled.
         */
        Dynamic,
        /**
         * Always cull this from the view, throwing away this object
         * and any children from rendering commands.
         */
        Always,
        /**
         * Never cull this from view, always draw it.
         * Note that we will still get culled if our parent is culled.
         */
        Never
    }

    /**
     * Specifies if this spatial should be batched
     */
    public enum BatchHint {

        /**
         * Do whatever our parent does. If no parent, default to {@link #Always}.
         */
        Inherit,
        /**
         * This spatial will always be batched when attached to a BatchNode.
         */
        Always,
        /**
         * This spatial will never be batched when attached to a BatchNode.
         */
        Never
    }

    /**
     * Refresh flag types
     */
    protected static final int RF_TRANSFORM = 0x01, // need light resort + combine transforms
            RF_BOUND = 0x02,
            RF_LIGHTLIST = 0x04, // changes in light lists 
            RF_CHILD_LIGHTLIST = 0x08, // some child need geometry update
            RF_MATPARAM_OVERRIDE = 0x10;

    protected CullHint cullHint = CullHint.Inherit;
    protected BatchHint batchHint = BatchHint.Inherit;
    /**
     * Spatial's bounding volume relative to the world.
     */
    protected BoundingVolume worldBound;
    /**
     * LightList
     */
    protected LightList localLights;
    protected transient LightList worldLights;

    protected List<MatParamOverride> localOverrides;
    protected List<MatParamOverride> worldOverrides;
    protected Map<String, Object> userData;

    /**
     * This spatial's name.
     */
    protected String name;
    // scale values
    protected transient Camera.FrustumIntersect frustrumIntersects
            = Camera.FrustumIntersect.Intersects;
    protected RenderQueue.Bucket queueBucket = RenderQueue.Bucket.Inherit;
    protected RenderQueue.ShadowMode shadowMode = RenderQueue.ShadowMode.Inherit;
    public transient float queueDistance = Float.NEGATIVE_INFINITY;
    protected Transform localTransform;
    protected Transform worldTransform;
    protected List<Control> controls = new CopyOnWriteArrayList<>();

    /**
     * Used for smart asset caching
     *
     * @see AssetKey
     */
    protected AssetKey key;
    /**
     * Spatial's parent, or null if it has none.
     */
    protected transient Node parent;
    /**
     * Refresh flags. Indicate what data of the spatial need to be
     * updated to reflect the correct state.
     */
    protected transient int refreshFlags = 0;

    /**
     * Set to true if a subclass requires updateLogicalState() even
     * if it doesn't have any controls.  Defaults to true thus implementing
     * the legacy behavior for any subclasses not specifically turning it
     * off.
     * This flag should be set during construction and never changed
     * as it's supposed to be class-specific and not runtime state.
     */
    private boolean requiresUpdates = true;

    /**
     * Serialization only. Do not use.
     * Not really. This class is never instantiated directly but the
     * subclasses like to use the no-arg constructor for their own
     * no-arg constructor... which is technically weaker than
     * forward supplying defaults.
     */
    protected Spatial() {
        this(null);
    }

    /**
     * Constructor instantiates a new <code>Spatial</code> object setting the
     * rotation, translation and scale value to defaults.
     *
     * @param name the name of the scene element. This is required for
     *             identification and comparison purposes.
     */
    protected Spatial(String name) {
        this.name = name;
        localTransform = new Transform();
        worldTransform = new Transform();

        localLights = new LightList(this);
        worldLights = new LightList(this);

        localOverrides = new ArrayList<>();
        worldOverrides = new ArrayList<>();
        refreshFlags |= RF_BOUND;
    }

    @Override
    /**
     * Note that meshes of geometries are not cloned explicitly, they are shared if static, or
     * specially cloned if animated.
     * <p>
     * All controls will be cloned using the Control.cloneForSpatial method on the clone.
     *
     * @return A clone of this Spatial, the scene graph in its entirety is cloned and can be altered
     * independently of the original scene graph.     *
     * @see Mesh#cloneForAnim()
     */
    public Object clone() {
        return clone(true);
    }

    @Override
    public void setKey(AssetKey key) {
        this.key = key;
    }

    /**
     * Returns true if this spatial requires updateLogicalState() to
     * be called, either because setRequiresUpdate(true) has been called
     * or because the spatial has controls.  This is package private to
     * avoid exposing it to the public API since it is only used by Node.
     */
    boolean requiresUpdates() {
        return requiresUpdates;
    }

    /**
     * Subclasses can call this with true to denote that they require
     * updateLogicalState() to be called even if they contain no controls.
     * Setting this to false reverts to the default behavior of only
     * updating if the spatial has controls.  This is not meant to
     * indicate dynamic state in any way and must be called while
     * unattached or an IllegalStateException is thrown.  It is designed
     * to be called during object construction and then never changed, ie:
     * it's meant to be subclass specific state and not runtime state.
     * Subclasses of Node or Geometry that do not set this will get the
     * old default behavior as if this was set to true.  Subclasses should
     * call setRequiresUpdate(false) in their constructors to receive
     * optimal behavior if they don't require updateLogicalState() to be
     * called even if there are no controls.
     */
    protected void setRequiresUpdates(boolean f) {
        // Note to explorers, the reason this was done as a protected setter
        // instead of passed on construction is because it frees all subclasses
        // from having to make sure to always pass the value up in case they
        // are subclassed.
        // The reason that requiresUpdates() isn't just a protected method to
        // override (which would be more correct) is because the flag provides
        // some flexibility in how we break subclasses.  A protected method
        // would require that all subclasses that required updates need implement
        // this method or they would silently stop processing updates.  A flag
        // lets us set a default when a subclass is detected that is different
        // than the internal "more efficient" default.
        // Spatial's default is 'true' for this flag requiring subclasses to
        // override it for more optimal behavior.  Node and Geometry will override
        // it to false if the class is Node.class or Geometry.class.
        // This means that all subclasses will default to the old behavior
        // unless they opt in.
        if (parent != null) {
            throw new IllegalStateException("setRequiresUpdates() cannot be called once attached.");
        }
        this.requiresUpdates = f;
    }

    /**
     * Indicate that the transform of this spatial has changed and that
     * a refresh is required.
     */
    protected void setTransformRefresh() {
        refreshFlags |= RF_TRANSFORM;
        setBoundRefresh();
    }

    protected void setLightListRefresh() {
        refreshFlags |= RF_LIGHTLIST;
        // Make sure next updateGeometricState() visits this branch
        // to update lights.
        Spatial p = parent;
        while (p != null) {
            if ((p.refreshFlags & RF_CHILD_LIGHTLIST) != 0) {
                // The parent already has this flag,
                // so must all ancestors.
                return;
            }
            p.refreshFlags |= RF_CHILD_LIGHTLIST;
            p = p.parent;
        }
    }

    protected void setMatParamOverrideRefresh() {
        refreshFlags |= RF_MATPARAM_OVERRIDE;
        Spatial p = parent;
        while (p != null) {
            if ((p.refreshFlags & RF_MATPARAM_OVERRIDE) != 0) {
                return;
            }

            p.refreshFlags |= RF_MATPARAM_OVERRIDE;
            p = p.parent;
        }
    }

    /**
     * Indicate that the bounding of this spatial has changed and that
     * a refresh is required.
     */
    protected void setBoundRefresh() {
        refreshFlags |= RF_BOUND;

        Spatial p = parent;
        while (p != null) {
            if ((p.refreshFlags & RF_BOUND) != 0) {
                return;
            }

            p.refreshFlags |= RF_BOUND;
            p = p.parent;
        }
    }

    /**
     * (Internal use only) Forces a refresh of the given types of data.
     *
     * @param transforms Refresh world transform based on parents'
     * @param bounds     Refresh bounding volume data based on child nodes
     * @param lights     Refresh light list based on parents'
     */
    public void forceRefresh(boolean transforms, boolean bounds, boolean lights) {
        if (transforms) {
            setTransformRefresh();
        }
        if (bounds) {
            setBoundRefresh();
        }
        if (lights) {
            setLightListRefresh();
        }
    }

    /**
     * <code>checkCulling</code> checks the spatial with the camera to see if it
     * should be culled.
     * <p>
     * This method is called by the renderer. Usually it should not be called
     * directly.
     *
     * @param cam The camera to check against.
     * @return true if inside or intersecting camera frustum
     * (should be rendered), false if outside.
     */
    public boolean checkCulling(Camera cam) {
        if (refreshFlags != 0) {
            throw new IllegalStateException("Scene graph is not properly updated for rendering.\n"
                                            + "State was changed after rootNode.updateGeometricState() call. \n"
                                            + "Make sure you do not modify the scene from another thread!\n"
                                            + "Problem spatial name: " + getName());
        }

        CullHint cm = getCullHint();
        assert cm != CullHint.Inherit;
        if (cm == Spatial.CullHint.Always) {
            setLastFrustumIntersection(Camera.FrustumIntersect.Outside);
            return false;
        } else if (cm == Spatial.CullHint.Never) {
            setLastFrustumIntersection(Camera.FrustumIntersect.Intersects);
            return true;
        }

        // check to see if we can cull this node
        frustrumIntersects = (parent != null ? parent.frustrumIntersects
                                             : Camera.FrustumIntersect.Intersects);

//        if (frustrumIntersects == Camera.FrustumIntersect.Intersects) {
////            if (getQueueBucket() == RenderQueue.Bucket.Gui) { TODO: GUI rendering
////                return cam.containsGui(getWorldBound()); TODO: Bounding check against frustum
////            } else {
//                frustrumIntersects = cam.contains(getWorldBound());
//            //}
//        }
//
//        return frustrumIntersects != Camera.FrustumIntersect.Outside;
        return true;
    }

    /**
     * Sets the name of this spatial.
     *
     * @param name The spatial's new name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this spatial.
     *
     * @return This spatial's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the local {@link LightList}, which are the lights
     * that were directly attached to this <code>Spatial</code> through the
     * {@link #addLight(Light) } and
     * {@link #removeLight(Light) } methods.
     *
     * @return The local light list
     */
    public LightList getLocalLightList() {
        return localLights;
    }

    /**
     * Returns the world {@link LightList}, containing the lights
     * combined from all this <code>Spatial's</code> parents up to and including
     * this <code>Spatial</code>'s lights.
     *
     * @return The combined world light list
     */
    public LightList getWorldLightList() {
        return worldLights;
    }

    /**
     * Get the local material parameter overrides.
     *
     * @return The list of local material parameter overrides.
     */
    public List<MatParamOverride> getLocalMatParamOverrides() {
        return localOverrides;
    }

    /**
     * Get the world material parameter overrides.
     * <p>
     * Note that this list is only updated on a call to
     * {@link #updateGeometricState()}. After update, the world overrides list
     * will contain the {@link #getParent() parent's} world overrides combined
     * with this spatial's {@link #getLocalMatParamOverrides() local overrides}.
     *
     * @return The list of world material parameter overrides.
     */
    public List<MatParamOverride> getWorldMatParamOverrides() {
        return worldOverrides;
    }

    /**
     * <code>getWorldRotation</code> retrieves the absolute rotation of the
     * Spatial.
     *
     * @return the Spatial's world rotation quaternion.
     */
    public Quaternion getWorldRotation() {
        checkDoTransformUpdate();
        return worldTransform.getRotation();
    }

    /**
     * <code>getWorldTranslation</code> retrieves the absolute translation of
     * the spatial.
     *
     * @return the Spatial's world translation vector.
     */
    public Vector3f getWorldTranslation() {
        checkDoTransformUpdate();
        return worldTransform.getTranslation();
    }

    /**
     * <code>getWorldScale</code> retrieves the absolute scale factor of the
     * spatial.
     *
     * @return the Spatial's world scale factor.
     */
    public Vector3f getWorldScale() {
        checkDoTransformUpdate();
        return worldTransform.getScale();
    }

    /**
     * <code>getWorldTransform</code> retrieves the world transformation
     * of the spatial.
     *
     * @return the world transform.
     */
    public Transform getWorldTransform() {
        checkDoTransformUpdate();
        return worldTransform;
    }

    /**
     * <code>rotateUpTo</code> is a utility function that alters the
     * local rotation to point the Y axis in the direction given by newUp.
     *
     * @param newUp the up vector to use - assumed to be a unit vector.
     */
    public void rotateUpTo(Vector3f newUp) {
        TempVars vars = TempVars.get();

        Vector3f compVecA = vars.vect1;
        Quaternion q = vars.quat1;

        // First figure out the current up vector.
        Vector3f upY = compVecA.set(Vector3f.UNIT_Y);
        Quaternion rot = localTransform.getRotation();
        rot.multLocal(upY);

        // get angle between vectors
        float angle = upY.angleBetween(newUp);

        // figure out rotation axis by taking cross product
        Vector3f rotAxis = upY.crossLocal(newUp).normalizeLocal();

        // Build a rotation quat and apply current local rotation.
        q.fromAngleNormalAxis(angle, rotAxis);
        q.mult(rot, rot);

        vars.release();

        setTransformRefresh();
    }

    /**
     * <code>lookAt</code> is a convenience method for auto-setting the local
     * rotation based on a position in world space and an up vector. It computes the rotation
     * to transform the z-axis to point onto 'position' and the y-axis to 'up'.
     * Unlike {@link Quaternion#lookAt(Vector3f, Vector3f) }
     * this method takes a world position to look at and not a relative direction.
     * <p>
     * Note : 28/01/2013 this method has been fixed as it was not taking into account the parent rotation.
     * This was resulting in improper rotation when the spatial had rotated parent nodes.
     * This method is intended to work in world space, so no matter what parent graph the
     * spatial has, it will look at the given position in world space.
     *
     * @param position where to look at in terms of world coordinates
     * @param upVector a vector indicating the (local) up direction. (typically {0, 1, 0})
     */
    public void lookAt(Vector3f position, Vector3f upVector) {
        Vector3f worldTranslation = getWorldTranslation();

        TempVars vars = TempVars.get();

        Vector3f compVecA = vars.vect4;
        compVecA.set(position).subtractLocal(worldTranslation);
        getLocalRotation().lookAt(compVecA, upVector);
        if (getParent() != null) {
            Quaternion rot = vars.quat1;
            rot = rot.set(parent.getWorldRotation()).inverseLocal().multLocal(getLocalRotation());
            rot.normalizeLocal();
            setLocalRotation(rot);
        }
        vars.release();
        setTransformRefresh();
    }

    /**
     * Should be overridden by Node and Geometry.
     */
    protected void updateWorldBound() {
        // the world bound of a leaf is the same as it's model bound
        // for a node, the world bound is a combination of all it's children
        // bounds
        // -> handled by subclass
        refreshFlags &= ~RF_BOUND;
    }

    protected void updateWorldLightList() {
        if (parent == null) {
            worldLights.update(localLights, null);
            refreshFlags &= ~RF_LIGHTLIST;
        } else {
            assert (parent.refreshFlags & RF_LIGHTLIST) == 0;
            worldLights.update(localLights, parent.worldLights);
            refreshFlags &= ~RF_LIGHTLIST;
        }
    }

    protected void updateMatParamOverrides() {
        refreshFlags &= ~RF_MATPARAM_OVERRIDE;

        worldOverrides.clear();
        if (parent == null) {
            worldOverrides.addAll(localOverrides);
        } else {
            assert (parent.refreshFlags & RF_MATPARAM_OVERRIDE) == 0;
            worldOverrides.addAll(parent.worldOverrides);
            worldOverrides.addAll(localOverrides);
        }
    }

    /**
     * Adds a local material parameter override.
     *
     * @param override The override to add.
     * @see MatParamOverride
     */
    public void addMatParamOverride(MatParamOverride override) {
        if (override == null) {
            throw new IllegalArgumentException("override cannot be null");
        }
        localOverrides.add(override);
        setMatParamOverrideRefresh();
    }

    /**
     * Remove a local material parameter override if it exists.
     *
     * @param override The override to remove.
     * @see MatParamOverride
     */
    public void removeMatParamOverride(MatParamOverride override) {
        if (localOverrides.remove(override)) {
            setMatParamOverrideRefresh();
        }
    }

    /**
     * Remove all local material parameter overrides.
     *
     * @see #addMatParamOverride(MatParamOverride)
     */
    public void clearMatParamOverrides() {
        if (!localOverrides.isEmpty()) {
            setMatParamOverrideRefresh();
        }
        localOverrides.clear();
    }

    /**
     * Should only be called from updateGeometricState().
     * In most cases should not be subclassed.
     */
    protected void updateWorldTransforms() {
        if (parent == null) {
            worldTransform.set(localTransform);
            refreshFlags &= ~RF_TRANSFORM;
        } else {
            // check if transform for parent is updated
            assert ((parent.refreshFlags & RF_TRANSFORM) == 0);
            worldTransform.set(localTransform);
            worldTransform.combineWithParent(parent.worldTransform);
            refreshFlags &= ~RF_TRANSFORM;
        }
    }

    /**
     * Computes the world transform of this Spatial in the most
     * efficient manner possible.
     */
    void checkDoTransformUpdate() {
        if ((refreshFlags & RF_TRANSFORM) == 0) {
            return;
        }

        if (parent == null) {
            worldTransform.set(localTransform);
            refreshFlags &= ~RF_TRANSFORM;
        } else {
            TempVars vars = TempVars.get();

            Spatial[] stack = vars.spatialStack;
            Spatial rootNode = this;
            int i = 0;
            while (true) {
                Spatial hisParent = rootNode.parent;
                if (hisParent == null) {
                    rootNode.worldTransform.set(rootNode.localTransform);
                    rootNode.refreshFlags &= ~RF_TRANSFORM;
                    i--;
                    break;
                }

                stack[i] = rootNode;

                if ((hisParent.refreshFlags & RF_TRANSFORM) == 0) {
                    break;
                }

                rootNode = hisParent;
                i++;
            }

            vars.release();

            for (int j = i; j >= 0; j--) {
                rootNode = stack[j];
                rootNode.updateWorldTransforms();
            }
        }
    }

    /**
     * Computes this Spatial's world bounding volume in the most efficient
     * manner possible.
     */
    void checkDoBoundUpdate() {
        if ((refreshFlags & RF_BOUND) == 0) {
            return;
        }

        checkDoTransformUpdate();

        // Go to children recursively and update their bound
        if (this instanceof Node) {
            Node node = (Node) this;
            int len = node.getQuantity();
            for (int i = 0; i < len; i++) {
                Spatial child = node.getChild(i);
                child.checkDoBoundUpdate();
            }
        }

        // All children's bounds have been updated. Update my own now.
        updateWorldBound();
    }

    private void runControlUpdate(float tpf) {
        if (controls.isEmpty()) {
            return;
        }

        for (Control control : controls) {
            control.update(tpf);
        }
    }

    /**
     * Called when the Spatial is about to be rendered, to notify controls attached to this Spatial
     * using the Control.render() method.
     *
     * @param renderManager The RenderManager rendering this Spatial.
     * @param vp            The ViewPort to which the Spatial is being rendered to.
     * @see Spatial#addControl(Control)
     * @see Spatial#removeControl(Control)
     */
    public void runControlRender(RenderManager renderManager, ViewPort vp) {
        if (controls.isEmpty()) {
            return;
        }

        for (Control control : controls) {
            control.render(renderManager, vp);
        }
    }

    /**
     * Add a control to the list of controls.
     *
     * @param control The control to add.
     * @see Spatial#
     */
    public void addControl(Control control) {
        boolean before = requiresUpdates();
        controls.add(control);
        control.setSpatial(this);
        boolean after = requiresUpdates();
        // If the requirement to be updated has changed then we need to let the parent know so it
        // can rebuild its update list.
        if (parent != null && before != after) {
            parent.invalidateUpdateList();
        }
    }

    /**
     * Removes the given control from this spatial's controls.
     *
     * @param control The control to remove
     */
    public boolean removeControl(Control control) {
        boolean before = requiresUpdates();
        boolean result = controls.remove(control);
        if (result) {
            control.setSpatial(null);
        }

        boolean after = requiresUpdates();
        // If the requirement to be updated has changed then we need to let the parent know so it
        // can rebuild its update list.
        if (parent != null && before != after) {
            parent.invalidateUpdateList();
        }

        return result;
    }

    /**
     * <code>updateLogicalState</code> call the <code>update()</code> method for all controls
     * attached to this <code>Spatial</code>.
     *
     * @param tpf Time per frame
     */
    public void updateLogicalState(float tpf) {
        runControlUpdate(tpf);
    }

    /**
     * <code>updateGeometricState</code> updates the lightlist,
     * computes the world transforms, and computes the world bounds
     * for this Spatial.
     * Calling this when the Spatial is attached to a node
     * will cause undefined results. User code should only call this
     * method on Spatials having no parent.
     *
     * @see Spatial#getWorldLightList()
     * @see Spatial#getWorldTransform()
     */
    public void updateGeometricState() {
        // assume that this Spatial is a leaf, a proper implementation
        // for this method should be provided by Node.

        // NOTE: Update world transforms first because
        // bound transform depends on them.
        if ((refreshFlags & RF_LIGHTLIST) != 0) {
            updateWorldLightList();
        }
        if ((refreshFlags & RF_TRANSFORM) != 0) {
            updateWorldTransforms();
        }
        if ((refreshFlags & RF_BOUND) != 0) {
            updateWorldBound();
        }
        if ((refreshFlags & RF_MATPARAM_OVERRIDE) != 0) {
            updateMatParamOverrides();
        }
        assert refreshFlags == 0;
    }

    /**
     * Convert a vector (in) from this spatial's local coordinate space to world
     * coordinate space.
     *
     * @param in    vector to read from
     * @param store where to write the result (null to create a new vector, may be
     *              same as in)
     * @return the result (store)
     */
    public Vector3f localToWorld(final Vector3f in, Vector3f store) {
        checkDoTransformUpdate();
        return worldTransform.transformVector(in, store);
    }

    /**
     * Convert a vector (in) from world coordinate space to this spatial's local
     * coordinate space.
     *
     * @param in    vector to read from
     * @param store where to write the result
     * @return the result (store)
     */
    public Vector3f worldToLocal(final Vector3f in, final Vector3f store) {
        checkDoTransformUpdate();
        return worldTransform.transformInverseVector(in, store);
    }

    /**
     * <code>getParent</code> retrieves this node's parent. If the parent is
     * null this is the root node.
     *
     * @return the parent of this node.
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Called by {@link Node#attachChild(Spatial)} and
     * {@link Node#detachChild(Spatial)} - don't call directly.
     * <code>setParent</code> sets the parent of this node.
     *
     * @param parent the parent of this node.
     */
    protected void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * <code>removeFromParent</code> removes this Spatial from it's parent.
     *
     * @return true if it has a parent and performed the remove.
     */
    public boolean removeFromParent() {
        if (parent != null) {
            parent.detachChild(this);
            return true;
        }
        return false;
    }

    /**
     * determines if the provided Node is the parent, or parent's parent, etc. of this Spatial.
     *
     * @param ancestor the ancestor object to look for.
     * @return true if the ancestor is found, false otherwise.
     */
    public boolean hasAncestor(Node ancestor) {
        return parent != null && (parent.equals(ancestor) || parent.hasAncestor(ancestor));
    }

    /**
     * <code>getLocalRotation</code> retrieves the local rotation of this
     * node.
     *
     * @return the local rotation of this node.
     */
    public Quaternion getLocalRotation() {
        return localTransform.getRotation();
    }

    /**
     * <code>setLocalRotation</code> sets the local rotation of this node
     * by using a {@link Matrix3f}.
     *
     * @param rotation the new local rotation.
     */
    public void setLocalRotation(Matrix3f rotation) {
        localTransform.getRotation().fromRotationMatrix(rotation);
        setTransformRefresh();
    }

    /**
     * <code>setLocalRotation</code> sets the local rotation of this node.
     *
     * @param quaternion the new local rotation.
     */
    public void setLocalRotation(Quaternion quaternion) {
        localTransform.setRotation(quaternion);
        setTransformRefresh();
    }

    /**
     * <code>getLocalScale</code> retrieves the local scale of this node.
     *
     * @return the local scale of this node.
     */
    public Vector3f getLocalScale() {
        return localTransform.getScale();
    }

    /**
     * <code>setLocalScale</code> sets the local scale of this node.
     *
     * @param localScale the new local scale, applied to x, y and z
     */
    public void setLocalScale(float localScale) {
        localTransform.setScale(localScale);
        setTransformRefresh();
    }

    /**
     * <code>setLocalScale</code> sets the local scale of this node.
     */
    public void setLocalScale(float x, float y, float z) {
        localTransform.setScale(x, y, z);
        setTransformRefresh();
    }

    /**
     * <code>setLocalScale</code> sets the local scale of this node.
     *
     * @param localScale the new local scale.
     */
    public void setLocalScale(Vector3f localScale) {
        localTransform.setScale(localScale);
        setTransformRefresh();
    }

    /**
     * <code>getLocalTranslation</code> retrieves the local translation of
     * this node.
     *
     * @return the local translation of this node.
     */
    public Vector3f getLocalTranslation() {
        return localTransform.getTranslation();
    }

    /**
     * <code>setLocalTranslation</code> sets the local translation of this
     * spatial.
     *
     * @param localTranslation the local translation of this spatial.
     */
    public void setLocalTranslation(Vector3f localTranslation) {
        this.localTransform.setTranslation(localTranslation);
        setTransformRefresh();
    }

    /**
     * <code>setLocalTranslation</code> sets the local translation of this
     * spatial.
     */
    public void setLocalTranslation(float x, float y, float z) {
        this.localTransform.setTranslation(x, y, z);
        setTransformRefresh();
    }

    /**
     * <code>setLocalTransform</code> sets the local transform of this
     * spatial.
     */
    public void setLocalTransform(Transform t) {
        this.localTransform.set(t);
        setTransformRefresh();
    }

    /**
     * <code>getLocalTransform</code> retrieves the local transform of
     * this spatial.
     *
     * @return the local transform of this spatial.
     */
    public Transform getLocalTransform() {
        return localTransform;
    }

    /**
     * Applies the given material to the Spatial, this will propagate the
     * material down to the geometries in the scene graph.
     *
     * @param material The material to set.
     */
    public void setMaterial(Material material) {
    }

    /**
     * <code>addLight</code> adds the given light to the Spatial; causing all
     * child Spatials to be affected by it.
     *
     * @param light The light to add.
     */
    public void addLight(Light light) {
        localLights.add(light);
        setLightListRefresh();
    }

    /**
     * <code>removeLight</code> removes the given light from the Spatial.
     *
     * @param light The light to remove.
     * @see Spatial#addLight(Light)
     */
    public void removeLight(Light light) {
        //localLights.remove(light);
        setLightListRefresh();
        throw new UnsupportedOperationException();
    }

    /**
     * Translates the spatial by the given translation vector.
     *
     * @return The spatial on which this method is called, e.g <code>this</code>.
     */
    public Spatial move(float x, float y, float z) {
        this.localTransform.getTranslation().addLocal(x, y, z);
        setTransformRefresh();

        return this;
    }

    /**
     * Translates the spatial by the given translation vector.
     *
     * @return The spatial on which this method is called, e.g <code>this</code>.
     */
    public Spatial move(Vector3f offset) {
        this.localTransform.getTranslation().addLocal(offset);
        setTransformRefresh();

        return this;
    }

    /**
     * Scales the spatial by the given value
     *
     * @return The spatial on which this method is called, e.g <code>this</code>.
     */
    public Spatial scale(float s) {
        return scale(s, s, s);
    }

    /**
     * Scales the spatial by the given scale vector.
     *
     * @return The spatial on which this method is called, e.g <code>this</code>.
     */
    public Spatial scale(float x, float y, float z) {
        this.localTransform.getScale().multLocal(x, y, z);
        setTransformRefresh();

        return this;
    }

    /**
     * Rotates the spatial by the given rotation.
     *
     * @return The spatial on which this method is called, e.g <code>this</code>.
     */
    public Spatial rotate(Quaternion rot) {
        this.localTransform.getRotation().multLocal(rot);
        setTransformRefresh();

        return this;
    }

    /**
     * Rotates the spatial by the xAngle, yAngle and zAngle angles (in radians),
     * (aka pitch, yaw, roll) in the local coordinate space.
     *
     * @return The spatial on which this method is called, e.g <code>this</code>.
     */
    public Spatial rotate(float xAngle, float yAngle, float zAngle) {
        TempVars vars = TempVars.get();
        Quaternion q = vars.quat1;
        q.fromAngles(xAngle, yAngle, zAngle);
        rotate(q);
        vars.release();

        return this;
    }

    /**
     * Centers the spatial in the origin of the world bound.
     */
    public void center() {
        Vector3f worldTranslation = getWorldTranslation();
        Vector3f worldCenter = getWorldBound().getCenter();

        Vector3f absTranslation = worldTranslation.subtract(worldCenter);
        setLocalTranslation(absTranslation);
    }

    @SuppressWarnings("unchecked")
    public <T> T getUserData(String key) {
        if (userData == null) {
            return null;
        }

        Object o = userData.get(key);
        if (o instanceof UserData) {
            return (T) ((UserData) o).getValue();
        } else {
            return (T) o;
        }
    }

    public void setUserData(String key, Object data) {
        if (data == null) { // Remove object
            if (userData != null) {
                userData.remove(key);
                if (userData.isEmpty()) {
                    userData = null;
                }
            }
        } else {
            if (userData == null) {
                userData = new HashMap<>();
            }
            userData.put(key, new UserData(UserData.getObjectType(data), data));
        }
    }

    /**
     * @return the cull mode of this spatial, or if set to CullHint.Inherit, the
     * cull mode of its parent.
     * @see #setCullHint(CullHint)
     */
    public CullHint getCullHint() {
        if (cullHint != CullHint.Inherit) {
            return cullHint;
        } else if (parent != null) {
            return parent.getCullHint();
        } else {
            return CullHint.Dynamic;
        }
    }

    public BatchHint getBatchHint() {
        if (batchHint != BatchHint.Inherit) {
            return batchHint;
        } else if (parent != null) {
            return parent.getBatchHint();
        } else {
            return BatchHint.Always;
        }
    }

    /**
     * Returns this spatial's renderqueue bucket. If the mode is set to inherit,
     * then the spatial gets its renderqueue bucket from its parent.
     *
     * @return The spatial's current renderqueue mode.
     */
    public RenderQueue.Bucket getQueueBucket() {
        if (queueBucket != RenderQueue.Bucket.Inherit) {
            return queueBucket;
        } else if (parent != null) {
            return parent.getQueueBucket();
        } else {
            return RenderQueue.Bucket.Opaque;
        }
    }

    /**
     * @return The shadow mode of this spatial, if the local shadow
     * mode is set to inherit, then the parent's shadow mode is returned.
     * @see Spatial#setShadowMode(RenderQueue.ShadowMode)
     * @see RenderQueue.ShadowMode
     */
    public RenderQueue.ShadowMode getShadowMode() {
        if (shadowMode != RenderQueue.ShadowMode.Inherit) {
            return shadowMode;
        } else if (parent != null) {
            return parent.getShadowMode();
        } else {
            return RenderQueue.ShadowMode.Off;
        }
    }

    /**
     * Sets the level of detail to use when rendering this Spatial,
     * this call propagates to all geometries under this Spatial.
     *
     * @param lod The lod level to set.
     */
    public void setLodLevel(int lod) {
    }

    /**
     * @return The sum of all vertices under this Spatial.
     */
    public abstract int getVertexCount();

    /**
     * @return The sum of all triangles under this Spatial.
     */
    public abstract int getTriangleCount();

    /**
     * @return A clone of this Spatial, the scene graph in its entirety
     * is cloned and can be altered independently of the original scene graph.
     * <p>
     * Note that meshes of geometries are not cloned explicitly, they
     * are shared if static, or specially cloned if animated.
     * <p>
     * All controls will be cloned using the Control.cloneForSpatial method
     * on the clone.
     * @see Mesh#cloneForAnim()
     */
    public Spatial clone(boolean cloneMaterial) {
        // Setup the cloner for the type of cloning we want to do.
        Cloner cloner = new Cloner();

        // First, we definitely do not want to clone our own parent
        cloner.setClonedValue(parent, null);

        // If we aren't cloning materials then we will make sure those aren't cloned
        if (!cloneMaterial) {
            cloner.setCloneFunction(Material.class, new IdentityCloneFunction<>());
        }

        // By default the meshes are not cloned. The geometry may choose to selectively force them
        // to be cloned but normally they will be shared.
        cloner.setCloneFunction(Mesh.class, new IdentityCloneFunction<>());

        // Clone it!
        Spatial clone = cloner.clone(this);

        // Because we've nulled the parent out we need to make sure
        // the transforms and stuff get refreshed.
        clone.setTransformRefresh();
        clone.setLightListRefresh();
        clone.setMatParamOverrideRefresh();

        return clone;
    }

    /**
     * @return Similar to Spatial.clone() except will create a deep clone of all
     * geometries' meshes. Normally this method shouldn't be used. Instead, use
     * Spatial.clone()
     * @see Spatial#clone()
     */
    public Spatial deepClone() {
        // Setup the cloner for the type of cloning we want to do.
        Cloner cloner = new Cloner();

        // First, we definitely do not want to clone our own parent
        cloner.setClonedValue(parent, null);

        Spatial clone = cloner.clone(this);

        // Because we've nulled the parent out we need to make sure
        // the transforms and stuff get refreshed.
        clone.setTransformRefresh();
        clone.setLightListRefresh();
        clone.setMatParamOverrideRefresh();

        return clone;
    }

    @Override
    public Spatial miniClone() {
        try {
            return (Spatial) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * Called internally by mini.utils.clone.Cloner. Do not call directly.
     */
    @Override
    public void cloneFields(Cloner cloner, Object original) {

        // Potential sharing fields
        this.parent = cloner.clone(parent);
        this.worldBound = cloner.clone(worldBound);
        this.worldLights = cloner.clone(worldLights);
        this.localLights = cloner.clone(localLights);
        this.worldTransform = cloner.clone(worldTransform);
        this.localTransform = cloner.clone(localTransform);
        this.worldOverrides = cloner.clone(worldOverrides);
        this.localOverrides = cloner.clone(localOverrides);

    }

    /**
     * <code>getWorldBound</code> retrieves the world bound at this node
     * level.
     *
     * @return the world bound at this level.
     */
    public BoundingVolume getWorldBound() {
        checkDoBoundUpdate();
        return worldBound;
    }

    /**
     * <code>setCullHint</code> alters how view frustum culling will treat this
     * spatial.
     *
     * @param hint one of: <code>CullHint.Dynamic</code>,
     *             <code>CullHint.Always</code>, <code>CullHint.Inherit</code>, or
     *             <code>CullHint.Never</code>
     *             <p>
     *             The effect of the default value (CullHint.Inherit) may change if the
     *             spatial gets re-parented.
     */
    public void setCullHint(CullHint hint) {
        cullHint = hint;
    }

    /**
     * <code>setBatchHint</code> alters how batching will treat this spatial.
     *
     * @param hint one of: <code>BatchHint.Never</code>,
     *             <code>BatchHint.Always</code>, or <code>BatchHint.Inherit</code>
     *             <p>
     *             The effect of the default value (BatchHint.Inherit) may change if the
     *             spatial gets re-parented.
     */
    public void setBatchHint(BatchHint hint) {
        batchHint = hint;
    }

    /**
     * @return the cullmode set on this Spatial
     */
    public CullHint getLocalCullHint() {
        return cullHint;
    }

    /**
     * @return the batchHint set on this Spatial
     */
    public BatchHint getLocalBatchHint() {
        return batchHint;
    }

    /**
     * <code>setQueueBucket</code> determines at what phase of the
     * rendering process this Spatial will rendered. See the
     * {@link RenderQueue.Bucket} enum for an explanation of the various
     * render queue buckets.
     *
     * @param queueBucket The bucket to use for this Spatial.
     */
    public void setQueueBucket(RenderQueue.Bucket queueBucket) {
        this.queueBucket = queueBucket;
    }

    /**
     * Sets the shadow mode of the spatial
     * The shadow mode determines how the spatial should be shadowed,
     * when a shadowing technique is used. See the
     * documentation for the class {@link RenderQueue.ShadowMode} for more information.
     *
     * @param shadowMode The local shadow mode to set.
     * @see RenderQueue.ShadowMode
     */
    public void setShadowMode(RenderQueue.ShadowMode shadowMode) {
        this.shadowMode = shadowMode;
    }

    /**
     * @return The locally set queue bucket mode
     * @see Spatial#setQueueBucket(RenderQueue.Bucket)
     */
    public RenderQueue.Bucket getLocalQueueBucket() {
        return queueBucket;
    }

    /**
     * @return The locally set shadow mode
     * @see Spatial#setShadowMode(RenderQueue.ShadowMode)
     */
    public RenderQueue.ShadowMode getLocalShadowMode() {
        return shadowMode;
    }

    /**
     * Returns this spatial's last frustum intersection result. This int is set
     * when a check is made to determine if the bounds of the object fall inside
     * a camera's frustum. If a parent is found to fall outside the frustum, the
     * value for this spatial will not be updated.
     *
     * @return The spatial's last frustum intersection result.
     */
    public Camera.FrustumIntersect getLastFrustumIntersection() {
        return frustrumIntersects;
    }

    /**
     * Overrides the last intersection result. This is useful for operations
     * that want to start rendering at the middle of a scene tree and don't want
     * the parent of that node to influence culling.
     *
     * @param intersects the new value
     */
    public void setLastFrustumIntersection(Camera.FrustumIntersect intersects) {
        frustrumIntersects = intersects;
    }

    /**
     * Returns the Spatial's name followed by the class of the spatial <br>
     * Example: "MyNode (mini.scene.Spatial)
     *
     * @return Spatial's name followed by the class of the Spatial
     */
    @Override
    public String toString() {
        return name + " (" + this.getClass().getSimpleName() + ')';
    }

    /**
     * Creates a transform matrix that will convert from this spatials'
     * local coordinate space to the world coordinate space
     * based on the world transform.
     *
     * @param store Matrix where to store the result, if null, a new one
     *              will be created and returned.
     * @return store if not null, otherwise, a new matrix containing the result.
     * @see Spatial#getWorldTransform()
     */
    public Matrix4f getLocalToWorldMatrix(Matrix4f store) {
        if (store == null) {
            store = new Matrix4f();
        } else {
            store.loadIdentity();
        }
        // multiply with scale first, then rotate, finally translate (cf.
        // Eberly)
        store.scale(getWorldScale());
        store.multLocal(getWorldRotation());
        store.setTranslation(getWorldTranslation());
        return store;
    }
}
