package mini.system.lwjgl;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ARBMultisample;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.PixelFormat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class LwjglDisplay extends LwjglAbstractDisplay {
    private final AtomicBoolean needRestart = new AtomicBoolean(false);
    private PixelFormat pixelFormat;

    protected DisplayMode getFullscreenDisplayMode(int width, int height, int bpp, int freq) {
        try {
            DisplayMode[] modes = Display.getAvailableDisplayModes();
            for (DisplayMode mode : modes) {
                if (mode.getWidth() == width
                        && mode.getHeight() == height
                        && (mode.getBitsPerPixel() == bpp || (bpp == 24 && mode.getBitsPerPixel() == 32))
                        && (mode.getFrequency() == freq || (freq == 60 && mode.getFrequency() == 59))) {
                    return mode;
                }
            }
        } catch (LWJGLException ex) {
            listener.handleError("Failed to acquire fullscreen display mode!", ex);
        }
        return null;
    }

    protected void createContext() throws LWJGLException {
        DisplayMode displayMode = new DisplayMode(1024, 768);

        //int samples = getNumSamplesToUse();
        PixelFormat pf = new PixelFormat();

        frameRate = 60;
        allowSwapBuffers = true;
        System.out.println("Selected display mode: " + displayMode);

        boolean pixelFormatChanged = false;
        if (created.get() && (pixelFormat.getBitsPerPixel() != pf.getBitsPerPixel()
                || pixelFormat.getAlphaBits() != pf.getAlphaBits()
                || pixelFormat.getDepthBits() != pf.getDepthBits()
                || pixelFormat.getStencilBits() != pf.getStencilBits()
                || pixelFormat.getSamples() != pf.getSamples())) {
            renderer.resetGLObjects();
            Display.destroy();
            pixelFormatChanged = true;
        }
        pixelFormat = pf;

        Display.setTitle("MiniEngine");
        Display.setResizable(true);

        Display.setFullscreen(false);
        Display.setDisplayMode(displayMode);

        if (created.get() && !pixelFormatChanged) {
            Display.releaseContext();
            Display.makeCurrent();
            Display.update();
        }

        if (!created.get() || pixelFormatChanged) {
            ContextAttribs attr = createContextAttribs();
            if (attr != null) {
                Display.create(pixelFormat, attr);
            } else {
                Display.create(pixelFormat);
            }
            renderable.set(true);

            if (pixelFormatChanged && pixelFormat.getSamples() > 1
                    && GLContext.getCapabilities().GL_ARB_multisample) {
                GL11.glEnable(ARBMultisample.GL_MULTISAMPLE_ARB);
            }
        }
    }

    protected void destroyContext() {
        try {
            renderer.cleanup();
            Display.releaseContext();
            Display.destroy();
        } catch (LWJGLException ex) {
            listener.handleError("Failed to destroy context", ex);
        }
    }

    public void create(boolean waitFor) {
        if (created.get()) {
            System.err.println("create() called when display is already created!");
            return;
        }

        new Thread(this, THREAD_NAME).start();
        if (waitFor)
            waitFor(true);
    }

    @Override
    public void runLoop() {
        // This method is overridden to do restart
        if (needRestart.getAndSet(false)) {
            try {
                createContext();
            } catch (LWJGLException ex) {
                System.err.println("Failed to set display settings!" + ex);
            }
            listener.reshape(1024, 768);
            System.out.println("Display restarted.");
        } else if (Display.wasResized()) {
            int newWidth = Display.getWidth();
            int newHeight = Display.getHeight();
            listener.reshape(newWidth, newHeight);
        }

        super.runLoop();
    }

    @Override
    public void restart() {
        if (created.get()) {
            needRestart.set(true);
        } else {
            System.err.println("Display is not created, cannot restart window.");
        }
    }

    public void setTitle(String title) {
        if (created.get())
            Display.setTitle(title);
    }

    private ByteBuffer[] imagesToByteBuffers(Object[] images) {
        ByteBuffer[] out = new ByteBuffer[images.length];
        for (int i = 0; i < images.length; i++) {
            BufferedImage image = (BufferedImage) images[i];
            out[i] = imageToByteBuffer(image);
        }
        return out;
    }

    private ByteBuffer imageToByteBuffer(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_INT_ARGB_PRE) {
            BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g = convertedImage.createGraphics();
            double width = image.getWidth() * (double) 1;
            double height = image.getHeight() * (double) 1;
            g.drawImage(image, (int) ((convertedImage.getWidth() - width) / 2),
                    (int) ((convertedImage.getHeight() - height) / 2),
                    (int) (width), (int) (height), null);
            g.dispose();
            image = convertedImage;
        }

        byte[] imageBuffer = new byte[image.getWidth() * image.getHeight() * 4];
        int counter = 0;
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                int colorSpace = image.getRGB(j, i);
                imageBuffer[counter] = (byte) ((colorSpace << 8) >> 24);
                imageBuffer[counter + 1] = (byte) ((colorSpace << 16) >> 24);
                imageBuffer[counter + 2] = (byte) ((colorSpace << 24) >> 24);
                imageBuffer[counter + 3] = (byte) (colorSpace >> 24);
                counter += 4;
            }
        }
        return ByteBuffer.wrap(imageBuffer);
    }

}
