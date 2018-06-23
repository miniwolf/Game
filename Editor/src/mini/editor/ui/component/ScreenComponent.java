package mini.editor.ui.component;

import mini.editor.annotation.FxThread;

public interface ScreenComponent {

    /**
     * Notify about finishing building the result scene
     */
    @FxThread
    default void notifyFinishBuild() {
    }
}
