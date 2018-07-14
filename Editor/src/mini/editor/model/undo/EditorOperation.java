package mini.editor.model.undo;

public interface EditorOperation {
    /**
     * Redo this operation using the editor.
     */
    void redo(final UndoableEditor editor);
}
