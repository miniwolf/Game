package mini.editor.executor.impl;

import com.ss.rlib.common.concurrent.lock.LockFactory;
import com.ss.rlib.common.concurrent.lock.Lockable;
import com.ss.rlib.common.concurrent.util.ConcurrentUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.editor.EditorThread;
import mini.editor.annotation.FromAnyThread;
import mini.editor.executor.EditorTaskExecutor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

public abstract class AbstractEditorTaskExecutor extends EditorThread
        implements EditorTaskExecutor, Lockable {
    protected final AtomicBoolean wait;
    protected final Array<Runnable> waitTasks;
    protected final Array<Runnable> executed;
    protected final Array<Runnable> execute;
    private final Lock lock;
    protected boolean running = true;

    public AbstractEditorTaskExecutor() {
        lock = LockFactory.newAtomicLock();
        waitTasks = createExecuteArray();
        execute = createExecuteArray();
        executed = createExecuteArray();
        wait = new AtomicBoolean(false);
    }

    @FromAnyThread
    private Array<Runnable> createExecuteArray() {
        return ArrayFactory.newArray(Runnable.class);
    }

    @Override
    @FromAnyThread
    public void execute(final Runnable task) {
        lock();

        try {
            waitTasks.add(task);
            if (!wait.get()) {
                return;
            }

            synchronized (wait) {
                if (wait.compareAndSet(true, false)) {
                    ConcurrentUtils.notifyAllInSynchronize(wait);
                }
            }
        } finally {
            unlock();
        }

        if (!wait.get()) {
            return;
        }

        synchronized (wait) {
            if (wait.compareAndSet(true, false)) {
                ConcurrentUtils.notifyAllInSynchronize(wait);
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            executed.clear();
            execute.clear();

            lock();

            try {
                if (waitTasks.isEmpty()) {
                    wait.set(true);
                } else {
                    execute.addAll(waitTasks);
                }
            } finally {
                unlock();
            }

            if (wait.get()) {
                synchronized (wait) {
                    if (wait.get()) {
                        ConcurrentUtils.waitInSynchronize(wait);
                    }
                }
            }

            if (execute.isEmpty()) {
                continue;
            }

            doExecute(execute, executed);

            if (executed.isEmpty()) {
                continue;
            }

            lock();
            try {
                waitTasks.removeAll(executed);
            } finally {
                unlock();
            }
        }
        System.out.println(Thread.currentThread().getName() + " has terminated");
    }

    protected abstract void doExecute(final Array<Runnable> execute,
                                      final Array<Runnable> executed);

    public void shutdown() {
        running = false;
        synchronized (wait) {
            ConcurrentUtils.notifyAllInSynchronize(wait);
        }
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }
}
