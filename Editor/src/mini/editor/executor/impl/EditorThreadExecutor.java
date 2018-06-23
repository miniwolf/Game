package mini.editor.executor.impl;

import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.array.ConcurrentArray;
import mini.editor.annotation.FromAnyThread;

public class EditorThreadExecutor {
    private static final EditorThreadExecutor INSTANCE = new EditorThreadExecutor();

    private final ConcurrentArray<Runnable> waitTasks;

    public EditorThreadExecutor() {
        waitTasks = ArrayFactory.newConcurrentAtomicARSWLockArray(Runnable.class);
    }

    @FromAnyThread
    public static EditorThreadExecutor getInstance() {
        return INSTANCE;
    }

    public void addToExecute(final Runnable task) {
        ArrayUtils.runInWriteLock(waitTasks, task, (tasks, toAdd) -> tasks.add(task));
    }
}
