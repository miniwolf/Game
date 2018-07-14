package mini.editor.manager;

import java.util.ArrayDeque;
import java.util.Deque;

public class InitializationManager {

    private static final InitializationManager instance = new InitializationManager();

    private static final Deque<Class<?>> QUEUE = new ArrayDeque<>();
    private static final String METHOD_NAME = "getInstance";

    public static InitializationManager getInstance() {
        return instance;
    }

    public static void register(Class<?> clazz) {
        try {
            clazz.getMethod(METHOD_NAME);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        QUEUE.add(clazz);
    }

    public static synchronized void valid(final Class<?> clazz) {
        if (QUEUE.getFirst() != clazz) {
            throw new IllegalStateException("The class has invalid initialization position");
        }
    }

    public void onBeforeCreateJavaFXContext() {

    }

    public void onAfterCreateJavaFXContext() {

    }

    public void onBeforeCreateEditorContext() {

    }
}
