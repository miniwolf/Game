package mini.editor.ui.control.tree;

import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.model.Entity;

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
}
