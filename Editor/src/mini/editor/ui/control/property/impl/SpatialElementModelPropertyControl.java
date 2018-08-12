package mini.editor.ui.control.property.impl;

import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.scene.Spatial;

public class SpatialElementModelPropertyControl<T extends Spatial, D> extends ElementModelPropertyControl<D, T> {
    public SpatialElementModelPropertyControl(
            Class<T> type,
            T value,
            String name,
            ModelChangeConsumer changeConsumer) {
        super(type, value, name, changeConsumer);
    }

    @Override
    @FxThread
    protected void reload() {
        var spatial = getPropertyValue();

        String name = spatial == null ? null : spatial.getName();
        name = name == null && spatial != null ? spatial.getClass().getSimpleName() : name;

        getElementLabel().setText(name == null ? NO_ELEMENT : name);
    }

    @Override
    protected void apply() {
    }
}
