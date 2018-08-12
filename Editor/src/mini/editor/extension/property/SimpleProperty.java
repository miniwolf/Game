package mini.editor.extension.property;

import mini.scene.Spatial;

public class SimpleProperty<T, O> implements EditableProperty<T, O> {
    private final String name;
    private final float scrollPower;
    private final float minValue;
    private final float maxValue;
    private final O object;
    private final String extension;
    private final EditablePropertyType type;
    private final Getter<O, T> getter;
    private final Setter<O, T> setter;

    public SimpleProperty(
            EditablePropertyType type,
            String name,
            O object,
            Getter<O, T> getter,
            Setter<O, T> setter) {
        this(type, name, 1F, Integer.MIN_VALUE, Integer.MAX_VALUE, object, getter, setter);
    }

    public SimpleProperty(
            EditablePropertyType type,
            String name,
            float scrollPower,
            O object,
            Getter<O, T> getter,
            Setter<O, T> setter) {
        this(type, name, scrollPower, Integer.MIN_VALUE, Integer.MAX_VALUE, object, getter, setter);
    }

    public SimpleProperty(
            EditablePropertyType type,
            String name,
            float scrollPower,
            float minValue,
            float maxValue,
            O object,
            Getter<O, T> getter,
            Setter<O, T> setter) {
        this(type, name, scrollPower, minValue, maxValue, object, null, getter, setter);
    }

    public SimpleProperty(
            EditablePropertyType type,
            String name,
            float scrollPower,
            float minValue,
            float maxValue,
            O object,
            String extension,
            Getter<O, T> getter,
            Setter<O, T> setter) {

        this.type = type;
        this.name = name;
        this.scrollPower = scrollPower;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.object = object;
        this.extension = extension;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public EditablePropertyType getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T getValue() {
        return getter.get(object);
    }

    @Override
    public void setValue(T value) {
        if (setter == null) {
            throw new IllegalStateException("This property " + this + " is read only. Setter was null");
        }
        setter.set(object, value);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    public O getObject() {
        return object;
    }
}
