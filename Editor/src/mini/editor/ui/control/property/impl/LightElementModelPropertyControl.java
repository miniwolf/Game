package mini.editor.ui.control.property.impl;

import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.light.Light;

public class LightElementModelPropertyControl<L extends Light, D> extends ElementModelPropertyControl<D, L> {
    public LightElementModelPropertyControl(
            Class<L> type,
            L propertyValue,
            String propertyName,
            ModelChangeConsumer changeConsumer) {
        super(type, propertyValue, propertyName, changeConsumer);
    }

    @Override
    @FxThread
    protected void reload() {
        var light = getPropertyValue();
        var elementLabel = getElementLabel();

        String name = light == null ? null : light.getName();
        name = name == null && light != null ? light.getClass().getSimpleName() : name;

        elementLabel.setText(name == null || name.isEmpty() ? NO_ELEMENT : name);
    }

    @Override
    protected void apply() {
    }
}
