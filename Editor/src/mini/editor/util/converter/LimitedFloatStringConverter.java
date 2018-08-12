package mini.editor.util.converter;

import javafx.util.StringConverter;

public class LimitedFloatStringConverter extends StringConverter<Float> {
    private Float minValue;
    private Float maxValue;

    public LimitedFloatStringConverter() {
        minValue = Float.MIN_VALUE;
        maxValue = Float.MAX_VALUE;
    }

    @Override
    public String toString(Float value) {
        if (value == null) {
            return "";
        }

        return Float.toString(value);
    }

    @Override
    public Float fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        var result = Float.valueOf(value);
        if (result < minValue) {
            throw new IllegalArgumentException();
        } else if (result > maxValue) {
            throw new IllegalArgumentException();
        }

        return result;
    }
}
