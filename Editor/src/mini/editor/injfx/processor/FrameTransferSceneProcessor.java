package mini.editor.injfx.processor;

import mini.post.SceneProcessor;

/**
 * Used for frame transfer processor
 */
public interface FrameTransferSceneProcessor extends SceneProcessor {

    enum TransferMode {
        ALWAYS,
        ON_CHANGES
    }

    void setTransferMode(TransferMode transferMode);

    void setEnabled(boolean enabled);
    boolean isEnabled();
}
