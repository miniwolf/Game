package mini.editor.injfx.processor;

import mini.post.SceneProcessor;

/**
 * Used for frame transfer processor
 */
public interface FrameTransferSceneProcessor extends SceneProcessor {
    void setTransferMode(TransferMode transferMode);

    enum TransferMode {
        ALWAYS,
        ON_CHANGES
    }
}
