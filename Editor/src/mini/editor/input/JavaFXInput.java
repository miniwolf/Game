package mini.editor.input;

import javafx.scene.Node;
import javafx.scene.Scene;
import mini.editor.injfx.ApplicationThreadExecutor;
import mini.editor.injfx.MiniOffscreenSurfaceContext;
import mini.editor.util.ObjectsUtil;
import mini.input.Input;
import mini.input.RawInputListener;

/**
 * The base implementation of the {@link Input} for using in the ImageView.
 */
public abstract class JavaFXInput implements Input {

    protected static final ApplicationThreadExecutor EXECUTOR = ApplicationThreadExecutor
            .getInstance();

    protected final MiniOffscreenSurfaceContext context;
    private boolean initialized;
    private Node node;
    private Scene scene;
    private RawInputListener listener;

    public JavaFXInput(MiniOffscreenSurfaceContext context) {
        this.context = context;
    }

    public RawInputListener getListener() {
        return ObjectsUtil.notNull(listener);
    }

    @Override
    public void initialize() {
        if (isInitialized()) {
            return;
        }

        initializeImplementation();
        initialized = true;
    }

    @Override
    public void update() {
        if (!context.isRenderable()) {
            return;
        }
        updateImpl();
    }

    protected abstract void initializeImplementation();

    protected abstract void updateImpl();

    @Override
    public void destroy() {
        unbind();
    }

    public void bind(Node node) {
        this.node = node;
        this.scene = ObjectsUtil.notNull(node.getScene());
    }

    public void unbind() {
        this.node = null;
        this.scene = null;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void setInputListener(RawInputListener listener) {
        this.listener = listener;
    }

    @Override
    public long getInputTimeNanos() {
        return System.nanoTime();
    }
}
