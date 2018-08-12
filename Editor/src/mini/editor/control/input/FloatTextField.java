package mini.editor.control.input;

import javafx.scene.control.TextFormatter;
import javafx.scene.input.ScrollEvent;
import javafx.util.StringConverter;
import mini.editor.util.converter.LimitedFloatStringConverter;

public class FloatTextField extends TypedTextField<Float> {
    public float getValue() {
        var textFormatter = getTypedTextFormatter();
        var value = textFormatter.getValue();
        return value == null ? 0F : value;
    }

    @Override
    protected TextFormatter<Float> createTextFormatter() {
        return new TextFormatter<>(new LimitedFloatStringConverter());
    }

    @Override
    protected void scrollValueImpl(ScrollEvent event) {
        var value = getValue();
        var longValue = (long) (value * 1000);
        longValue += event.getDeltaY() * (getScrollPower() * (event.isShiftDown() ? 0.5F : 1.0F));

        var resultValue = longValue / 1000F;
        var stringValue = String.valueOf(resultValue);

        var textFormatter = getTextFormatter();
        var valueConverter = textFormatter.getValueConverter();
        try {
            valueConverter.fromString(stringValue);
        } catch (RuntimeException e) {
            return;
        }

        setText(stringValue);
        positionCaret(stringValue.length());
    }

    public void setValue(float value) {
        setText(String.valueOf(value));
    }
}
