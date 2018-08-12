package mini.editor.ui.control.property.impl;

import com.ss.rlib.common.util.array.ArrayFactory;
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
import mini.math.FastMath;
import mini.math.Quaternion;

public class QuaternionPropertyControl<C extends ChangeConsumer, D> extends PropertyControl<C, D, Quaternion> {
    private FloatTextField xField;
    private FloatTextField yField;
    private FloatTextField zField;

    public QuaternionPropertyControl(
            Quaternion propertyValue,
            String propertyName,
            C changeConsumer) {
        super(propertyValue, propertyName, changeConsumer);
    }

    @Override
    protected void createComponents(HBox container) {
        xField = new FloatTextField();
        xField.setOnKeyReleased(this::keyReleased);
        xField.prefWidthProperty().bind(widthProperty().divide(3));

        yField = new FloatTextField();
        yField.setOnKeyReleased(this::keyReleased);
        yField.prefWidthProperty().bind(widthProperty().divide(3));

        zField = new FloatTextField();
        zField.setOnKeyReleased(this::keyReleased);
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

    private void changeValue() {
        if (!isIgnoreListener()) {
            apply();
        }
    }

    private void keyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            changeValue();
        }
    }

    @Override
    @FxThread
    protected void reload() {
        var angles = new float[3];
        var value = getPropertyValue();
        if (value != null) {
            value.toAngles(angles);
        }

        var xField = getXField();
        xField.setValue(angles[0] * FastMath.RAD_TO_DEG);
        xField.positionCaret(xField.getText().length());

        var yField = getYField();
        yField.setValue(angles[1] * FastMath.RAD_TO_DEG);
        yField.positionCaret(yField.getText().length());

        var zField = getZField();
        zField.setValue(angles[2] * FastMath.RAD_TO_DEG);
        zField.positionCaret(zField.getText().length());

    }

    @Override
    @FxThread
    protected void apply() {
        var oldValue = getPropertyValue();
        if (oldValue != null) {
            oldValue = oldValue.clone();
        }

        var x = getXField().getValue() * FastMath.DEG_TO_RAD;
        var y = getYField().getValue() * FastMath.DEG_TO_RAD;
        var z = getZField().getValue() * FastMath.DEG_TO_RAD;

        var newValue = new Quaternion()
                .fromAngles(ArrayFactory.toFloatArray(x, y, z));
        changed(newValue, oldValue);
    }


    public FloatTextField getXField() {
        return ObjectsUtil.notNull(xField);
    }

    public FloatTextField getYField() {
        return ObjectsUtil.notNull(yField);
    }

    public FloatTextField getZField() {
        return ObjectsUtil.notNull(zField);
    }
}
