package mini.scene;

import mini.material.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>Node</code> defines an internal node of a scene graph. The internal
 * node maintains a collection of children and handles merging said children
 * into a single bound to allow for very fast culling of multiple nodes. Node
 * allows for any number of children to be attached.
 *
 * @author Mark Powell
 * @author Gregg Patton
 * @author Joshua Slack
 */
public class Node extends Spatial {
    /**
     * This node's children.
     */
    protected List<Spatial> children = new ArrayList<>();

    /**
     * If this node is a root, this list will contain the current
     * set of children (and children of children) that require
     * updateLogicalState() to be called as indicated by their
     * requiresUpdate() method.
     */
    private List<Spatial> updateList = null;
    /**
     * False if the update list requires rebuilding.  This is Node.class
     * specific and therefore not included as part of the Spatial update flags.
     * A flag is used instead of nulling the updateList to avoid reallocating
     * a whole list every time the scene graph changes.
     */
    private boolean updateListValid = false;

    /**
     * Serialization only. Do not use.
     */
    public Node() {
        this(null);
    }

    /**
     * Constructor instantiates a new <code>Node</code> with a default empty
     * list for containing children.
     *
     * @param name the name of the scene element. This is required for
     *             identification and comparison purposes.
     */
    public Node(String name) {
        super(name);
        // For backwards compatibility, only clear the "requires
        // update" flag if we are not a subclass of Node.
        // This prevents subclass from silently failing to receive
        // updates when they upgrade.
        setRequiresUpdates(Node.class != getClass());
    }

    /**
     * <code>getQuantity</code> returns the number of children this node
     * maintains.
     *
     * @return the number of children this node maintains.
     */
    public int getQuantity() {
        return children.size();
    }

    @Override
    protected void setTransformRefresh() {
        super.setTransformRefresh();
        for (Spatial child : children) {
            if ((child.refreshFlags & RF_TRANSFORM) != 0)
                continue;

            child.setTransformRefresh();
        }
    }

    @Override
    protected void setLightListRefresh() {
        super.setLightListRefresh();
        for (Spatial child : children) {
            if ((child.refreshFlags & RF_LIGHTLIST) != 0)
                continue;

            child.setLightListRefresh();
        }
    }

    @Override
    protected void setMatParamOverrideRefresh() {
        super.setMatParamOverrideRefresh();
        for (Spatial child : children) {
            if ((child.refreshFlags & RF_MATPARAM_OVERRIDE) != 0) {
                continue;
            }

            child.setMatParamOverrideRefresh();
        }
    }

//    @Override
//    protected void updateWorldBound(){
//        super.updateWorldBound();
//        // for a node, the world bound is a combination of all it's children
//        // bounds
//        BoundingVolume resultBound = null;
//        for (Spatial child : children.getArray()) {
//            // child bound is assumed to be updated
//            assert (child.refreshFlags & RF_BOUND) == 0;
//            if (resultBound != null) {
//                // merge current world bound with child world bound
//                resultBound.mergeLocal(child.getWorldBound());
//            } else {
//                // set world bound to first non-null child world bound
//                if (child.getWorldBound() != null) {
//                    resultBound = child.getWorldBound().clone(this.worldBound);
//                }
//            }
//        }
//        this.worldBound = resultBound;
//    }

    @Override
    protected void setParent(Node parent) {
        if (this.parent == null && parent != null) {
            // We were a root before and now we aren't... make sure if
            // we had an updateList then we clear it completely to
            // avoid holding the dead array.
            updateList = null;
            updateListValid = false;
        }
        super.setParent(parent);
    }

    private void addUpdateChildren(List<Spatial> results) {
        for (Spatial child : children) {
            if (child.requiresUpdates()) {
                results.add(child);
            }
            if (child instanceof Node) {
                ((Node) child).addUpdateChildren(results);
            }
        }
    }

    /**
     * Called to invalidate the root node's update list.  This is
     * called whenever a spatial is attached/detached as well as
     * when a control is added/removed from a Spatial in a way
     * that would change state.
     */
    void invalidateUpdateList() {
        updateListValid = false;
        if (parent != null) {
            parent.invalidateUpdateList();
        }
    }

    private List<Spatial> getUpdateList() {
        if (updateListValid) {
            return updateList;
        }
        if (updateList == null) {
            updateList = new ArrayList<>();
        } else {
            updateList.clear();
        }

        // Build the list
        addUpdateChildren(updateList);
        updateListValid = true;
        return updateList;
    }

