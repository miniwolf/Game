package mini.editor.model.undo;

/**
 * Editor that supports undoing a command
 */
public interface UndoableEditor {
    /**
     * Count changes
     */
    void incrementChange();
}
