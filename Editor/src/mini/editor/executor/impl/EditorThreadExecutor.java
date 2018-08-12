package mini.editor.executor.impl;

import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.array.ConcurrentArray;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.EditorThread;

public class EditorThreadExecutor {
    private static final EditorThreadExecutor INSTANCE = new EditorThreadExecutor();

    private final ConcurrentArray<Runnable> waitTasks;

    private final Array<Runnable> execute;

    public EditorThreadExecutor() {
        waitTasks = ArrayFactory.newConcurrentAtomicARSWLockArray(Runnable.class);
        execute = ArrayFactory.newArray(Runnable.class);
    }

    @FromAnyThread
    public static EditorThreadExecutor getInstance() {
        return INSTANCE;
    }

    public void addToExecute(final Runnable task) {
        ArrayUtils.runInWriteLock(waitTasks, task, (tasks, toAdd) -> tasks.add(task));
    }

    @EditorThread
    private static void execute(final Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EditorThread
    public void execute() {
        if (waitTasks.isEmpty()) {
            return;
        }

        ArrayUtils.runInWriteLock(waitTasks, execute, ArrayUtils::move);

        try {
            execute.forEach(EditorThreadExecutor::execute);
        } finally {
            execute.clear();
        }
    }
}
