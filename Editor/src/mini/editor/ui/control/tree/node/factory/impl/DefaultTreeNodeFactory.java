package mini.editor.ui.control.tree.node.factory.impl;

import com.ss.rlib.common.util.ClassUtils;
import mini.editor.ui.control.tree.TreeNode;
import mini.editor.ui.control.tree.node.factory.TreeNodeFactory;
import mini.editor.ui.control.tree.node.factory.impl.spatial.GeometryTreeNode;
import mini.editor.ui.control.tree.node.factory.impl.spatial.NodeTreeNode;
import mini.material.Material;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.Node;

public class DefaultTreeNodeFactory implements TreeNodeFactory {
    @Override
    public <T, V extends TreeNode<T>> V createFor(T element, long objectId) {
        if (element instanceof Node) {
            return ClassUtils.unsafeCast(new NodeTreeNode<>((Node) element, objectId));
        } else if (element instanceof Geometry) {
            return ClassUtils.unsafeCast(new GeometryTreeNode<>((Geometry) element, objectId));
        } else if (element instanceof Mesh) {
            throw new UnsupportedOperationException();
        } else if (element instanceof Material) {
            throw new UnsupportedOperationException();
        }

        return null;
    }
}
