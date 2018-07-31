package mini.editor.ui.control.tree;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.model.Entity;
import mini.editor.ui.control.tree.node.factory.TreeNodeFactoryRegistry;

import java.util.Objects;

public class TreeNode<T> implements Entity {
    protected static final Array<TreeNode<?>> EMPTY_ARRAY = ArrayFactory.newArray(TreeNode.class);

    protected static final TreeNodeFactoryRegistry
            FACTORY_REGISTRY = TreeNodeFactoryRegistry.getInstance();

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

    public Array<TreeNode<?>> getChildren(NodeTree<?> nodeTree) {
        return EMPTY_ARRAY;
    }

    /**
     * Notify about that a model node will be added as a child of this node.
     *
     * @param treeNode the model node.
     */
    @FxThread
    public void notifyChildPreAdd(final TreeNode<?> treeNode) {
        treeNode.setParent(this);
    }

    /**
     * Notify about that a model node was added as a child of this node.
     *
     * @param treeNode the model node.
     */
    @FxThread
    public void notifyChildAdded(final TreeNode<?> treeNode) {
    }
}
