package mini.editor.ui.control.tree.node.factory.impl;

import mini.animation.SkeletonControl;
import mini.editor.annotation.FxThread;
import mini.editor.ui.control.tree.TreeNode;
import mini.editor.ui.control.tree.node.factory.TreeNodeFactory;
import mini.scene.control.Control;
import mini.scene.control.LightControl;

public class ControlTreeNodeFactory implements TreeNodeFactory {
    @Override
    @FxThread
    public <T, V extends TreeNode<T>> V createFor(T element, long objectId) {
        if (element instanceof SkeletonControl) {
            throw new UnsupportedOperationException();
        } else if (element instanceof LightControl) {
            throw new UnsupportedOperationException();
        } else if (element instanceof Control) {
            throw new UnsupportedOperationException();
        }

        return null;
    }
}
