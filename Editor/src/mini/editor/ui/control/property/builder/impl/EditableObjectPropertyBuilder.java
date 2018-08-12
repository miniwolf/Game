package mini.editor.ui.control.property.builder.impl;

import com.ss.rlib.common.util.ClassUtils;
import com.ss.rlib.common.util.ObjectUtils;
import javafx.scene.layout.VBox;
import mini.editor.extension.property.EditableProperty;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.control.property.PropertyControl;
import mini.editor.ui.control.property.impl.EnumPropertyControl;
import mini.editor.ui.control.property.impl.QuaternionPropertyControl;
import mini.editor.ui.control.property.impl.Vector3fPropertyControl;
import mini.editor.util.EditorUtil;
import mini.math.Quaternion;
import mini.math.Vector3f;

import java.util.List;

public abstract class EditableObjectPropertyBuilder<C extends ChangeConsumer>
        extends AbstractPropertyBuilder<C> {
    protected EditableObjectPropertyBuilder(Class<? extends C> type) {
        super(type);
    }

    @Override
    protected void buildForImpl(
            Object object,
            Object parent,
            VBox container,
            C changeConsumer) {
        var properties = getProperties(object, parent, changeConsumer);
        if (properties == null || properties.isEmpty()) {
            return;
        }

        for (var description : properties) {
            buildFor(container, changeConsumer, description);
        }
    }

    protected void buildFor(
            VBox container,
            C changeConsumer,
            EditableProperty<?, ?> description) {
        var type = description.getType();

        switch (type) {
            case ENUM: {
                EditableProperty<Enum<?>, ?> property = cast(description);
                var value = ObjectUtils.notNull(property.getValue(), "Enum value cannot be null");
                var availableValues = EditorUtil.getAvailableValues(value);

                var propertyControl = new EnumPropertyControl<C, EditableProperty<Enum<?>, ?>, Enum<?>>(
                        value,
                        property.getName(),
                        changeConsumer,
                        availableValues);

                addControl(container, property, propertyControl);
                break;
            }
            case VECTOR_3F: {
                EditableProperty<Vector3f, ?> property = cast(description);
                var currentValue = property.getValue();

                var propertyControl = new Vector3fPropertyControl<C, EditableProperty<Vector3f, ?>>(
                        currentValue,
                        property.getName(),
                        changeConsumer);

                addControl(container, property, propertyControl);
                break;
            }
            case QUATERNION: {
                EditableProperty<Quaternion, ?> property = cast(description);
                var currentValue = property.getValue();

                var propertyControl = new QuaternionPropertyControl<C, EditableProperty<Quaternion, ?>>(
                        currentValue,
                        property.getName(),
                        changeConsumer);

                addControl(container, property, propertyControl);
                break;
            }
            case SEPARATOR: {
                buildSplitLine(container);
                break;
            }
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }

    protected <T> void addControl(
            VBox container,
            EditableProperty<T, ?> property,
            PropertyControl<? extends C, EditableProperty<T, ?>, T> propertyControl) {
        propertyControl.setApplyHandler(EditableProperty::setValue);
        propertyControl.setSyncHandler(EditableProperty::getValue);
        propertyControl.setEditObject(property);
        propertyControl.setDisable(property.isReadOnly());

        container.getChildren().add(propertyControl);
    }

    protected List<EditableProperty<?, ?>> getProperties(
            Object object,
            Object parent,
            C changeConsumer) {
        return getProperties(object);
    }

    protected abstract List<EditableProperty<?, ?>> getProperties(Object object);

    protected <T> EditableProperty<T, ?> cast(EditableProperty<?, ?> property) {
        return ClassUtils.unsafeCast(property);
    }
}
