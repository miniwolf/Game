package mini.editor.control.input;

import com.ss.rlib.common.util.ClassUtils;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.ScrollEvent;

public abstract class TypedTextField<T> extends TextField {
    private float scrollPower;

    public TypedTextField() {
        setTextFormatter(createTextFormatter());
        setOnScroll(this::scrollValue);
    }

    protected abstract TextFormatter<?> createTextFormatter();

    private void scrollValue(ScrollEvent event) {
        if (!event.isControlDown()) {
            return;
        }

        event.consume();
        scrollValueImpl(event);
    }

    protected abstract void scrollValueImpl(ScrollEvent event);

    protected TextFormatter<T> getTypedTextFormatter() {
        return ClassUtils.unsafeCast(getTextFormatter());
    }

    public float getScrollPower() {
        return scrollPower;
    }

    public void setScrollPower(float scrollPower) {
        this.scrollPower = scrollPower;
    }

    public ReadOnlyObjectProperty<T> valueProperty() {
        return getTypedTextFormatter().valueProperty();
    }
}
