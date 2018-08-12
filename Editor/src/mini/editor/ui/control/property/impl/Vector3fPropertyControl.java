package mini.editor.ui.control.property.impl;

import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import mini.editor.annotation.FxThread;
import mini.editor.control.input.FloatTextField;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.control.property.PropertyControl;
import mini.editor.ui.css.CssClasses;
import mini.editor.util.ObjectsUtil;
import mini.editor.util.UIUtils;
import mini.math.Vector3f;

public class Vector3fPropertyControl<C extends ChangeConsumer, D> extends PropertyControl<C, D, Vector3f> {
    private FloatTextField xField;
    private FloatTextField yField;
    private FloatTextField zField;

    public Vector3fPropertyControl(Vector3f propertyValue, String propertyName, C changeConsumer) {
        super(propertyValue, propertyName, changeConsumer);
    }

    @Override
    protected void createComponents(HBox container) {

        xField = new FloatTextField();
        xField.setOnKeyReleased(this::keyReleased);
        xField.setScrollPower(getScrollPower());
        xField.prefWidthProperty().bind(widthProperty().divide(3));


        yField = new FloatTextField();
        yField.setOnKeyReleased(this::keyReleased);
        yField.setScrollPower(getScrollPower());
        yField.prefWidthProperty().bind(widthProperty().divide(3));


        zField = new FloatTextField();
        zField.setOnKeyReleased(this::keyReleased);
        zField.setScrollPower(getScrollPower());
        zField.prefWidthProperty().bind(widthProperty().divide(3));

        xField.valueProperty().addListener(((observable, oldValue, newValue) -> changeValue()));
        yField.valueProperty().addListener(((observable, oldValue, newValue) -> changeValue()));
        zField.valueProperty().addListener(((observable, oldValue, newValue) -> changeValue()));

        var xLabel = new Label("x:");
        var yLabel = new Label("y:");
        var zLabel = new Label("z:");

        xLabel.getStyleClass().add(CssClasses.ABSTRACT_PARAM_CONTROL_NUMBER_LABEL);
        yLabel.getStyleClass().add(CssClasses.ABSTRACT_PARAM_CONTROL_NUMBER_LABEL);
        zLabel.getStyleClass().add(CssClasses.ABSTRACT_PARAM_CONTROL_NUMBER_LABEL);

        container.getStyleClass().addAll(
                CssClasses.DEF_HBOX,
                CssClasses.TEXT_INPUT_CONTAINER,
                CssClasses.ABSTRACT_PARAM_CONTROL_INPUT_CONTAINER);

        xField.getStyleClass().addAll(
                CssClasses.PROPERTY_CONTROL_VECTOR_3F_FIELD,
                CssClasses.TRANSPARENT_TEXT_FIELD);

        container.getChildren().addAll(
                xLabel, xField,
                yLabel, yField,
                zLabel, zField);

        UIUtils.addFocusBinding(container, xField, yField, zField)
                .addListener(((observable, oldValue, newValue) -> applyOnLostFocus(newValue)));
    }

    @FxThread
    private float getScrollPower() {
        return 10F;
    }

    @FxThread
    private void keyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            changeValue();
        }
    }

    @FxThread
    private void changeValue() {
        if (!isIgnoreListener()) {
            apply();
        }
    }

    @FxThread
    @Override
    protected void apply() {
        var x = getXField().getValue();
        var y = getYField().getValue();
        var z = getZField().getValue();

        var storedValue = getPropertyValue() == null ? Vector3f.ZERO : getPropertyValue();
        changed(new Vector3f(x, y, z), storedValue.clone());
    }

    @FxThread
    private FloatTextField getXField() {
        return ObjectsUtil.notNull(xField);
    }

    @FxThread
    private FloatTextField getYField() {
        return ObjectsUtil.notNull(yField);
    }

    @FxThread
    private FloatTextField getZField() {
        return ObjectsUtil.notNull(zField);
    }

    @Override
    protected void reload() {

    }
}
