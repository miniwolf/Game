package mini.editor.injfx.transfer;

import javafx.scene.image.PixelWriter;
import mini.editor.injfx.processor.FrameTransferSceneProcessor;
import mini.renderer.RenderManager;
import mini.renderer.Renderer;
import mini.textures.FrameBuffer;
import mini.textures.Image;
import mini.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractFrameTransfer<T> implements FrameTransfer {
    protected static final int RUNNING_STATE = 1;
    protected static final int WAITING_STATE = 2;
    protected static final int DISPOSING_STATE = 3;
    protected static final int DISPOSED_STATE = 4;

    protected final AtomicInteger frameState;
    protected final AtomicInteger imageState;

    protected final FrameBuffer frameBuffer;
    protected final ByteBuffer frameByteBuffer;
    protected final PixelWriter pixelWriter;

    private final int width;
    private final int height;
    private final T destination;

    public AbstractFrameTransfer(T destination,
                                 FrameTransferSceneProcessor.TransferMode transferMode,
                                 FrameBuffer frameBuffer,
                                 int width,
                                 int height) {
        this.destination = destination;
        this.frameState = new AtomicInteger(WAITING_STATE);
        this.imageState = new AtomicInteger(WAITING_STATE);
        this.width = frameBuffer != null ? frameBuffer.getWidth() : width;
        this.height = frameBuffer != null ? frameBuffer.getHeight() : height;

        if (frameBuffer != null) {
            this.frameBuffer = frameBuffer;
        } else {
            this.frameBuffer = new FrameBuffer(width, height, 1);
            this.frameBuffer.setDepthBuffer(Image.Format.Depth);
            this.frameBuffer.setColorBuffer(Image.Format.RGBA8);
            this.frameBuffer.setSrgb(true);
        }

        frameByteBuffer = BufferUtils.createByteBuffer(getWidth() * getHeight() * 4);
        pixelWriter = getPixelWriter(destination, this.frameBuffer, width, height);
    }

    protected abstract PixelWriter getPixelWriter(T destination, FrameBuffer frameBuffer, int width,
                                                  int height);

    @Override
    public void initFor(Renderer renderer, boolean main) {
        if (main) {
            renderer.setMainFrameBufferOverride(frameBuffer);
        }
    }

    @Override
    public void dispose() {
        while (!frameState.compareAndSet(WAITING_STATE, DISPOSING_STATE)) {
            ;
        }
        while (!imageState.compareAndSet(WAITING_STATE, DISPOSING_STATE)) {
            ;
        }
        disposeImpl();
        frameState.compareAndSet(DISPOSING_STATE, DISPOSED_STATE);
        imageState.compareAndSet(DISPOSING_STATE, DISPOSED_STATE);
    }

    protected void disposeImpl() {
        frameBuffer.dispose();
        BufferUtils.destroyDirectBuffer(frameByteBuffer);
    }

    @Override
    public void copyFrameBufferToImage(RenderManager renderManager) {

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