    @Override
    public void updateGeometricState() {
        if (refreshFlags == 0) {
            // This branch has no geometric state that requires updates.
            return;
        }
        if ((refreshFlags & RF_LIGHTLIST) != 0) {
            updateWorldLightList();
        }
        if ((refreshFlags & RF_TRANSFORM) != 0) {
            // combine with parent transforms- same for all spatial
            // subclasses.
            updateWorldTransforms();
        }
        if ((refreshFlags & RF_MATPARAM_OVERRIDE) != 0) {
            updateMatParamOverrides();
        }

        refreshFlags &= ~RF_CHILD_LIGHTLIST;
        if (!children.isEmpty()) {
            // the important part- make sure child geometric state is refreshed
            // first before updating own world bound. This saves
            // a round-trip later on.
            // NOTE 9/19/09
            // Although it does save a round trip,
            for (Spatial child : children) {
                child.updateGeometricState();
            }
        }

        if ((refreshFlags & RF_BOUND) != 0) {
            updateWorldBound();
        }

        assert refreshFlags == 0;
    }

    /**
     * <code>getTriangleCount</code> returns the number of triangles contained
     * in all sub-branches of this node that contain geometry.
     *
     * @return the triangle count of this branch.
     */
    @Override
    public int getTriangleCount() {
        int count = 0;
        if (children != null) {
            for (Spatial child : children) {
                count += child.getTriangleCount();
            }
        }

        return count;
    }

    /**
     * <code>getVertexCount</code> returns the number of vertices contained
     * in all sub-branches of this node that contain geometry.
     *
     * @return the vertex count of this branch.
     */
    @Override
    public int getVertexCount() {
        int count = 0;
        if (children != null) {
            for (Spatial child : children) {
                count += child.getVertexCount();
            }
        }

        return count;
    }

    /**
     * <code>attachChild</code> attaches a child to this node. This node
     * becomes the child's parent. The current number of children maintained is
     * returned.
     * <br>
     * If the child already had a parent it is detached from that former parent.
     *
     * @param child the child to attach to this node.
     * @return the number of children maintained by this node.
     * @throws IllegalArgumentException if child is null.
     */
    public int attachChild(Spatial child) {
        return attachChildAt(child, children.size());
    }

    /**
     * <code>attachChildAt</code> attaches a child to this node at an index. This node
     * becomes the child's parent. The current number of children maintained is
     * returned.
     * <br>
     * If the child already had a parent it is detached from that former parent.
     *
     * @param child the child to attach to this node.
     * @return the number of children maintained by this node.
     * @throws NullPointerException if child is null.
     */
    public int attachChildAt(Spatial child, int index) {
        if (child == null)
            throw new NullPointerException();

        if (child.getParent() != this && child != this) {
            if (child.getParent() != null) {
                child.getParent().detachChild(child);
            }
            child.setParent(this);
            children.add(index, child);
            // XXX: Not entirely correct? Forces bound update up the
            // tree stemming from the attached child. Also forces
            // transform update down the tree-
            child.setTransformRefresh();
            child.setLightListRefresh();
            child.setMatParamOverrideRefresh();
            System.out.println("Child (" + child.getName() + ") attached to this node (" + getName() + ")");
            invalidateUpdateList();
        }
        return children.size();
    }

    /**
     * <code>detachChild</code> removes a given child from the node's list.
     * This child will no longer be maintained.
     *
     * @param child the child to remove.
     * @return the index the child was at. -1 if the child was not in the list.
     */
    public int detachChild(Spatial child) {
        if (child == null) {
            throw new NullPointerException();
        }

        if (child.getParent() == this) {
            int index = children.indexOf(child);
            if (index != -1) {
                detachChildAt(index);
            }
            return index;
        }

        return -1;
    }

    /**
     * <code>detachChild</code> removes a given child from the node's list.
     * This child will no longe be maintained. Only the first child with a
     * matching name is removed.
     *
     * @param childName the child to remove.
     * @return the index the child was at. -1 if the child was not in the list.
     */
    public int detachChildNamed(String childName) {
        if (childName == null) {
            throw new NullPointerException();
        }

        for (int x = 0, max = children.size(); x < max; x++) {
            Spatial child = children.get(x);
            if (childName.equals(child.getName())) {
                detachChildAt(x);
                return x;
            }
        }
        return -1;
    }

