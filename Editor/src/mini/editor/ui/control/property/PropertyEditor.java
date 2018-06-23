package mini.editor.ui.control.property;

import javafx.scene.control.ScrollPane;
import mini.editor.model.undo.editor.ChangeConsumer;

public class PropertyEditor<T extends ChangeConsumer> extends ScrollPane {
    private Object currentObject;

    public void buildFor(Object object, Object parent) {
        if (getCurrentObject() == object) {
            return;
        }

        throw new UnsupportedOperationException();
    }

    public Object getCurrentObject() {
        return currentObject;
    }
}
