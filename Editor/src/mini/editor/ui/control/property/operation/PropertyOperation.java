package mini.editor.ui.control.property.operation;

import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.model.undo.editor.model.undo.impl.AbstractEditorOperation;

import java.util.function.BiConsumer;

public class PropertyOperation<C extends ChangeConsumer, D, T> extends AbstractEditorOperation<C> {
    private final D target;
    private final String propertyName;
    private final T newValue;
    private final T oldValue;

    private BiConsumer<D, T> applyHandler;

    public PropertyOperation(
            final D target,
            final String propertyName,
            final T newValue,
            final T oldValue) {
        this.target = target;
        this.propertyName = propertyName;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    @Override
    protected void redoImpl(C editor) {
        EXECUTOR_MANAGER.addEditorTask(() -> {
            editor.notifyEditorPreChangedProperty(target, propertyName);
            apply(target, newValue);
            editor.notifyEditorChangedProperty(target, propertyName);
            EXECUTOR_MANAGER.addFXTask(() -> editor.notifyJavaFXChangedProperty(target, propertyName));
        });
    }

    public void setApplyHandler(final BiConsumer<D, T> applyHandler) {
        this.applyHandler = applyHandler;
    }

    private void apply(
            final D spatial,
            final T value) {
        applyHandler.accept(spatial, value);
    }
}
