package mini.system.lwjgl;

import mini.input.KeyInput;
import mini.input.MouseInput;
import mini.input.lwjgl.LwjglKeyInput;
import mini.input.lwjgl.LwjglMouseInput;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.OpenGLException;
import org.lwjgl.opengl.Util;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class LwjglAbstractDisplay extends LwjglContext implements Runnable {

    protected AtomicBoolean needClose = new AtomicBoolean(false);
    protected boolean wasActive = false;
    protected int frameRate = 0;
    protected boolean autoFlush = true;
    protected boolean allowSwapBuffers = false;

    /**
     * Set the title if its a windowed display
     * @param title
     */
    public abstract void setTitle(String title);

    /**
     * Restart if its a windowed or full-screen display.
     */
    public abstract void restart();

    /**
     * Apply the settings, changing resolution, etc.
     */
    protected abstract void createContext() throws LWJGLException;

    /**
     * Destroy the context.
     */
    protected abstract void destroyContext();

    /**
     * Does LWJGL display initialization in the OpenGL thread
     */
    protected boolean initInThread(){
        try {
            // Enable uncaught exception handler only for current thread
            Thread.currentThread().setUncaughtExceptionHandler((thread, thrown) -> {
                listener.handleError("Uncaught exception thrown in "+thread.toString(), thrown);
                if (needClose.get()){
                    // listener.handleError() has requested the
                    // context to close. Satisfy request.
                    deinitInThread();
                }
            });

            // For canvas, this will create a pbuffer,
            // allowing us to query information.
            // When the canvas context becomes available, it will
            // be replaced seamlessly.
            createContext();
            printContextInitInfo();

            created.set(true);
            super.internalCreate();
        } catch (Exception ex){
            try {
                if (Display.isCreated())
                    Display.destroy();
            } catch (Exception ex2){
                System.err.println(ex2.getMessage());
            }

            listener.handleError("Failed to create display", ex);
            return false; // if we failed to create display, do not continue
        }

        listener.initialize();
        return true;
    }

    protected boolean checkGLError(){
        try {
            Util.checkGLError();
        } catch (OpenGLException ex){
            listener.handleError("An OpenGL error has occured!", ex);
        }
        // NOTE: Always return true since this is used in an "assert" statement
        return true;
    }

    /**
     * execute one iteration of the render loop in the OpenGL thread
     */
    protected void runLoop(){
        if (!created.get())
            throw new IllegalStateException();

        listener.update();

        // All this does is call swap buffers
        // If the canvas is not active, there's no need to waste time
        // doing that ..
        if (renderable.get()){
            assert checkGLError();

            // calls swap buffers, etc.
            try {
                if (allowSwapBuffers && autoFlush) {
                    Display.update(false);
                }
            } catch (Throwable ex){
                listener.handleError("Error while swapping buffers", ex);
            }
        }

        int frameRateCap;
        if (autoFlush) {
            frameRateCap = frameRate;
        } else {
            frameRateCap = 20;
        }

        if (frameRateCap > 0) {
            // Cap framerate
            Display.sync(frameRateCap);
        }

        // check mini.input after we synchronize with framerate.
        // this reduces mini.input lag.
        if (renderable.get()){
            Display.processMessages();
        }

        // Subclasses just call GLObjectManager clean up objects here
        // it is safe .. for now.
        renderer.postFrame();
    }

    /**
     * De-initialize in the OpenGL thread.
     */
    protected void deinitInThread(){
        destroyContext();

        listener.destroy();
        System.out.println("Display destroyed.");
        super.internalDestroy();
    }

    public void run(){
        if (listener == null) {
            throw new IllegalStateException("SystemListener is not set on context!"
                    + "Must set with JmeContext.setSystemListner().");
        }

        loadNatives();
        System.out.println("Using LWJGL " + Sys.getVersion());
        if (!initInThread()) {
            System.err.println("Display initialization failed. Cannot continue.");
            return;
        }
        while (true){
            if (renderable.get()){
                if (Display.isCloseRequested())
                    listener.requestClose(false);

                if (wasActive != Display.isActive()) {
                    wasActive = !wasActive;
                }
            }

            runLoop();

            if (needClose.get())
                break;
        }
        deinitInThread();
    }

    public MouseInput getMouseInput() {
        if (mouseInput == null){
            mouseInput = new LwjglMouseInput(this);
        }
        return mouseInput;
    }

    public KeyInput getKeyInput() {
        if (keyInput == null){
            keyInput = new LwjglKeyInput(this);
        }
        return keyInput;
    }

    public void setAutoFlushFrames(boolean enabled){
        this.autoFlush = enabled;
    }

    public void destroy(boolean waitFor){
        needClose.set(true);
        if (waitFor)
            waitFor(false);
    }
}
