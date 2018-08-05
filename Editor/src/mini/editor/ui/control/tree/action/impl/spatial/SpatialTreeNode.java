package mini.editor.ui.control.tree.action.impl.spatial;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.ui.control.tree.NodeTree;
import mini.editor.ui.control.tree.TreeNode;
import mini.light.LightList;
import mini.scene.Spatial;
import mini.scene.control.Control;

public class SpatialTreeNode<T extends Spatial> extends TreeNode<T> {
    public SpatialTreeNode(final T element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FxThread
    public Array<TreeNode<?>> getChildren(NodeTree<?> nodeTree) {
        final Array<TreeNode<?>> result = ArrayFactory.newArray(TreeNode.class);

        final Spatial element = getElement();
        final LightList lightList = element.getLocalLightList();
        lightList.forEach(light -> result.add(FACTORY_REGISTRY.createFor(light)));

        final int numControls = element.getNumControls();
        for (int i = 0; i < numControls; i++) {
            final Control control = element.getControl(i);
            result.add(FACTORY_REGISTRY.createFor(control));
        }

        return result;
    }

    @Override
    @FromAnyThread
    public String getName() {
        final String name = getElement().getName();
        return name == null ? "name is null" : name;
    }
}
