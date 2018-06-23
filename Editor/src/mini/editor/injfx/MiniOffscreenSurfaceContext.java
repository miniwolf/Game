package mini.editor.injfx;

import com.ss.rlib.common.util.ObjectUtils;
import mini.editor.input.JavaFXKeyInput;
import mini.editor.input.JavaFXMouseInput;
import mini.editor.util.ObjectsUtil;
import mini.renderer.Renderer;
import mini.system.ApplicationContext;
import mini.system.ApplicationSettings;
import mini.system.ApplicationSystem;
import mini.system.SystemListener;
import mini.system.time.Timer;

public class MiniOffscreenSurfaceContext implements ApplicationContext {
    /**
     * The mouse input
     */
    protected final JavaFXMouseInput mouseInput;
    /**
     * The keyboard input
     */
    protected final JavaFXKeyInput keyInput;
    private final ApplicationContext backgroundContext;
    private ApplicationSettings settings;
    /**
     * The current width.
     */
    private volatile int width;
    /**
     * The current height.
     */
    private volatile int height;

    public MiniOffscreenSurfaceContext() {
        settings = createSettings();
        backgroundContext = createBackgroundContext();
        mouseInput = new JavaFXMouseInput(this);
        keyInput = new JavaFXKeyInput(this);
        height = 1;
        width = 1;
    }

    private ApplicationContext createBackgroundContext() {
        return ApplicationSystem.newContext(settings, Type.OffscreenSurface);
    }

    private ApplicationSettings createSettings() {
        var settings = new ApplicationSettings(true);
        settings.setRenderer(ApplicationSettings.LWJGL_OPENGL3);
        return settings;
    }

    @Override
    public ApplicationSettings getSettings() {
        return settings;
    }

    @Override
    public void setSettings(ApplicationSettings settings) {
        this.settings.copyFrom(settings);
        this.settings.setRenderer(ApplicationSettings.LWJGL_OPENGL3);

        ObjectUtils.notNull(getBackgroundContext())
                   .setSettings(settings);
    }

    @Override
    public boolean isRenderable() {
        return backgroundContext != null && backgroundContext.isRenderable();
    }

    @Override
    public boolean isCreated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void create(boolean waitFor) {
        var render = System.getProperty("jfx.background.render",
                                        ApplicationSettings.LWJGL_OPENGL3);

        var backgroundContext = ObjectsUtil.notNull(getBackgroundContext());
        backgroundContext.getSettings().setRenderer(render);
        backgroundContext.create(waitFor);
    }

    @Override
    public Timer getTimer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Renderer getRenderer() {
        return ObjectsUtil.notNull(getBackgroundContext())
                          .getRenderer();
    }

    @Override
    public JavaFXMouseInput getMouseInput() {
        return mouseInput;
    }

    @Override
    public JavaFXKeyInput getKeyInput() {
        return keyInput;
    }

    @Override
    public void setSystemListener(SystemListener listener) {
        ObjectUtils.notNull(getBackgroundContext())
                   .setSystemListener(listener);
    }

    @Override
    public void destroy(boolean waitFor) {
        throw new UnsupportedOperationException();
    }

    public ApplicationContext getBackgroundContext() {
        return backgroundContext;
    }

    /**
     * @return current width
     */
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return current height
     */
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
