package mini.utils.clone;

/**
 *  A CloneFunction implementation that simply returns the
 *  the passed object without cloning it.  This is useful for
 *  forcing some object types (like Meshes) to be shared between
 *  the original and cloned object graph.
 */
public class IdentityCloneFunction<T> implements CloneFunction<T> {

    /**
     *  Returns the object directly.
     */
    public T cloneObject( Cloner cloner, T object ) {
        return object;
    }

    /**
     *  Does nothing.
     */
    public void cloneFields( Cloner cloner, T clone, T object ) {
    }
}