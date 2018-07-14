package mini.editor.ui.control.tree;

import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.model.Entity;

import java.util.Objects;

public class TreeNode<T> implements Entity {
    private final long objectId;
    private final T element;
    private TreeNode<?> parent;

    public TreeNode(final T element,
                    final long objectId) {
        this.objectId = objectId;
        this.element = element;
    }

    @FxThread
    public TreeNode<?> getParent() {
        return parent;
    }

    public void setParent(TreeNode<?> parent) {
        this.parent = parent;
    }

    /**
     * @return the wrapped element
     */
    @FromAnyThread
    public T getElement() {
        return element;
    }

    @Override
    @FromAnyThread
    public long getObjectID() {
        return objectId;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (obj instanceof TreeNode) {
            var treeNode = (TreeNode<?>) obj;
            return element.equals(treeNode.element);
        }

        return Objects.equals(element, obj);
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }

    /**
     * @return the name of this node.
     */
    @FromAnyThread
    public String getName() {
        return "unknown name";
    }

    /**
     * @return whether this node supports name editing.
     */
    @FxThread
    public boolean canEditName() {
        return false;
    }

    /**
     * @return whether this node has any children
     */
    @FxThread
    public boolean hasChildren(final NodeTree<?> nodeTree) {
        return false;
    }

    @FxThread
    public boolean canRemove() {
        return true;
    }

    /**
     * A model node has been removed from the list of children of this node.
     */
    public void notifyChildPreRemove(final TreeNode<?> treeNode) {
    }

    public void notifyChildRemoved(final TreeNode<?> treeNode) {
        treeNode.setParent(treeNode);
    }
}
