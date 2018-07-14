package mini.editor.model.undo;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.application.Platform;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.manager.ExecutorManager;

public class EditorOperationControl {

    private final UndoableEditor editor;
    private final Array<EditorOperation> operations;
    private final Array<EditorOperation> toRedo;

    public EditorOperationControl(final UndoableEditor editor) {
        this.editor = editor;
        operations = ArrayFactory.newArray(EditorOperation.class);
        toRedo = ArrayFactory.newArray(EditorOperation.class);
    }

    @FromAnyThread
    public void execute(final EditorOperation operation) {
        if (Platform.isFxApplicationThread()) {
            executeImpl(operation);
        } else {
            final ExecutorManager executorManager = ExecutorManager.getInstance();
            executorManager.addFXTask(() -> executeImpl(operation));
        }
    }

    @FxThread
    private void executeImpl(final EditorOperation operation) {
        final UndoableEditor editor = getEditor();
        operation.redo(editor);

        editor.incrementChange();

        final Array<EditorOperation> operations = getOperations();
        operations.add(operation);

        final Array<EditorOperation> toRedo = getToRedo();
        toRedo.clear();
    }

    public UndoableEditor getEditor() {
        return editor;
    }

    public Array<EditorOperation> getOperations() {
        return operations;
    }

    public Array<EditorOperation> getToRedo() {
        return toRedo;
    }
}
