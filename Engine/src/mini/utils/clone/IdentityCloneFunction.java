package mini.utils.clone;

/**
 * A CloneFunction implementation that simply returns the passed object without cloning it. This is
 * useful for forcing some object types (like Materials) to be shared between the original and
 * cloned object graph.
 */
public class IdentityCloneFunction<T> implements CloneFunction<T> {

    /**
     * @param cloner The cloner performing the cloning operation.
     * @param object The original object that needs to be cloned.
     * @return Returns the object directly.
     */
    @Override
    public T cloneObject(Cloner cloner, T object) {
        return object;
    }

    /**
     * Does nothing.
     */
    @Override
    public void cloneFields(Cloner cloner, T result, T object) {
    }
}
