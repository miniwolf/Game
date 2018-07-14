package mini.editor.executor.impl;

import com.ss.rlib.common.concurrent.util.ConcurrentUtils;
import com.ss.rlib.common.util.array.Array;
import javafx.application.Platform;
import mini.editor.annotation.FromAnyThread;

public class FXEditorTaskExecutor extends AbstractEditorTaskExecutor {
    private static final int EXECUTE_LIMIT = 300;

    private final Runnable FXTask = () -> doExecute(execute, executed);

    public FXEditorTaskExecutor() {
        setName(FXEditorTaskExecutor.class.getSimpleName());
        setPriority(NORM_PRIORITY);
        try {
            Platform.startup(this::start);
        } catch (IllegalStateException e) {
            start();
        }
    }

    @Override
    protected void doExecute(Array<Runnable> execute, Array<Runnable> executed) {
        var array = execute.array();

        for (int i = 0, length = execute.size(); i < length; ) {
            for (int count = 0; count < EXECUTE_LIMIT && i < length; count++, i++) {
                var task = array[i];
                task.run();

                executed.add(task);
            }
        }

        ConcurrentUtils.notifyAll(FXTask);
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

            executeInFXUIThread();

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
    }

    @FromAnyThread
    private void executeInFXUIThread() {
        synchronized (FXTask) {
            Platform.runLater(FXTask);
            ConcurrentUtils.waitInSynchronize(FXTask);
        }
    }
}
