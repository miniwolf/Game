package mini.editor.ui.control.tree.node.factory;

import com.ss.rlib.common.util.ClassUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.editor.ui.control.tree.TreeNode;

import java.util.concurrent.atomic.AtomicLong;

public class TreeNodeFactoryRegistry {
    private static final TreeNodeFactoryRegistry INSTANCE = new TreeNodeFactoryRegistry();
    private static final AtomicLong ID_GENERATOR = new AtomicLong();

    private final Array<TreeNodeFactory> factories;

    public TreeNodeFactoryRegistry() {
        factories = ArrayFactory.newArray(TreeNodeFactory.class);

    }

    public static TreeNodeFactoryRegistry getInstance() {
        return INSTANCE;
    }

    public <T, V extends TreeNode<T>> V createFor(T element) {
        if (element instanceof TreeNode) {
            return ClassUtils.unsafeCast(element);
        }

        final long objectID = ID_GENERATOR.incrementAndGet();

        V result;

        final Array<TreeNodeFactory> factories = getFactories();
        for (TreeNodeFactory factory : factories) {
            result = factory.createFor(element, objectID);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public Array<TreeNodeFactory> getFactories() {
        return factories;
    }
}
