package mini.editor.ui.control.tree.node.factory.impl;

import mini.editor.annotation.FxThread;
import mini.editor.ui.control.tree.TreeNode;
import mini.editor.ui.control.tree.node.factory.TreeNodeFactory;
import mini.editor.ui.control.tree.node.factory.impl.light.DirectionalLightTreeNode;
import mini.light.AmbientLight;
import mini.light.DirectionalLight;
import mini.light.LightProbe;
import mini.light.PointLight;

public class LightTreeNodeFactory implements TreeNodeFactory {
    @Override
    @FxThread
    public <T, V extends TreeNode<T>> V createFor(
            final T element,
            final long objectId) {
        if (element instanceof LightProbe) {
            throw new UnsupportedOperationException();
        } else if (element instanceof AmbientLight) {
            throw new UnsupportedOperationException();
        } else if (element instanceof DirectionalLight) {
            new DirectionalLightTreeNode((DirectionalLight) element, objectId);
        } else if (element instanceof PointLight) {
            throw new UnsupportedOperationException();
        }
        return null;
    }
}
