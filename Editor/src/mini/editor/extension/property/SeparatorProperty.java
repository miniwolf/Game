package mini.editor.extension.property;

public class SeparatorProperty implements EditableProperty<Object, Object> {
    private static final EditableProperty<?, ?> INSTANCE = new SeparatorProperty();

    public static EditableProperty<?,?> getInstance() {
        return INSTANCE;
    }

    @Override
    public EditablePropertyType getType() {
        return EditablePropertyType.SEPARATOR;
    }

    @Override
    public String getName() {
        return "separator";
    }

    @Override
    public Object getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
