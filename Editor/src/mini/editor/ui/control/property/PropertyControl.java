package mini.editor.ui.control.property;

import com.ss.rlib.common.function.SixObjectConsumer;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.control.property.operation.PropertyOperation;
import mini.editor.ui.css.CssClasses;
import mini.editor.util.ObjectsUtil;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class PropertyControl<C extends ChangeConsumer, D, T> extends VBox {

    public interface ChangeHandler<C, D, T> extends SixObjectConsumer<C, D, String, T, T, BiConsumer<D, T>> {
    }

    public static final double CONTROL_WIDTH_PERCENTAGE = 0.4;
    public static final double CONTROL_WIDTH_PERCENTAGE_2 = 0.6;
    private final C changeConsumer;
    private final SixObjectConsumer<C, D, String, T, T, BiConsumer<D, T>> changeHandler;
    private Function<D, T> syncHandler;

    private boolean ignoreListener;
    private BiConsumer<D, T> applyHandler;
    private String propertyName;
    private Label propertyNameLabel;
    private D editObject;
    private T propertyValue;

    public PropertyControl(T propertyValue, String propertyName, C changeConsumer) {
        this(propertyValue, propertyName, changeConsumer, null);
    }

    public PropertyControl(
            T propertyValue,
            String propertyName,
            C changeConsumer,
            ChangeHandler<C, D, T> changeHandler) {
        this.propertyName = propertyName;
        this.changeConsumer = changeConsumer;
        this.changeHandler = changeHandler == null ? newChangeHandler() : changeHandler;
        setPropertyValue(propertyValue);

        createComponents();
        setIgnoreListener(true);
        try {
            reload();
        } finally {
            setIgnoreListener(false);
        }
    }

    @FromAnyThread
    private ChangeHandler<C, D, T> newChangeHandler() {
        return (changeConsumer, object, propName, newValue, oldValue, handler) -> {
            var operation = new PropertyOperation<>(object, propName, newValue, oldValue);
            operation.setApplyHandler(handler);

            changeConsumer.execute(operation);
        };
    }

    private void setPropertyValue(T propertyValue) {
        this.propertyValue = propertyValue;
    }

    private void createComponents() {
        setAlignment(isSingleRow() ? Pos.CENTER_RIGHT : Pos.CENTER);

        var container = new HBox();
        container.setAlignment(getAlignment());

        propertyNameLabel = new Label(getPropertyName() + ":");
        if (isSingleRow()) {
            propertyNameLabel.maxWidthProperty()
                             .bind(widthProperty().multiply(1F - CONTROL_WIDTH_PERCENTAGE));
        }

        container.getStyleClass().add(CssClasses.DEF_HBOX);
        propertyNameLabel.getStyleClass().add(getLabelCssClass());

        createComponents(container);
    }

    private String getLabelCssClass() {
        return isSingleRow()
               ? CssClasses.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW
               : CssClasses.ABSTRACT_PARAM_CONTROL_PARAM_NAME;

    }

    /**
     * Apply new value to the edit object.
     */
    @FxThread
    protected void changed(T newValue, T oldValue) {
        changeHandler.accept(
                getChangeConsumer(),
                getEditObject(),
                getPropertyName(),
                newValue,
                oldValue,
                getApplyHandler());
    }

    protected abstract void createComponents(HBox container);

    protected abstract void reload();

    protected abstract void apply();

    protected void applyOnLostFocus(boolean focused) {
        if (!isIgnoreListener() && !focused) {
            apply();
        }
    }

    public BiConsumer<D,T> getApplyHandler() {
        return applyHandler;
    }

    public void setApplyHandler(BiConsumer<D,T> applyHandler) {
        this.applyHandler = applyHandler;
    }

    public Function<D, T> getSyncHandler() {
        return syncHandler;
    }

    public void setSyncHandler(Function<D, T> syncHandler) {
        this.syncHandler = syncHandler;
    }

    @FromAnyThread
    protected boolean isSingleRow() {
        return false;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public T getPropertyValue() {
        return propertyValue;
    }

    public C getChangeConsumer() {
        return changeConsumer;
    }

    public D getEditObject() {
        return ObjectsUtil.notNull(editObject);
    }

    public void setEditObject(D editObject) {
        this.editObject = editObject;
    }

    protected void setIgnoreListener(boolean ignoreListener) {
        this.ignoreListener = ignoreListener;
    }

    protected boolean isIgnoreListener() {
        return ignoreListener;
    }
}
