package mini.editor.ui.control.property.builder;

import javafx.scene.layout.VBox;
import mini.editor.model.undo.editor.ChangeConsumer;

public interface PropertyBuilder {
    /**
     * Build properties controls for the object to the container.
     *
     * @param container      attach the controls to this container.
     * @param changeConsumer used for working between controls and editor.
     */
    void buildFor(
            Object object,
            Object parent,
            VBox container,
            ChangeConsumer changeConsumer);

    default int getPriority() {
        return 0;
    }

    default int compareTo(PropertyBuilder o) {
        return o.getPriority() - getPriority();
    }
}
