package mini.utils.clone;

/**
 * Indicates an object that wishes to more actively participate in the two-part deep copying process
 * provided by the <code>Cloner</code>. Objects implementing this interface can access the already
 * cloned object graph to resolve their local dependencies in a way that will be equivalent to the
 * original object graph. In other words, if two objects in the graph share the same target
 * reference then the cloned version will share the cloned reference.
 * <p>
 * <p>For example, if an object wishes to deep clone one of its fields then it will call
 * <code>cloner.clone(object</code> instead of <code>object.clone()</code>. The cloner will keep
 * track of any clones already created for 'object' and return that instead of a new clone.</p>
 * <p>
 * <p>Cloning of a MiniCloneable object is done in two parts. First, the standard Java
 * <code>clone()</code> method is called to create a shallow clone of the object. Secondly, the
 * cloner will call the <code>cloneFields()</code> method to let the object deep clone any of its
 * fields that should be cloned.</p>
 * <p>
 * <p>This two part process is necessary to facilitate circular references. When an object calls
 * <code>cloner.clone()</code> during its <code>cloneFields()</code> method, it may get only a
 * shallow clone that will be filled in later.</p>
 */
public interface MiniCloneable extends Cloneable {
    /**
     * Performs a shallow clone of the object. Some fields may also be cloned but generally only if
     * they will never be shared with other objects. (An example, local <code>Vector3f</code>s and
     * so on.)
     * <p>
     * <p>This method is separate from the regular <code>clone()</code> method so that objects might
     * still maintain their own regular java <code>clone()</code> semantics (perhaps even using
     * <code>Cloner</code> for those methods). However, because Java's <code>clone()</code> has
     * specific features in the sense of Object's clone() implementation, it's usually best to have
     * some path for subclasses to bypass the public clone() method that might be cloning fields and
     * instead get at the superclass protected clone() methods. For example, through
     * <code>super.miniClone()</code> or another protected clone method that some base class
     * eventually calls <code>super.clone()</code> in.</p>
     */
    Object miniClone();

    /**
     * Implemented to perform deep cloning for this object, resolving local cloned references using
     * the specified cloner. The object can call cloner.clone(fieldValue) to deep clone any of its
     * fields.
     *
     * @param cloner   The cloner that is performing the cloning operation. The cloneFields method can
     *                 call back into the cloner to make clones if its subordinate fields.
     * @param original The original object from which this object was cloned. This is provided for
     *                 the very rare case that this object needs to refer to its original for some
     *                 reason. In general, all of the relevant values should have been transferred
     *                 during the shallow clone and this object need merely clone what it wants.
     */
    void cloneFields(Cloner cloner, Object original);
}
