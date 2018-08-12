package mini.editor.extension.property;

public interface EditableProperty<T, C> {
    /**
     * @return the property type
     */
    EditablePropertyType getType();

    String getName();

    /**
     * @return the current value.
     */
    T getValue();

    void setValue(final T value);

    boolean isReadOnly();
}
