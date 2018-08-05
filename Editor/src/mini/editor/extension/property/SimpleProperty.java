package mini.editor.extension.property;

public class SimpleProperty<T, O> implements EditableProperty<T, O> {
    private final String name;
    private final O object;
    private final EditablePropertyType type;

    public SimpleProperty(
            EditablePropertyType type,
            String name,
            O object) {
        this.type = type;
        this.name = name;
        this.object = object;
    }

    @Override
    public EditablePropertyType getType() {
        return type;
    }
}
