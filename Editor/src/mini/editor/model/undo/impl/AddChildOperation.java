package mini.editor.model.undo.impl;

import mini.editor.Messages;
import mini.editor.model.undo.EditorOperation;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.model.undo.editor.model.undo.impl.AbstractEditorOperation;
import mini.scene.AssetLinkNode;
import mini.scene.Node;
import mini.scene.Spatial;

public class AddChildOperation extends AbstractEditorOperation<ModelChangeConsumer> {
    private final Spatial newChild;
    private final Node parent;
    private final boolean needSelect;

    public AddChildOperation(
            final Spatial newChild,
            final Node parent,
            boolean needSelect) {
        this.newChild = newChild;
        this.parent = parent;
        this.needSelect = needSelect;
    }

    @Override
    protected void redoImpl(ModelChangeConsumer editor) {
        EXECUTOR_MANAGER.addEditorTask(() -> {
            editor.notifyEditorPreChangedProperty(newChild, Messages.MODEL_PROPERTY_TRANSFORMATION);
            parent.attachChildAt(newChild, 0);
            editor.notifyEditorChangedProperty(newChild, Messages.MODEL_PROPERTY_TRANSFORMATION);

            EXECUTOR_MANAGER.addFXTask(() -> editor.notifyJavaFXAddedChild(parent, newChild, 0, needSelect));
        });
    }
}
