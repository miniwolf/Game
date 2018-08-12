package mini.editor.extension.property;

public interface Setter<O, P> {
    void set(O object, P property);
}
