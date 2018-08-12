package mini.editor.ui.control.property.impl;

import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.control.property.PropertyControl;
import mini.editor.ui.css.CssClasses;

public class EnumPropertyControl<C extends ChangeConsumer, D, E extends Enum<?>> extends PropertyControl<C, D, E> {
    private ComboBox<E> enumComboBox;

    public EnumPropertyControl(
            E propertyValue,
            String propertyName,
            C changeConsumer,
            E[] availableValues) {
        super(propertyValue, propertyName, changeConsumer);
        getEnumComboBox().getItems().addAll(availableValues);
    }

    @Override
    protected void createComponents(HBox container) {
        enumComboBox = new ComboBox<>();
        enumComboBox.prefWidthProperty().bind(widthProperty().multiply(CONTROL_WIDTH_PERCENTAGE));

        enumComboBox.valueProperty().addListener((observable, oldValue, newValue) -> change());
        enumComboBox.getStyleClass().add(CssClasses.PROPERTY_CONTROL_COMBO_BOX);

        container.getChildren().add(enumComboBox);
    }

    @FxThread
    private void change() {
        if (isIgnoreListener()) {
            return;
        }

        var enumComboBox = getEnumComboBox();
        var selectionModel = enumComboBox.getSelectionModel();
        var newValue = selectionModel.getSelectedItem();

        changed(newValue, getPropertyValue());
    }

    @Override
    @FxThread
    protected void reload() {
        getEnumComboBox().getSelectionModel().select(getPropertyValue());
    }

    @Override
    protected void apply() {
    }

    public ComboBox<E> getEnumComboBox() {
        return enumComboBox;
    }
}
