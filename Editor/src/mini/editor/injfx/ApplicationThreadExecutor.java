package mini.editor.injfx;

import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.array.ConcurrentArray;

public class ApplicationThreadExecutor {

    private static final ApplicationThreadExecutor INSTANCE = new ApplicationThreadExecutor();

    private final ConcurrentArray<Runnable> waitingTasks;

    public ApplicationThreadExecutor() {
        waitingTasks = ArrayFactory.newConcurrentAtomicARSWLockArray(Runnable.class);
    }

    public static ApplicationThreadExecutor getInstance() {
        return INSTANCE;
    }

    /**
     * Add the task to execute
     *
     * @param task the new task
     */
    public void addToExecute(Runnable task) {
        ArrayUtils.runInWriteLock(waitingTasks, task, Array::add);
    }
}
