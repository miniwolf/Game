package mini.editor.ui.control.model;

import com.ss.rlib.common.util.array.Array;
import javafx.scene.control.SelectionMode;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.ui.control.tree.NodeTree;
import mini.editor.ui.control.tree.action.impl.multi.RemoveElementsAction;

import java.util.function.Consumer;

/**
 * Represents a model in the editor. Lists elements and fields.
 */
public class ModelNodeTree extends NodeTree<ModelChangeConsumer> {
    static {
        register(RemoveElementsAction.ACTION_FILLER);
    }

    public ModelNodeTree(
            final Consumer<Array<Object>> selectionHandler,
            final ModelChangeConsumer consumer) {
        super(selectionHandler, consumer, SelectionMode.MULTIPLE);
    }

    public ModelNodeTree(
            final Consumer<Array<Object>> selectionHandler,
            final ModelChangeConsumer consumer,
            final SelectionMode single) {
        super(selectionHandler, consumer, single);
    }
}
