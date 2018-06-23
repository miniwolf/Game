package mini.editor.manager;

import mini.editor.annotation.FromAnyThread;
import mini.editor.executor.EditorTaskExecutor;
import mini.editor.executor.impl.BackgroundEditorTaskExecutor;
import mini.editor.executor.impl.EditorThreadExecutor;
import mini.editor.executor.impl.FXEditorTaskExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorManager {
    private static final Runtime RUNTIME = Runtime.getRuntime();

    private static final int PROP_BACKGROUND_TASK_EXECUTORS = RUNTIME.availableProcessors();
    private static ExecutorManager instance;
    private final ScheduledExecutorService scheduledExecutorService;
    private final EditorTaskExecutor fxEditorTaskExecutor;
    private final EditorThreadExecutor editorThreadExecutor;

    /**
     * The list of background tasks executors.
     */
    private final EditorTaskExecutor[] backgroundTaskExecutors;
    private final AtomicInteger nextBackgroundTaskExecutor;

    private ExecutorManager() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        backgroundTaskExecutors = new EditorTaskExecutor[PROP_BACKGROUND_TASK_EXECUTORS];

        for (int i = 0, length = backgroundTaskExecutors.length; i < length; i++) {
            backgroundTaskExecutors[i] = new BackgroundEditorTaskExecutor(i + 1);
        }

        fxEditorTaskExecutor = new FXEditorTaskExecutor();
        editorThreadExecutor = EditorThreadExecutor.getInstance();
        nextBackgroundTaskExecutor = new AtomicInteger();
    }

    public static ExecutorManager getInstance() {
        if (instance == null) {
            instance = new ExecutorManager();
        }

        return instance;
    }

    /**
     * Add the new task to be executed in the JavaFX thread.
     *
     * @param task
     */
    @FromAnyThread
    public void addFXTask(Runnable task) {
        getFXTaskExecutor().execute(task);
    }

    private EditorTaskExecutor getFXTaskExecutor() {
        return fxEditorTaskExecutor;
    }

    /**
     * Add new background task
     *
     * @param task the background task
     */
    @FromAnyThread
    public void addBackgroundTask(final Runnable task) {
        var executors = getBackgroundTaskExecutors();
        var nextTaskExecutor = getNextBackgroundTaskExecutor();

        var index = nextTaskExecutor.incrementAndGet();

        if (index < executors.length) {
            executors[index].execute(task);
        } else {
            nextTaskExecutor.set(0);
            executors[0].execute(task);
        }
    }

    public void addEditorTask(final Runnable task) {
        getEditorTaskExecutor().addToExecute(task);
    }

    /**
     * @return the list of background tasks executors.
     */
    @FromAnyThread
    public EditorTaskExecutor[] getBackgroundTaskExecutors() {
        return backgroundTaskExecutors;
    }

    public AtomicInteger getNextBackgroundTaskExecutor() {
        return nextBackgroundTaskExecutor;
    }

    public EditorThreadExecutor getEditorTaskExecutor() {
        return editorThreadExecutor;
    }
}
