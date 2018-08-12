package mini.editor.ui.control.property.builder.impl;

import mini.animation.SkeletonControl;
import mini.editor.annotation.FxThread;
import mini.editor.extension.property.EditableProperty;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.scene.AbstractControl;
import mini.scene.control.Control;
import mini.scene.control.LightControl;

import java.util.ArrayList;
import java.util.List;

public class DefaultControlPropertyBuilder extends EditableModelObjectPropertyBuilder {
    private static final DefaultControlPropertyBuilder INSTANCE = new DefaultControlPropertyBuilder();

    private DefaultControlPropertyBuilder() {
        super(ModelChangeConsumer.class);
    }

    public static DefaultControlPropertyBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    @FxThread
    protected List<EditableProperty<?, ?>> getProperties(Object object) {
        var properties = new ArrayList<EditableProperty<?, ?>>();

        if (!(object instanceof Control)) {
            return properties;
        }

        if (object instanceof LightControl) {
            throw new UnsupportedOperationException();
        } else if (object instanceof SkeletonControl) {
            throw new UnsupportedOperationException();
        } else if (object instanceof AbstractControl) {
            throw new UnsupportedOperationException();
        }

        return properties;
    }
}
