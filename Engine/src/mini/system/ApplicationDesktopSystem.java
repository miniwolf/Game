package mini.system;

import java.awt.*;

/**
 */
public class ApplicationDesktopSystem extends ApplicationSystemDelegate {
    @Override
    public void showErrorDialog(String message) {
        if (!GraphicsEnvironment.isHeadless()) {
            final String msg = message;
            EventQueue.invokeLater(() -> ErrorDialog.showDialog(msg));
        } else {
            System.err.println("[ERROR] " + message);
        }
    }

    private ApplicationContext newContextLwjgl() {
        try {
            Class<? extends ApplicationContext> ctxClazz =
                    (Class<? extends ApplicationContext>) Class.forName("mini.system.lwjgl.LwjglDisplay");
            return ctxClazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            System.err.println("Failed to create context" + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.err.println("CRITICAL ERROR: Context class is missing!\n"
                    + "Make sure context class is on the classpath." + ex);
        }

        return null;
    }

    @Override
    public ApplicationContext newContext() {
        return newContextLwjgl();
    }
}