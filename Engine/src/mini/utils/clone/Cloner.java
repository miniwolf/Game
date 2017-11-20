package mini.utils.clone;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A deep clone utility that provides similar object-graph-preserving
 * qualities to typical serialization schemes.  An internal registry
 * of cloned objects is kept to be used by other objects in the deep
 * clone process that implement JmeCloneable.
 * <p>Enhanced object cloning is done in a two step process.  First,
 * the object is cloned using the normal Java clone() method and stored
 * in the clone registry.  After that, if it implements JmeCloneable then
 * its cloneFields() method is called to deep clone any of the fields.
 * This two step process has a few benefits.  First, it means that objects
 * can easily have a regular shallow clone implementation just like any
 * normal Java objects.  Second, the deep cloning of fields happens after
 * creation wich means that the clone is available to future field cloning
 * to resolve circular references.</p>
 * <p>
 * <p>Similar to Java serialization, the handling of specific object
 * types can be customized.  This allows certain objects to be cloned gracefully
 * even if they aren't normally Cloneable.  This can also be used as a
 * sort of filter to keep certain types of objects from being cloned.
 * (For example, adding the IdentityCloneFunction for Mesh.class would cause
 * all mesh instances to be shared with the original object graph.)</p>
 * <p>
 * <p>By default, the Cloner registers serveral default clone functions
 * as follows:</p>
 * <ul>
 * <li>java.util.ArrayList: ListCloneFunction
 * <li>java.util.LinkedList: ListCloneFunction
 * <li>java.util.concurrent.CopyOnWriteArrayList: ListCloneFunction
 * <li>java.util.Vector: ListCloneFunction
 * <li>java.util.Stack: ListCloneFunction
 * <li>java.util.List: ListCloneFunction
 * </ul>
 * <p>
 * <p>Usage:</p>
 * <pre>
 *  // Example 1: using an instantiated, reusable cloner.
 *  Cloner cloner = new Cloner();
 *  Foo fooClone = cloner.clone(foo);
 *  cloner.clearIndex(); // prepare it for reuse
 *  Foo fooClone2 = cloner.clone(foo);
 *
 *  // Example 2: using the utility method that self-instantiates a temporary cloner.
 *  Foo fooClone = Cloner.deepClone(foo);
 *
 *  </pre>
 */
public class Cloner {
    static Logger log = Logger.getLogger(Cloner.class.getName());

    /**
     * Keeps track of the objects that have been cloned so far.
     */
    private IdentityHashMap<Object, Object> index = new IdentityHashMap<>();

    /**
     * Cache the clone methods once for all cloners.
     */
    private static final Map<Class, Method> methodCache = new ConcurrentHashMap<>();
    private Map<Class, CloneFunction> functions = new HashMap<>();

    /**
     * Convenience utility function that creates a new Cloner, uses it to
     * deep clone the object, and then returns the result.
     */
    public static <T> T deepClone(T object) {
        return new Cloner().clone(object);
    }

    /**
     * Internal method to work around a Java generics typing issue by
     * isolating the 'bad' case into a method with suppressed warnings.
     */
    @SuppressWarnings("unchecked")
    private <T> Class<T> objectClass(T object) {
        // This should be 100% allowed without a cast but Java generics
        // is not that smart sometimes.
        // Wrapping it in a method at least isolates the warning suppression
        return (Class<T>) object.getClass();
    }

    /**
     * Deeps clones the specified object, reusing previous clones when possible.
     * <p>
     * <p>Object cloning priority works as follows:</p>
     * <ul>
     * <li>If the object has already been cloned then its clone is returned.
     * <li>If useFunctions is true and there is a custom CloneFunction then it is
     * called to clone the object.
     * <li>If the object implements Cloneable then its clone() method is called, arrays are
     * deep cloned with entries passing through clone().
     * </ul>
     * <p>
     * <p>The abililty to selectively use clone functions is useful when
     * being called from a clone function.</p>
     * <p>
     * Note: objects returned by this method may not have yet had their cloneField()
     * method called.
     */
    public <T> T clone(T object) {
        if (object == null) {
            return null;
        }

        if (log.isLoggable(Level.FINER)) {
            log.finer("cloning:" + object.getClass() + "@" + System.identityHashCode(object));
        }

        Class<T> type = objectClass(object);

        // Check the index to see if we already have it
        Object clone = index.get(object);
        if (clone != null || index.containsKey(object)) {
            if (log.isLoggable(Level.FINER)) {
                log.finer("cloned:" + object.getClass() + "@" + System.identityHashCode(object)
                          + " as cached:" + (clone == null
                                             ? "null"
                                             : (clone.getClass() + "@" + System
                        .identityHashCode(clone))));
            }
            return type.cast(clone);
        }

        // See if there is a custom function... that trumps everything.
        CloneFunction<T> func = getCloneFunction(type);
        if (func != null) {
            T result = func.cloneObject(this, object);

            // Store the object in the identity map so that any circular references are resolved.
            index.put(object, result);

            func.cloneFields(this, result, object);

            if (result == null) {
                System.err.println("cloned " + object.getClass() + "@"
                                   + System.identityHashCode(object) + " as transformed:null");
            }
            return result;
        }

        if (object.getClass().isArray()) {
            // Perform an array clone
            clone = arrayClone(object);

            // Array clone already indexes the clone
        } else if (object instanceof Cloneable) {

            // Perform a regular Java shallow clone
            try {
                clone = javaClone(object);
            } catch (CloneNotSupportedException e) {
                throw new IllegalArgumentException("Object is not cloneable, type:" + type, e);
            }

            // Store the object in the identity map so that any circular references
            // are resolvable
            index.put(object, clone);
        } else {
            throw new IllegalArgumentException("Object is not cloneable, type:" + type);
        }

        if (log.isLoggable(Level.FINER)) {
            log.finer("cloned:" + object.getClass() + "@" + System.identityHashCode(object)
                      + " as " + clone.getClass() + "@" + System.identityHashCode(clone));
        }
        return type.cast(clone);
    }

