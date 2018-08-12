package mini.editor.extension.property;

public interface Getter<O, P> {
    /**
     * @return the current property value
     */
    P get(O object);
}
