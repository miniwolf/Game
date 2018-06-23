package mini.editor.util;

import javafx.application.Platform;

/**
 * The class with additional utility methods for JavaFX <code>Platform</code>.
 */
public class JavaFXPlatform {
    public static void runInFXThread(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }
}
