package mini.system.lwjgl;

import mini.input.lwjgl.LwjglKeyInput;
import mini.input.lwjgl.LwjglMouseInput;
import mini.renderer.Renderer;
import mini.renderer.opengl.GLRenderer;
import mini.system.ApplicationContext;
import mini.system.NativeLibraryLoader;
import mini.system.SystemListener;
import mini.system.time.Timer;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.ARBDebugOutputCallback;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferMultisample;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A LWJGL implementation of a graphics context.
 */
public abstract class LwjglContext implements ApplicationContext {
    protected static final String THREAD_NAME = "miniEngine main";
    protected AtomicBoolean created = new AtomicBoolean(false);
    protected AtomicBoolean renderable = new AtomicBoolean(false);
    protected final Object createdLock = new Object();

    protected Renderer renderer;
    protected LwjglKeyInput keyInput;
    protected LwjglMouseInput mouseInput;
    protected Timer timer;
    protected SystemListener listener;

    public void setSystemListener(SystemListener listener) {
        this.listener = listener;
    }

    protected void printContextInitInfo() {
        System.out.println("LWJGL " + Sys.getVersion() + " context running on thread "
                + Thread.currentThread().getName() + "\n"
                + " * Graphics Adapter: " + Display.getAdapter() + "\n"
                + " * Driver Version: " + Display.getVersion() + "\n"
                + " * Scaling Factor: " + Display.getPixelScaleFactor());
    }

    protected ContextAttribs createContextAttribs() {
        ContextAttribs attr = new ContextAttribs(4, 5);
        attr = attr.withProfileCore(true).withForwardCompatible(true)
                .withProfileCompatibility(false).withDebug(true);
        return attr;
    }

    protected int determineMaxSamples() {
        try {
            // If we already have a valid context, determine samples using current
            // context.
            if (Display.isCreated() && Display.isCurrent()) {
                if (GLContext.getCapabilities().GL_ARB_framebuffer_object) {
                    return GL11.glGetInteger(ARBFramebufferObject.GL_MAX_SAMPLES);
                } else if (GLContext.getCapabilities().GL_EXT_framebuffer_multisample) {
                    return GL11.glGetInteger(EXTFramebufferMultisample.GL_MAX_SAMPLES_EXT);
                } else {
                    // Unknown.
                    return Integer.MAX_VALUE;
                }
            }
        } catch (LWJGLException ex) {
            listener.handleError("Failed to check if display is current", ex);
        }
        if ((Pbuffer.getCapabilities() & Pbuffer.PBUFFER_SUPPORTED) == 0) {
            // No pbuffer, assume everything is supported.
            return Integer.MAX_VALUE;
        } else {
            Pbuffer pb = null;
            // OpenGL2 method: Create pbuffer and query samples
            // from GL_ARB_framebuffer_object or GL_EXT_framebuffer_multisample.
            try {
                pb = new Pbuffer(1, 1, new PixelFormat(0, 0, 0), null);
                pb.makeCurrent();

                if (GLContext.getCapabilities().GL_ARB_framebuffer_object) {
                    return GL11.glGetInteger(ARBFramebufferObject.GL_MAX_SAMPLES);
                } else if (GLContext.getCapabilities().GL_EXT_framebuffer_multisample) {
                    return GL11.glGetInteger(EXTFramebufferMultisample.GL_MAX_SAMPLES_EXT);
                }

                // OpenGL2 method failed.
                return Integer.MAX_VALUE;
            } catch (LWJGLException ex) {
                // Something else failed.
                return Integer.MAX_VALUE;
            } finally {
                if (pb != null) {
                    pb.destroy();
                }
            }
        }
    }

    protected void loadNatives() {
        NativeLibraryLoader.loadNativeLibrary("lwjgl", true);
    }

    protected int getNumSamplesToUse() {
        return 0; // TODO: Compute this value
    }

    protected void initContextFirstTime() {
        if (!GLContext.getCapabilities().OpenGL20) {
            throw new RuntimeException("OpenGL 2.0 or higher is required for running this engine");
        }

        renderer = new GLRenderer();
        renderer.initialize();

        ARBDebugOutput.glDebugMessageCallbackARB(new ARBDebugOutputCallback(new LwjglGLDebugOutputHandler()));
//        renderer.setMainFrameBufferSrgb(settings.isGammaCorrection());
//        renderer.setLinearizeSrgbImages(settings.isGammaCorrection());

        // Init input
        if (keyInput != null) {
            keyInput.initialize();
        }

        if (mouseInput != null) {
            mouseInput.initialize();
        }
    }

    public void internalDestroy() {
        renderer = null;
        timer = null;
        renderable.set(false);
        synchronized (createdLock) {
            created.set(false);
            createdLock.notifyAll();
        }
    }

    public void internalCreate() {
        synchronized (createdLock) {
            created.set(true);
            createdLock.notifyAll();
        }
        if (renderable.get()) {
            initContextFirstTime();
        }
    }

    public void create() {
        create(false);
    }

    public void destroy() {
        destroy(false);
    }

    protected void waitFor(boolean createdVal) {
        synchronized (createdLock) {
            while (created.get() != createdVal) {
                try {
                    createdLock.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public boolean isCreated() {
        return created.get();
    }

    public boolean isRenderable() {
        return renderable.get();
    }

    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public Timer getTimer() {
        return timer;
    }
}