package mini.utils.clone;

/**
 * Provides custom cloning for a particular object type. Once registered with the Cloner, this
 * function object will be called twice for any cloned object that matches the class for which it
 * was registered.
 * It will first call cloneObject() to shallow clone the object and then call cloneFields() to deep
 * clone the object's values.
 * <p>
 * This two step process is important because this is what allows circular references in the cloned
 * object graph.
 */
public interface CloneFunction<T> {
    /**
     * Performs a shallow clone of the specified object.
     * Once the shallow clone is created, it is cached and CloneFunction.cloneFields() is called. In
     * this way, the CloneFunction interface can completely take over the style of cloning an object
     * that doesn't otherwise implement a suitable interface.
     *
     * @param cloner The cloner performing the cloning operation.
     * @param object The original object that needs to be cloned.
     */
    T cloneObject(Cloner cloner, T object);

    /**
     * Performs a deep clone of the specified clone's field.
     * Once the shallow clone is created, it is cached and CloneFunction.cloneFields() is called. In
     * this way, the CloneFunction interface can completely take over the style of cloning an object
     * that doesn't otherwise implement a suitable interface.
     *
     * @param cloner The cloner performing the cloning operation.
     * @param result The clone previously returned from cloneObject().
     * @param object The original object that was cloned. This is provided for the very special case
     *               where field cloning needs to refer to the original object. Mostly necessary
     *               fields should already be on the clone.
     */
    void cloneFields(Cloner cloner, T result, T object);
}
