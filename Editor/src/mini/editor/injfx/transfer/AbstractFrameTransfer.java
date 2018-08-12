package mini.editor.injfx.transfer;

import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import mini.editor.injfx.processor.FrameTransferSceneProcessor;
import mini.editor.util.JavaFXPlatform;
import mini.renderer.RenderManager;
import mini.renderer.Renderer;
import mini.textures.FrameBuffer;
import mini.textures.Image;
import mini.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
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
    private FrameTransferSceneProcessor.TransferMode transferMode;

    private final byte[] byteBuffer;
    private final byte[] prevImageByteBuffer;
    private final byte[] imageByteBuffer;

    private int frameCount;

    public AbstractFrameTransfer(T destination,
                                 FrameTransferSceneProcessor.TransferMode transferMode,
                                 FrameBuffer frameBuffer,
                                 int width,
                                 int height) {
        this.transferMode = transferMode;
        this.frameState = new AtomicInteger(WAITING_STATE);
        this.imageState = new AtomicInteger(WAITING_STATE);
        this.width = frameBuffer != null ? frameBuffer.getWidth() : width;
        this.height = frameBuffer != null ? frameBuffer.getHeight() : height;
        this.frameCount = 0;

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
        byteBuffer = new byte[getWidth() * getHeight() * 4];
        prevImageByteBuffer = new byte[getWidth() * getHeight() * 4];
        imageByteBuffer = new byte[getWidth() * getHeight() * 4];
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
        while (!frameState.compareAndSet(WAITING_STATE, DISPOSING_STATE)) ;
        while (!imageState.compareAndSet(WAITING_STATE, DISPOSING_STATE)) ;

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
        while (!frameState.compareAndSet(WAITING_STATE, RUNNING_STATE)) {
            if (frameState.get() == DISPOSING_STATE) {
                return;
            }
        }

        // Convert screenshot.
        try {
            frameByteBuffer.clear();

            var renderer = renderManager.getRenderer();
            renderer.readFrameBufferWithFormat(frameBuffer, frameByteBuffer, Image.Format.RGBA8);
        } finally {
            if (!frameState.compareAndSet(RUNNING_STATE, WAITING_STATE)) {
                throw new RuntimeException("unknown problem with the frame state");
            }
        }

        synchronized (byteBuffer) {
            frameByteBuffer.get(byteBuffer);

            if (transferMode == FrameTransferSceneProcessor.TransferMode.ON_CHANGES) {

                final byte[] prevBuffer = getPrevImageByteBuffer();

                if (Arrays.equals(prevBuffer, byteBuffer)) {
                    if (frameCount == 0) return;
                } else {
                    frameCount = 2;
                    System.arraycopy(byteBuffer, 0, prevBuffer, 0, byteBuffer.length);
                }

                frameByteBuffer.position(0);
                frameCount--;
            }
        }

        JavaFXPlatform.runInFXThread(this::writeFrame);
    }

    private void writeFrame() {
        while (!imageState.compareAndSet(WAITING_STATE, RUNNING_STATE)) {
            if (imageState.get() == DISPOSED_STATE) {
                return;
            }
        }

        try {
            var imageByteBuffer = getImageByteBuffer();

            synchronized (byteBuffer) {
                System.arraycopy(byteBuffer, 0, imageByteBuffer, 0, byteBuffer.length);
            }

            for (int i = 0, length = width * height * 4; i < length; i += 4) {
                byte r = imageByteBuffer[i];
                byte g = imageByteBuffer[i + 1];
                byte b = imageByteBuffer[i + 2];
                byte a = imageByteBuffer[i + 3];
                imageByteBuffer[i] = b;
                imageByteBuffer[i + 1] = g;
                imageByteBuffer[i + 2] = r;
                imageByteBuffer[i + 3] = a;
            }

            var pixelFormat = PixelFormat.getByteBgraInstance();

            pixelWriter.setPixels(0, 0, width, height, pixelFormat, imageByteBuffer, 0, width * 4);
        } finally {
            if (!imageState.compareAndSet(RUNNING_STATE, WAITING_STATE)) {
                throw new RuntimeException("unknown problem with the image state");
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * @return the previous image byte buffer
     */
    public byte[] getPrevImageByteBuffer() {
        return prevImageByteBuffer;
    }

    public byte[] getImageByteBuffer() {
        return imageByteBuffer;
    }
}
