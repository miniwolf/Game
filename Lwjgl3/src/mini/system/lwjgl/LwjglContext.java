package mini.system.lwjgl;

import mini.input.lwjgl.GlfwKeyInput;
import mini.input.lwjgl.GlfwMouseInput;
import mini.renderer.Renderer;
import mini.renderer.RendererException;
import mini.renderer.lwjgl.LwjglGL;
import mini.renderer.lwjgl.LwjglGLExt;
import mini.renderer.lwjgl.LwjglGLFboEXT;
import mini.renderer.lwjgl.LwjglGLFboGL3;
import mini.renderer.opengl.GL;
import mini.renderer.opengl.GLDebugDesktop;
import mini.renderer.opengl.GLExt;
import mini.renderer.opengl.GLFbo;
import mini.renderer.opengl.GLRenderer;
import mini.system.ApplicationContext;
import mini.system.ApplicationSettings;
import mini.system.SystemListener;
import mini.system.time.Timer;
import mini.utils.BufferAllocatorFactory;
import mini.utils.lwjgl.LWJGLBufferAllocator;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.EXTFramebufferMultisample;
import org.lwjgl.opengl.GLCapabilities;

import java.util.concurrent.atomic.AtomicBoolean;

import static mini.utils.lwjgl.LWJGLBufferAllocator.PROPERTY_CONCURRENT_BUFFER_ALLOCATOR;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.glGetInteger;

/**
 * A LWJGL implementation of a graphics context.
 */
public abstract class LwjglContext implements ApplicationContext {

    protected static final String THREAD_NAME = "mini Main";

    static {

        final String implementation
                = BufferAllocatorFactory.PROPERTY_BUFFER_ALLOCATOR_IMPLEMENTATION;

        if (System.getProperty(implementation) == null) {
            if (Boolean.parseBoolean(
                    System.getProperty(PROPERTY_CONCURRENT_BUFFER_ALLOCATOR, "true"))) {
                System.setProperty(implementation,
                                   LWJGLBufferAllocator.ConcurrentLWJGLBufferAllocator.class
                                           .getName());
            } else {
                System.setProperty(implementation, LWJGLBufferAllocator.class.getName());
            }
        }
    }

    protected final Object createdLock = new Object();
    protected AtomicBoolean created = new AtomicBoolean(false);
    protected AtomicBoolean renderable = new AtomicBoolean(false);
    protected ApplicationSettings settings = new ApplicationSettings(true);
    protected Renderer renderer;
    protected GlfwKeyInput keyInput;
    protected GlfwMouseInput mouseInput;
    protected Timer timer;
    protected SystemListener listener;

    public void setSystemListener(SystemListener listener) {
        this.listener = listener;
    }

    protected void printContextInitInfo() {
        System.out
                .println("LWJGL " + org.lwjgl.Version.getVersion() + " context running on thread\n"
                         + Thread.currentThread().getName()
                         + " * Graphics Adapter: GLFW " + GLFW.glfwGetVersionString());
    }

    protected int determineMaxSamples() {
        // If we already have a valid context, determine samples using current context.
        if (GLFW.glfwExtensionSupported("GL_ARB_framebuffer_object")) {
            return glGetInteger(ARBFramebufferObject.GL_MAX_SAMPLES);
        } else if (GLFW.glfwExtensionSupported("GL_EXT_framebuffer_multisample")) {
            return glGetInteger(EXTFramebufferMultisample.GL_MAX_SAMPLES_EXT);
        }

        return Integer.MAX_VALUE;
    }

    protected int getNumSamplesToUse() {
        int samples = 0;
        if (settings.getSamples() > 1) {
            samples = settings.getSamples();
            final int supportedSamples = determineMaxSamples();
            if (supportedSamples < samples) {
                System.err.println("Warning: Couldn't satisfy antialiasing samples requirement: x" +
                                   samples + ". Video hardware only supports: x"
                                   + supportedSamples);

                samples = supportedSamples;
            }
        }
        return samples;
    }

    protected void initContextFirstTime() {

        final String renderer = settings.getRenderer();
        final GLCapabilities
                capabilities = createCapabilities(
                !renderer.equals(ApplicationSettings.LWJGL_OPENGL2));

        if (!capabilities.OpenGL20) {
            throw new RendererException("OpenGL 2.0 or higher is required for jMonkeyEngine");
        }

        if (renderer.equals(ApplicationSettings.LWJGL_OPENGL2)
            || renderer.equals(ApplicationSettings.LWJGL_OPENGL3)
            || renderer.equals(ApplicationSettings.LWJGL_OPENGL33)
            || renderer.equals(ApplicationSettings.LWJGL_OPENGL4)
            || renderer.equals(ApplicationSettings.LWJGL_OPENGL41)
            || renderer.equals(ApplicationSettings.LWJGL_OPENGL42)
            || renderer.equals(ApplicationSettings.LWJGL_OPENGL43)
            || renderer.equals(ApplicationSettings.LWJGL_OPENGL44)
            || renderer.equals(ApplicationSettings.LWJGL_OPENGL45)) {

            GL gl = new LwjglGL();
            GLExt glext = new LwjglGLExt();
            GLFbo glfbo;

            if (capabilities.OpenGL30) {
                glfbo = new LwjglGLFboGL3();
            } else {
                glfbo = new LwjglGLFboEXT();
            }

            if (settings.getBoolean("GraphicsDebug")) {
                gl = new GLDebugDesktop(gl, glext, glfbo);
                glext = (GLExt) gl;
                glfbo = (GLFbo) gl;
            }

//            if (settings.getBoolean("GraphicsTiming")) {
//                GLTimingState timingState = new GLTimingState();
//                gl = (GL) GLTiming.createGLTiming(gl, timingState, GL.class, GL2.class, GL3.class, GL4.class);
//                glext = (GLExt) GLTiming.createGLTiming(glext, timingState, GLExt.class);
//                glfbo = (GLFbo) GLTiming.createGLTiming(glfbo, timingState, GLFbo.class);
//            }
//
//            if (settings.getBoolean("GraphicsTrace")) {
//                gl = (GL) GLTracer.createDesktopGlTracer(gl, GL.class, GL2.class, GL3.class, GL4.class);
//                glext = (GLExt) GLTracer.createDesktopGlTracer(glext, GLExt.class);
//                glfbo = (GLFbo) GLTracer.createDesktopGlTracer(glfbo, GLFbo.class);
//            }

            this.renderer = new GLRenderer(gl, glext, glfbo);
            this.renderer.initialize();
        } else {
            throw new UnsupportedOperationException("Unsupported renderer: " + renderer);
        }

        if (capabilities.GL_ARB_debug_output && settings.getBoolean("GraphicsDebug")) {
            ARBDebugOutput.glDebugMessageCallbackARB(new LwjglGLDebugOutputHandler(), 0);
        }

        this.renderer.setMainFrameBufferSrgb(settings.isGammaCorrection());
        this.renderer.setLinearizeSrgbImages(settings.isGammaCorrection());

        // Init input
        if (keyInput != null) {
            keyInput.initialize();
        }

        if (mouseInput != null) {
            mouseInput.initialize();
        }
        renderable.set(true);
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

        initContextFirstTime();
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

    @Override
    public boolean isCreated() {
        return created.get();
    }

    @Override
    public boolean isRenderable() {
        return renderable.get();
    }

    @Override
    public ApplicationSettings getSettings() {
        return settings;
    }

    @Override
    public void setSettings(ApplicationSettings settings) {
        this.settings.copyFrom(settings);
    }

    @Override
    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public Timer getTimer() {
        return timer;
    }
}
