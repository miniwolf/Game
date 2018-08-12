package mini.editor.ui.control.property.impl;

import com.ss.rlib.common.util.ObjectUtils;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.control.property.PropertyControl;

import java.util.function.Function;

public class DefaultPropertyControl<C extends ChangeConsumer, D, T> extends PropertyControl<C, D, T> {
    private Function<T, String> toStringFunction;
    private Label propertyValueLabel;

    public DefaultPropertyControl(
            T propertyValue,
            String name,
            C changeConsumer) {
        super(propertyValue, name, changeConsumer);
    }

    @Override
    @FxThread
    protected void createComponents(HBox container) {
        propertyValueLabel = new Label();
        propertyValueLabel.prefWidthProperty().bind(container.widthProperty());
    }

    @Override
    public void reload() {
        var function = getToStringFunction();
        var propertyValueLabel = getPropertyValueLabel();
        propertyValueLabel.setText(function == null
                ? String.valueOf(getPropertyValue())
                : function.apply(getPropertyValue()));
    }

    @Override
    protected void apply() {
    }

    public Function<T, String> getToStringFunction() {
        return toStringFunction;
    }

    public void setToStringFunction(Function<T, String> toStringFunction) {
        this.toStringFunction = toStringFunction;
    }

    public Label getPropertyValueLabel() {
        return ObjectUtils.notNull(propertyValueLabel);
    }
}
