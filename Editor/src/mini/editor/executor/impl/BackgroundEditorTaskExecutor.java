package mini.editor.executor.impl;

import com.ss.rlib.common.util.array.Array;

public class BackgroundEditorTaskExecutor extends AbstractEditorTaskExecutor {

    private static final Runtime RUNTIME = Runtime.getRuntime();

    /**
     * The max count of task to execute at the same time.
     */
    private static final int PROP_MAXIMUM_UPDATE = 500 / RUNTIME.availableProcessors();

    /**
     * The max count of tasks to execute in one iteration.
     */
    private static final int PROP_EXECUTOR_LIMIT = 5;

    public BackgroundEditorTaskExecutor(final int order) {
        setName(BackgroundEditorTaskExecutor.class.getSimpleName() + "_" + order);
        setPriority(NORM_PRIORITY - 2);
        start();
    }

    @Override
    protected void doExecute(Array<Runnable> execute, Array<Runnable> executed) {
        final Runnable[] array = execute.array();
        for (int i = 0, length = Math.min(execute.size(), PROP_MAXIMUM_UPDATE); i < length; ) {
            for (int count = 0; count < PROP_EXECUTOR_LIMIT && i < length; count++, i++) {
                final Runnable task = array[i];
                task.run();

                executed.add(task);
            }
        }
    }
}
