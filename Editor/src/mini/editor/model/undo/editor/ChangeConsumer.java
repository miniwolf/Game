package mini.editor.model.undo.editor;

import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.model.undo.EditorOperation;

/**
 * Notify about any changes
 */
public interface ChangeConsumer {
    @FromAnyThread
    void execute(EditorOperation operation);

    @FxThread
    void notifyJavaFXRemovedChild(Object parent, Object removed);
}
