package mini.editor.ui.control.tree.action.impl.spatial;

import mini.editor.ui.control.tree.TreeNode;
import mini.scene.Spatial;

public class SpatialTreeNode<T extends Spatial> extends TreeNode<T> {
    public SpatialTreeNode(final T element, final long objectId) {
        super(element, objectId);
    }
}
