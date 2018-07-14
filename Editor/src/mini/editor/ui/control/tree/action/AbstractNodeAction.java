package mini.editor.ui.control.tree.action;

import com.ss.rlib.common.util.ClassUtils;
import com.ss.rlib.common.util.array.Array;
import javafx.scene.control.MenuItem;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.control.tree.NodeTree;
import mini.editor.ui.control.tree.TreeNode;

public abstract class AbstractNodeAction<C extends ChangeConsumer> extends MenuItem
        implements Comparable<MenuItem> {
    private final NodeTree<C> nodeTree;
    private final Array<TreeNode<?>> nodes;

    public AbstractNodeAction(NodeTree<?> nodeTree, Array<TreeNode<?>> nodes) {
        this.nodeTree = ClassUtils.unsafeCast(nodeTree);
        this.nodes = nodes;

        setOnAction(event -> process());
        setText(getName());
    }

    protected abstract void process();

    protected abstract String getName();

    @Override
    public int compareTo(MenuItem o) {
        return 0;
    }

    public Array<TreeNode<?>> getNodes() {
        return nodes;
    }

    protected NodeTree<C> getNodeTree() {
        return nodeTree;
    }
}