    /**
     * @return a previously registered clone function for the specified type or null if there is no
     * custom clone function for the type.
     */
    private <T> CloneFunction<T> getCloneFunction(Class<T> type) {
        CloneFunction<T> cloneFunction = (CloneFunction<T>) functions.get(type);
        if (cloneFunction != null) {
            return cloneFunction;
        }

        // Exhaustive search
        for (Map.Entry<Class, CloneFunction> entry : functions.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
                cloneFunction = entry.getValue();
                break;
            }
        }
        if (cloneFunction != null) {
            // Cache polymorphy for later
            functions.put(type, cloneFunction);
        }
        return cloneFunction;
    }

    /**
     * Sets a custom CloneFunction for implementations of the specified Java type.
     */
    public <T> void setCloneFunction(Class<T> type, CloneFunction<T> function) {
        if (function == null) {
            functions.remove(type);
        } else {
            functions.put(type, function);
        }
    }

    /**
     * Forces an object to be added to the indexing cache such that attempts
     * to clone the 'original' will always result in the 'clone' being returned.
     * This can be used to stub out specific values from being cloned or to
     * force global shared instances to be used even if the object is cloneable
     * normally.
     */
    public <T> void setClonedValue(T original, T clone) {
        index.put(original, clone);
    }

    /**
     * Returns true if the specified object has already been cloned
     * by this cloner during this session.  Cloned objects are cached
     * for later use and it's sometimes convenient to know if some
     * objects have already been cloned.
     */
    public boolean isCloned(Object o) {
        return index.containsKey(o);
    }

    /**
     * Clears the object index allowing the cloner to be reused for a brand new
     * cloning operation.
     */
    public void clearIndex() {
        index.clear();
    }

    /**
     * Performs a raw shallow Java clone using reflection.  This call does NOT
     * check against the clone index and so will return new objects every time
     * it is called.  That's because these are shallow clones and have not (and may
     * not ever, depending on the caller) get resolved.
     * <p>
     * <p>This method is provided as a convenient way for CloneFunctions to call
     * clone() and objects without necessarily knowing their real type.</p>
     */
    public <T> T javaClone(T object) throws CloneNotSupportedException {
        if (object == null) {
            return null;
        }
        Method m = methodCache.get(object.getClass());
        if (m == null) {
            try {
                // Lookup the method and cache it
                m = object.getClass().getMethod("clone");
            } catch (NoSuchMethodException e) {
                throw new CloneNotSupportedException(
                        "No public clone method found for:" + object.getClass());
            }
            methodCache.put(object.getClass(), m);

            // Note: yes we might cache the method twice... but so what?
        }

        try {
            Class<? extends T> type = objectClass(object);
            return type.cast(m.invoke(object));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error cloning object of type:" + object.getClass(), e);
        }
    }

    /**
     * Clones a primitive array by coping it and clones an object
     * array by coping it and then running each of its values through
     * Cloner.clone().
     */
    protected <T> T arrayClone(T object) {
        // Java doesn't support the cloning of arrays through reflection unless
        // you open access to Object's protected clone array... which requires
        // elevated privileges. So we will do a work-around that is slightly less elegant.
        // This should be 100% allowed without a case but Java generics
        // is not that smart
        Class<T> type = objectClass(object);
        Class elementType = type.getComponentType();
        int size = Array.getLength(object);
        Object clone = Array.newInstance(elementType, size);

        // Store the clone for later lookups
        index.put(object, clone);

        if (elementType.isPrimitive()) {
            // Then our job is a bit easier
            System.arraycopy(object, 0, clone, 0, size);
        } else {
            // Else it's an object array so we'll clone it and its children
            for (int i = 0; i < size; i++) {
                Object element = clone(Array.get(object, i));
                Array.set(clone, i, element);
            }
        }

        return type.cast(clone);
    }
}
