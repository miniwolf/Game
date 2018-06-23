package mini.editor.ui.component.painting;

import javafx.scene.image.Image;

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
}
