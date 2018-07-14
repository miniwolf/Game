package mini.editor.ui.control.tree.action.impl.multi;

import com.ss.rlib.common.function.TripleConsumer;
import com.ss.rlib.common.util.ObjectUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayCollectors;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.scene.control.MenuItem;
import mini.editor.Messages;
import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.model.undo.editor.model.undo.impl.RemoveElementsOperation;
import mini.editor.ui.control.tree.NodeTree;
import mini.editor.ui.control.tree.TreeNode;
import mini.editor.ui.control.tree.action.AbstractNodeAction;
import mini.editor.ui.control.tree.action.impl.spatial.SpatialTreeNode;

import java.util.List;

public class RemoveElementsAction extends AbstractNodeAction<ModelChangeConsumer> {

    private static final Array<Class<?>> AVAILABLE_TYPES = ArrayFactory.newArray(Class.class);
    public static final TripleConsumer<NodeTree<?>, List<MenuItem>, Array<TreeNode<?>>>
            ACTION_FILLER =
            ((nodeTree, menuItems, treeNodes) -> {
                final TreeNode<?> unexpectedItem = treeNodes.search(treeNode ->
                                                                            AVAILABLE_TYPES
                                                                                    .search(treeNode,
                                                                                            Class::isInstance)
                                                                            == null || !treeNode
                                                                                    .canRemove());

                if (unexpectedItem == null) {
                    menuItems.add(new RemoveElementsAction(nodeTree, treeNodes));
                }
            });

    static {
        AVAILABLE_TYPES.add(SpatialTreeNode.class);
    }

    public RemoveElementsAction(final NodeTree<?> nodeTree,
                                final Array<TreeNode<?>> treeNodes) {
        super(nodeTree, treeNodes);
    }

    @Override
    @FxThread
    protected void process() {
        final Array<TreeNode<?>> nodes = getNodes();
        final Array<RemoveElementsOperation.Element> toRemove =
                nodes.stream()
                     .filter(treeNode -> treeNode.getParent() != null)
                     .map(this::convert)
                     .collect(ArrayCollectors.toArray(RemoveElementsOperation.Element.class));

        final NodeTree<ModelChangeConsumer> nodeTree = getNodeTree();
        final ModelChangeConsumer changeConsumer = ObjectUtils
                .notNull(nodeTree.getChangeConsumer());
        changeConsumer.execute(new RemoveElementsOperation(toRemove));
    }

    @FxThread
    private RemoveElementsOperation.Element convert(final TreeNode<?> treeNode) {
        final Object value = treeNode.getElement();
        final TreeNode<?> parentNode = ObjectUtils.notNull(treeNode.getParent());
        final Object parent = parentNode.getElement();
        return new RemoveElementsOperation.Element(value, parent);
    }

    @Override
    @FxThread
    public String getName() {
        return Messages.MODEL_NODE_TREE_ACTION_REMOVE;
    }
}