    /**
     * <code>detachChildAt</code> removes a child at a given index. That child
     * is returned for saving purposes.
     *
     * @param index the index of the child to be removed.
     * @return the child at the supplied index.
     */
    public Spatial detachChildAt(int index) {
        Spatial child = children.remove(index);
        if (child != null) {
            child.setParent(null);
            System.out.println(this.toString() + ": Child removed.");

            // since a child with a bound was detached;
            // our own bound will probably change.
            setBoundRefresh();

            // our world transform no longer influences the child.
            // XXX: Not neccessary? Since child will have transform updated
            // when attached anyway.
            child.setTransformRefresh();
            // lights are also inherited from parent
            child.setLightListRefresh();
            child.setMatParamOverrideRefresh();

            invalidateUpdateList();
        }
        return child;
    }

    /**
     * <code>detachAllChildren</code> removes all children attached to this
     * node.
     */
    public void detachAllChildren() {
        // Note: this could be a bit more efficient if it delegated
        // to a private method that avoided setBoundRefresh(), etc.
        // for every child and instead did one in here at the end.
        for (int i = children.size() - 1; i >= 0; i--) {
            detachChildAt(i);
        }
    }

    /**
     * <code>getChildIndex</code> returns the index of the given spatial
     * in this node's list of children.
     *
     * @param sp The spatial to look up
     * @return The index of the spatial in the node's children, or -1
     * if the spatial is not attached to this node
     */
    public int getChildIndex(Spatial sp) {
        return children.indexOf(sp);
    }

    /**
     * More efficient than e.g detaching and attaching as no updates are needed.
     *
     * @param index1 The index of the first child to swap
     * @param index2 The index of the second child to swap
     */
    public void swapChildren(int index1, int index2) {
        Spatial c2 = children.get(index2);
        Spatial c1 = children.remove(index1);
        children.add(index1, c2);
        children.remove(index2);
        children.add(index2, c1);
    }

    /**
     * <code>getChild</code> returns a child at a given index.
     *
     * @param i the index to retrieve the child from.
     * @return the child at a specified index.
     */
    public Spatial getChild(int i) {
        return children.get(i);
    }

    /**
     * <code>getChild</code> returns the first child found with exactly the
     * given name (case sensitive.) This method does a depth first recursive
     * search of all descendants of this node, it will return the first spatial
     * found with a matching name.
     *
     * @param name the name of the child to retrieve. If null, we'll return null.
     * @return the child if found, or null.
     */
    public Spatial getChild(String name) {
        if (name == null) {
            return null;
        }

        for (Spatial child : children) {
            if (name.equals(child.getName())) {
                return child;
            } else if (child instanceof Node) {
                Spatial out = ((Node) child).getChild(name);
                if (out != null) {
                    return out;
                }
            }
        }
        return null;
    }

    /**
     * determines if the provided Spatial is contained in the children list of
     * this node.
     *
     * @param spat the child object to look for.
     * @return true if the object is contained, false otherwise.
     */
    public boolean hasChild(Spatial spat) {
        if (children.contains(spat)) {
            return true;
        }

        for (Spatial child : children) {
            if (child instanceof Node && ((Node) child).hasChild(spat)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns all children to this node. Note that modifying that given
     * list is not allowed.
     *
     * @return a list containing all children to this node
     */
    public List<Spatial> getChildren() {
        return children;
    }

    @Override
    public void setMaterial(Material mat) {
        children.forEach(child -> child.setMaterial(mat));
    }

    @Override
    public void setLodLevel(int lod) {
        super.setLodLevel(lod);
        for (Spatial child : children) {
            child.setLodLevel(lod);
        }
    }

    public Node clone(boolean cloneMaterials) {
        Node nodeClone = (Node) super.clone();
//        nodeClone.children = new ArrayList<Spatial>();
//        for (Spatial child : children){
//            Spatial childClone = child.clone();
//            childClone.parent = nodeClone;
//            nodeClone.children.add(childClone);
//        }

        // Reset the fields of the clone that should be in a 'new' state.
        nodeClone.updateList = null;
        nodeClone.updateListValid = false; // safe because parent is nulled out in super.clone()
        return nodeClone;
    }

    @Override
    public Spatial deepClone() {
        Node nodeClone = (Node) super.deepClone();

        // Reset the fields of the clone that should be in a 'new' state.
        nodeClone.updateList = null;
        nodeClone.updateListValid = false; // safe because parent is nulled out in super.clone()

        return nodeClone;
    }
}
