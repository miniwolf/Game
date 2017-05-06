package mini.scene;

import mini.material.Material;

import java.util.ArrayList;
import java.util.List;

public class Entity extends Spatial {
    private Geometry geometry;
    private Material material;

    private boolean castsShadow = true;
    private boolean hasReflection = true;
    private boolean seenUnderWater = false;
    private boolean isImportant = false;

    /**
     * This node's children.
     */
    private List<Spatial> children = new ArrayList<>();

    public Entity(String name) {
        super(name);
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
     * Returns all children to this node. Note that modifying that given
     * list is not allowed.
     *
     * @return a list containing all children to this node
     */
    public List<Spatial> getChildren() {
        return children;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void delete() {
        //geometry.delete();
        //material.delete();
    }

    public boolean isShadowCasting() {
        return castsShadow;
    }

    public void setCastsShadow(boolean shadow) {
        this.castsShadow = shadow;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public boolean hasReflection() {
        return hasReflection;
    }

    public void setHasReflection(boolean reflects) {
        this.hasReflection = reflects;
    }

    public void setImportant(boolean isImportant) {
        this.isImportant = isImportant;
    }

    public boolean isSeenUnderWater() {
        return seenUnderWater;
    }

    public void setSeenUnderWater(boolean seenUnderWater) {
        this.seenUnderWater = seenUnderWater;
    }
}
