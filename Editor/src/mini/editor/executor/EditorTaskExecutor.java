package mini.editor.executor;

public interface EditorTaskExecutor {
    public static EditorTaskExecutor getInstance() {
        return null;
    }

    /**
     * Add the new task to the executor.
     * This does not guarantee immediate execution
     */
    void execute(Runnable task);

    void shutdown();
}
