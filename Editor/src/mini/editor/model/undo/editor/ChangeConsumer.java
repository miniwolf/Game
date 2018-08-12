package mini.editor.model.undo.editor;

import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.annotation.EditorThread;
import mini.editor.model.undo.EditorOperation;
import mini.scene.Node;
import mini.scene.Spatial;

/**
 * Notify about any changes
 */
public interface ChangeConsumer {
    @FromAnyThread
    void execute(EditorOperation operation);

    @FxThread
    void notifyJavaFXRemovedChild(Object parent, Object removed);

    @EditorThread
    void notifyEditorPreChangedProperty(Object object, String propertyName);

    @EditorThread
    void notifyEditorChangedProperty(Object object, String propertyName);

    @FxThread
    void notifyJavaFXChangedProperty(Object object, String propertyName);

    @FxThread
    void notifyJavaFXAddedChild(Object parent, Object added, int index, boolean needSelect);
}
