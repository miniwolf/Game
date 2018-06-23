package mini.editor;

import mini.editor.util.LocalObjects;

/**
 * Thread implementation for the editor application.
 */
public class EditorThread extends Thread {
    /**
     * Container of thread local objects.
     */
    private LocalObjects localObjects;

    public EditorThread() {
        localObjects = new LocalObjects();
    }

    public EditorThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
        localObjects = new LocalObjects();
    }

    public LocalObjects getLocal() {
        return localObjects;
    }
}
