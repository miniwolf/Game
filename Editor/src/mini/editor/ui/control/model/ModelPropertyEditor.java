package mini.editor.ui.control.model;

import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.ui.control.property.PropertyEditor;

/**
 * The component to contain property controls in the editor.
 */
public class ModelPropertyEditor extends PropertyEditor<ModelChangeConsumer> {
    public ModelPropertyEditor(final ModelChangeConsumer changeConsumer) {
        super(changeConsumer);
    }
}
