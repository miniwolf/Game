package mini.editor.injfx.transfer;

import mini.renderer.RenderManager;
import mini.renderer.Renderer;

/**
 * For transferring content from a Mini frame buffer to somewhere
 */
public interface FrameTransfer {
    void initFor(Renderer renderer, boolean main);

    /**
     * Dispose this transfer
     */
    void dispose();

    /**
     * Copy the content from render to the frameByteBuffer and write this content to JavaFX
     */
    void copyFrameBufferToImage(RenderManager renderManager);

    int getHeight();

    int getWidth();
}
