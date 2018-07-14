package mini.editor.ui.control.tree.node.factory;

import mini.editor.ui.control.tree.TreeNode;

public interface TreeNodeFactory extends Comparable<TreeNodeFactory> {

    /**
     * Create a tree node for the element.
     *
     * @param <T> the type of an element in the treenode
     * @param <V> the type of the tree node
     * @return the constructed tree node.
     */
    <T, V extends TreeNode<T>> V createFor(final T element, final long objectId);

    default int getPriority() {
        return 0;
    }

    @Override
    default int compareTo(TreeNodeFactory o) {
        return o.getPriority() - getPriority();
    }
}
