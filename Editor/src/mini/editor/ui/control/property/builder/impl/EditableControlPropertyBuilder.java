package mini.editor.ui.control.property.builder.impl;

import mini.editor.annotation.FxThread;
import mini.editor.extension.property.EditableProperty;
import mini.editor.extension.scene.control.EditableControl;
import mini.editor.model.undo.editor.ModelChangeConsumer;

import java.util.List;

public class EditableControlPropertyBuilder extends EditableModelObjectPropertyBuilder {
    private static final EditableControlPropertyBuilder INSTANCE = new EditableControlPropertyBuilder();

    private EditableControlPropertyBuilder() {
        super(ModelChangeConsumer.class);
    }

    public static EditableControlPropertyBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    @FxThread
    protected List<EditableProperty<?, ?>> getProperties(Object object) {
        if (!(object instanceof EditableControl)) {
            return null;
        }
        throw new UnsupportedOperationException();
    }
}
