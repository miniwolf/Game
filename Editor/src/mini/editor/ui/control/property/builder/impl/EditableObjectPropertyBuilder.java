package mini.editor.ui.control.property.builder.impl;

import javafx.scene.layout.VBox;
import mini.editor.extension.property.EditableProperty;
import mini.editor.model.undo.editor.ChangeConsumer;

import java.util.List;

public abstract class EditableObjectPropertyBuilder<C extends ChangeConsumer>
        extends AbstractPropertyBuilder<C> {
    protected EditableObjectPropertyBuilder(Class<? extends C> type) {
        super(type);
    }

    @Override
    protected void buildForImpl(Object object, Object parent, VBox container,
                                C changeConsumer) {
        var properties = getProperties(object, parent, changeConsumer);
        if (properties == null || properties.isEmpty()) {
            return;
        }

        for (var description : properties) {
            buildFor(container, changeConsumer, description);
        }
    }

    private void buildFor(
            VBox container,
            C changeConsumer,
            EditableProperty<?, ?> description) {
        var type = description.getType();

        switch (type) {
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected List<EditableProperty<?, ?>> getProperties(
            Object object,
            Object parent,
            C changeConsumer) {
        return getProperties(object);
    }

    protected abstract List<EditableProperty<?, ?>> getProperties(Object object);
}
