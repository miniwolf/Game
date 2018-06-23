package mini.editor.ui.component.editor.state;

import java.io.Serializable;

/**
 * State container for the Editor
 */
public interface EditorState extends Serializable {
    /**
     * Sets the change handler.
     *
     * @param handle the change handler.
     */
    void setChangeHandler(Runnable handle);
}
