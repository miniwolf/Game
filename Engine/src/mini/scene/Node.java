package mini.scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miniwolf on 06-05-2017.
 */
public class Node extends Spatial {
    /**
     * This node's children.
     */
    private List<Spatial> children = new ArrayList<>();

    /**
     * Constructor instantiates a new <code>Spatial</code> object setting the rotation, translation
     * and scale value to defaults.
     *
     * @param name the name of the scene element. This is required for identification and
     *             comparison purposes.
     */
    public Node(String name) {
        super(name);

        // For backwards compatibility, only clear the "requires
        // update" flag if we are not a subclass of Node.
        // This prevents subclass from silently failing to receive
        // updates when they upgrade.
        setRequiresUpdates(Node.class != getClass());
    }

    @Override
    public void updateModelBound() {
        if (children != null) {
            for (Spatial child : children) {
                child.updateModelBound();
            }
        }
    }

    @Override
    public void updateGeometricState(){
        if (refreshFlags == 0) {
            // This branch has no geometric state that requires updates.
            return;
        }
        if ((refreshFlags & RF_LIGHTLIST) != 0){
            updateWorldLightList();
        }
        if ((refreshFlags & RF_TRANSFORM) != 0){
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

        if ((refreshFlags & RF_BOUND) != 0){
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
            for (Spatial aChildren : children) {
                count += aChildren.getTriangleCount();
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
            for (Spatial aChildren : children) {
                count += aChildren.getVertexCount();
            }
        }

        return count;
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
        if (child == null) {
            throw new NullPointerException();
        }

        if (child.getParent() != this && child != this) {
            if (child.getParent() != null) {
                child.getParent().detachChild(child);
            }
            child.setParent(this);
            children.add(index, child);
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
     * <code>detachChildAt</code> removes a child at a given index. That child is returned for
     * saving purposes.
     *
     * @param index the index of the child to be removed.
     * @return the child at the supplied index.
     */
    public Spatial detachChildAt(int index) {
        Spatial child = children.remove(index);
        if (child != null) {
            child.setParent(null);
        }
        return child;
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
     * Returns all children to this node. Note that modifying that given
     * list is not allowed.
     *
     * @return a list containing all children to this node
     */
    public List<Spatial> getChildren() {
        return children;
    }
}
