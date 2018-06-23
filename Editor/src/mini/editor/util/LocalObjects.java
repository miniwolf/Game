package mini.editor.util;

import com.ss.rlib.common.util.CycleBuffer;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.editor.EditorThread;
import mini.editor.annotation.FromAnyThread;
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

    public LocalObjects() {
        spatialArrayBuffer = new CycleBuffer<>(Array.class, SIZE,
                                               () -> ArrayFactory.newArray(Spatial.class),
                                               Collection::clear);
        vectorBuffer = new CycleBuffer<>(Vector3f.class, SIZE, Vector3f::new);
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
}
