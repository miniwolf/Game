package mini.editor.ui.component.painting;

import javafx.scene.image.Image;
import mini.editor.annotation.FxThread;
import mini.editor.ui.component.editor.state.EditorState;

public interface PaintingComponent {
    /**
     * @return icon of this component.
     */
    default Image getIcon() {
        return null;
    }

    /**
     * @return the name of this component.
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Callback when component is being hidden.
     */
    void notifyHiding();

    /**
     * Callback when component is being shown
     */
    void notifyShowed();

    /**
     * Stop painting of the last object.
     */
    void stopPainting();

    /**
     * Start painting process for the object.
     */
    void startPainting(Object object);

    /**
     * Checks that an object can be processed using this component.
     *
     * @param element the object to check
     * @return whether this object can be processed.
     */
    boolean isSupported(Object element);

    /**
     * Load the state of this component from the editor state.
     * @param editorState
     */
    @FxThread
    void loadState(EditorState editorState);
}
