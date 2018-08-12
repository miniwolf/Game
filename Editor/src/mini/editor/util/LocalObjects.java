package mini.editor.util;

import com.ss.rlib.common.util.CycleBuffer;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.pools.Reusable;
import mini.collision.CollisionResults;
import mini.editor.EditorThread;
import mini.editor.annotation.FromAnyThread;
import mini.math.Ray;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.scene.Spatial;

import java.util.Collection;

import static java.lang.Thread.currentThread;

/**
 * Container for local objects
 */
public class LocalObjects {
    private static final int SIZE = 50;

    private static final ThreadLocal<LocalObjects> THREAD_LOCAL = ThreadLocal
            .withInitial(LocalObjects::new);

    private final CycleBuffer<Array<Spatial>> spatialArrayBuffer;
    private final CycleBuffer<Vector3f> vectorBuffer;
    private final CycleBuffer<Vector2f> vector2fBuffer;
    private final CycleBuffer<ResuableCollisionResults> collisionResultsBuffer;
    private final CycleBuffer<Array<Object>> objectArrayBuffer;
    private final CycleBuffer<Ray> rayBuffer;

    public LocalObjects() {
        spatialArrayBuffer = new CycleBuffer<>(Array.class, SIZE,
                                               () -> ArrayFactory.newArray(Spatial.class),
                                               Collection::clear);
        vectorBuffer = new CycleBuffer<>(Vector3f.class, SIZE, Vector3f::new);
        vector2fBuffer = new CycleBuffer<>(Vector2f.class, SIZE, Vector2f::new);
        rayBuffer = new CycleBuffer<>(Ray.class, SIZE, Ray::new);
        collisionResultsBuffer = new CycleBuffer<>(
                ResuableCollisionResults.class,
                SIZE,
                ResuableCollisionResults::new,
                Reusable::free);
        objectArrayBuffer = new CycleBuffer<>(Array.class, SIZE,
                                              () -> ArrayFactory.newArray(Object.class),
                                              Collection::clear);
    }

    public static LocalObjects get() {
        final Thread currentThread = currentThread();
        if (currentThread instanceof EditorThread) {
            return ((EditorThread) currentThread).getLocal(); // TODO: Maybe?
        }

        return THREAD_LOCAL.get();
    }

    /**
     * @return the next free spatial's array.
     */
    @FromAnyThread
    public Array<Spatial> nextSpatialArray() {
        return spatialArrayBuffer.next();
    }

    /**
     * @return the next free vector.
     */
    @FromAnyThread
    public Vector3f nextVector() {
        return vectorBuffer.next();
    }

    public Array<Object> nextObjectArray() {
        return objectArrayBuffer.next();
    }

    /**
     * @return the next free vector2
     */
    @FromAnyThread
    public Vector2f nextVector(float x, float y) {
        return vector2fBuffer.next().set(x, y);
    }

    /**
     * @return the next free ray
     */
    @FromAnyThread
    public Ray nextRay() {
        return rayBuffer.next();
    }

    /**
     * @return the next free collision results
     */
    @FromAnyThread
    public CollisionResults nextCollisionResults() {
        return collisionResultsBuffer.next();
    }

    /**
     * @param vector the source vector
     * @return the next free vector with values from the source vector.
     */
    public Vector3f nextVector(Vector3f vector) {
        return vectorBuffer.next().set(vector);
    }
}
