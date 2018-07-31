package mini.editor.ui.control.tree.node.factory.impl.spatial;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.editor.ui.control.model.ModelNodeTree;
import mini.editor.ui.control.tree.NodeTree;
import mini.editor.ui.control.tree.TreeNode;
import mini.editor.ui.control.tree.action.impl.spatial.SpatialTreeNode;
import mini.material.Material;
import mini.scene.Geometry;
import mini.scene.Mesh;

public class GeometryTreeNode<T extends Geometry> extends SpatialTreeNode<T> {
    public GeometryTreeNode(
            final T element,
            final long objectId) {
        super(element, objectId);
    }

    @Override
    public Array<TreeNode<?>> getChildren(NodeTree<?> nodeTree) {
        if (!(nodeTree instanceof ModelNodeTree)) {
            return TreeNode.EMPTY_ARRAY;
        }

        final Array<TreeNode<?>> result = ArrayFactory.newArray(TreeNode.class);

        final Geometry geometry = getElement();
        final Mesh mesh = geometry.getMesh();
        final Material material = geometry.getMaterial();

        if (mesh != null) {
            result.add(FACTORY_REGISTRY.createFor(mesh));
        }

        result.add(FACTORY_REGISTRY.createFor(material));
        result.addAll(super.getChildren(nodeTree));

        return result;
    }
}
