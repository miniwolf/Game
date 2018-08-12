package mini.editor.ui.control.property.impl;

import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.ui.dialog.node.selector.NodeSelectorDialog;

public abstract class ElementModelPropertyControl<D, T> extends ElementPropertyControl<ModelChangeConsumer, D, T> {
    public ElementModelPropertyControl(
            Class<T> type,
            T propertyValue,
            String propertyName,
            ModelChangeConsumer changeConsumer) {
        super(type, propertyValue, propertyName, changeConsumer);
    }

    private NodeSelectorDialog<T> createNodeSelectorDialog() {
        return new NodeSelectorDialog<>(getChangeConsumer().getCurrentModel(), type, this::addElement);
    }

    @Override
    @FxThread
    protected void addElement() {
        createNodeSelectorDialog().show(this);
    }

    protected void addElement(T newElement) {
        changed(newElement, getPropertyValue());
    }
}
