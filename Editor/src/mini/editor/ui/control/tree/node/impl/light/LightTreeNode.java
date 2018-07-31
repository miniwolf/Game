package mini.editor.ui.control.tree.node.impl.light;

import mini.editor.ui.control.tree.TreeNode;
import mini.light.Light;

public class LightTreeNode<T extends Light> extends TreeNode<T> {
    public LightTreeNode(
            final T element,
            final long objectId) {
        super(element, objectId);
    }

    @Override
    public String getName() {
        final T element = getElement();
        String name = element.getName();
        return name == null || name.isEmpty() ? element.getClass().getSimpleName() : name;
    }
}
