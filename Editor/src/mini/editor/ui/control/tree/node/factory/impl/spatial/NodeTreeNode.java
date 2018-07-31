package mini.editor.ui.control.tree.node.factory.impl.spatial;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.editor.annotation.FxThread;
import mini.editor.ui.control.model.ModelNodeTree;
import mini.editor.ui.control.tree.NodeTree;
import mini.editor.ui.control.tree.TreeNode;
import mini.editor.ui.control.tree.action.impl.spatial.SpatialTreeNode;
import mini.scene.Node;
import mini.scene.Spatial;

import java.util.List;

public class NodeTreeNode<V extends Node> extends SpatialTreeNode<V> {

    public NodeTreeNode(
            final V element,
            final long objectId) {
        super(element, objectId);
    }

    @Override
    @FxThread
    public boolean hasChildren(final NodeTree<?> nodeTree) {
        return nodeTree instanceof ModelNodeTree;
    }

    @Override
    @FxThread
    public Array<TreeNode<?>> getChildren(final NodeTree<?> nodeTree) {
        //final V element = getElement();
        final Array<TreeNode<?>> result = ArrayFactory.newArray(TreeNode.class);
        final List<Spatial> children = getSpatialChildren();
        for (final Spatial child : children) {
            // TODO: filters, maybe as a BiPredicate::test call.
            result.add(FACTORY_REGISTRY.createFor(child));
        }

        result.addAll(super.getChildren(nodeTree));
        return result;
    }

    /**
     * @return the children of the spatial represented in this node.
     */
    private List<Spatial> getSpatialChildren() {
        final Node element = getElement();
        return element.getChildren();
    }
}
