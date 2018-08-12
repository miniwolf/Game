package mini.editor.injfx;

import com.ss.rlib.common.util.ObjectUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import mini.editor.injfx.processor.FrameTransferSceneProcessor;
import mini.editor.injfx.transfer.FrameTransfer;
import mini.editor.util.JavaFXPlatform;
import mini.editor.util.ObjectsUtil;
import mini.renderer.RenderManager;
import mini.renderer.ViewPort;
import mini.renderer.queue.RenderQueue;
import mini.textures.FrameBuffer;
import mini.textures.Image;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractFrameTransferSceneProcessor<T extends Node>
        implements FrameTransferSceneProcessor {
    /**
     * When we should resize
     */
    private final AtomicInteger reshapeNeeded;
    private RenderManager renderManager;
    private volatile MiniToJavaFXApplication application;
    private boolean enabled;
    /**
     * The source view port
     */
    private ViewPort viewPort;
    /**
     * Flag whether this processor is main
     */
    private boolean main;
    private FrameTransfer frameTransfer;
    private T destination;

    private int askWidth;
    private int askHeight;

    private boolean askFixAspect;
    private TransferMode transferMode;

    protected final ChangeListener<? super Number> widthListener;
    protected final ChangeListener<? super Number> heightListener;
    protected final ChangeListener<? super Boolean> rationListener;

    public AbstractFrameTransferSceneProcessor() {
        transferMode = TransferMode.ALWAYS;
        reshapeNeeded = new AtomicInteger(2);
        askWidth = 1;
        askHeight = 1;
        main = true;

        widthListener = (view, oldValue, newValue) -> notifyChangedWidth(newValue);
        heightListener = (view, oldValue, newValue) -> notifyChangedHeight(newValue);
        rationListener = (view, oldValue, newValue) -> notifyChangedRatio(newValue);
    }

    private void notifyChangedRatio(boolean newValue) {
        notifyComponentResized(getDestinationWidth(), getDestinationHeight(), newValue);
    }

    private void notifyChangedHeight(Number newValue) {
        notifyComponentResized(getDestinationWidth(), newValue.intValue(), isPreserveRatio());
    }

    private void notifyChangedWidth(Number newValue) {
        notifyComponentResized(newValue.intValue(), getDestinationHeight(), isPreserveRatio());
    }

    protected abstract boolean isPreserveRatio();

    public void bind(
            T destination,
            MiniToJavaFXApplication application,
            ViewPort viewPort) {
        bind(destination, application, destination, viewPort, true);
    }

    public void bind(
            T destination,
            MiniToJavaFXApplication application,
            Node inputNode,
            ViewPort viewPort,
            boolean main) {
        if (hasApplication()) {
            throw new IllegalStateException("This process is already bound.");
        }

        setApplication(application);
        setEnabled(true);

        this.main = main;
        this.viewPort = viewPort;
        this.viewPort.addProcessor(this);

        JavaFXPlatform.runInFXThread(() -> bindDestination(application, destination, inputNode));
    }

    /**
     * Bind this processor
     */
    protected void bindDestination(
            MiniToJavaFXApplication application,
            T destination,
            Node inputNode) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("this call is not from the JavaFX thread.");
        }

        if (isMain()) {
            var context = (MiniOffscreenSurfaceContext) application.getContext();
            context.getMouseInput().bind(inputNode);
            context.getKeyInput().bind(inputNode);
        }

        setDestination(destination);
        bindListeners();

        destination.setPickOnBounds(true);

        notifyComponentResized(getDestinationWidth(), getDestinationHeight(), this.isPreserveRatio());
    }

    protected abstract void bindListeners();

    /**
     * Unbind this processor from its current destination.
     */
    public void unbind() {

        if (viewPort != null) {
            viewPort.removeProcessor(this);
            viewPort = null;
        }

        JavaFXPlatform.runInFXThread(this::unbindDestination);
    }

    /**
     * Unbind this processor from destination.
     */
    protected void unbindDestination() {

        if (!Platform.isFxApplicationThread()) {
            throw new RuntimeException("this call is not from JavaFX thread.");
        }

        if (hasApplication() && isMain()) {
            var context = (MiniOffscreenSurfaceContext) getApplication().getContext();
            context.getMouseInput().unbind();
            context.getKeyInput().unbind();
        }

        setApplication(null);

        if (hasDestination()) {
            unbindListeners();
            setDestination(null);
        }
    }

    private void unbindListeners() {
    }

    private void notifyComponentResized(int newWidth,
                                        int newHeight,
                                        boolean fixAspect) {
        newWidth = Math.max(newWidth, 1);
        newHeight = Math.max(newHeight, 1);

        if (askHeight == newHeight && askWidth == newWidth && askFixAspect == fixAspect) {
            return;
        }

        askWidth = newWidth;
        askHeight = newHeight;
        askFixAspect = fixAspect;
        reshapeNeeded.set(2);
    }

    protected abstract int getDestinationHeight();

    protected abstract int getDestinationWidth();

    protected abstract FrameTransfer createFrameTransfer(FrameBuffer frameBuffer, int width,
                                                         int height);

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        this.renderManager = rm;
    }

    @Override
    public boolean isInitialized() {
        return frameTransfer != null;
    }

    public void reshape() {
        reshapeNeeded.set(2);
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
    }

    @Override
    public void preFrame(float tpf) {
    }

    @Override
    public void postQueue(RenderQueue rq) {
    }

    @Override
    public void postFrame(FrameBuffer out) {
        if (!isEnabled()) {
            return;
        }

        var frameTransfer = getFrameTransfer();
        if (frameTransfer != null) {
            frameTransfer.copyFrameBufferToImage(getRenderManager());
        }

        // For the next frame
        if (hasDestination() && reshapeNeeded.get() > 0 && reshapeNeeded.decrementAndGet() >= 0) {

            if (frameTransfer != null) {
                frameTransfer.dispose();
            }

            setFrameTransfer(reshapeInThread(askWidth, askHeight));
        }
    }

    protected FrameTransfer reshapeInThread(int width, int height) {
        reshapeCurrentViewPort(width, height);

        var viewPort = getViewPort();
        var renderManager = getRenderManager();
        FrameBuffer frameBuffer = viewPort.getOutputFrameBuffer();

        var frameTransfer = createFrameTransfer(frameBuffer, width, height);
        frameTransfer.initFor(renderManager.getRenderer(), isMain());

        if (isMain()) {
            var context = (MiniOffscreenSurfaceContext) getApplication().getContext();
            context.setHeight(height);
            context.setWidth(width);
        }

        return frameTransfer;
    }

    protected void reshapeCurrentViewPort(int width, int height) {
        var viewPort = getViewPort();
        var camera = viewPort.getCamera();
        var aspect = (float) camera.getWidth() / camera.getHeight();

        if (isMain()) {
            getRenderManager().notifyReshape(width, height);
            camera.setFrustumPerspective(45, aspect, 1f, 10000);
            return;
        }

        camera.resize(width, height, true);
        camera.setFrustumPerspective(45, aspect, 1f, 10000);

        var processors = viewPort.getProcessors();
        var any = processors.stream()
                            .filter(sceneProcessor -> !(sceneProcessor instanceof FrameTransferSceneProcessor))
                            .findAny();

        if (!any.isPresent()) {
            var frameBuffer = new FrameBuffer(width, height, 1);
            frameBuffer.setDepthBuffer(Image.Format.Depth);
            frameBuffer.setColorBuffer(Image.Format.RGBA8);
            frameBuffer.setSrgb(true);

            viewPort.setOutputFrameBuffer(frameBuffer);
        }

        for (var sceneProcessor: processors) {
            if (!sceneProcessor.isInitialized()) {
                sceneProcessor.initialize(renderManager, viewPort);
            } else {
                sceneProcessor.reshape(viewPort, width, height);
            }
        }
    }

    private boolean hasDestination() {
        return destination != null;
    }

    @Override
    public void cleanup() {
        throw new UnsupportedOperationException();
    }

    private boolean hasApplication() {
        return application != null;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isMain() {
        return main;
    }

    public T getDestination() {
        return destination;
    }

    public void setDestination(T destination) {
        this.destination = destination;
    }

    public TransferMode getTransferMode() {
        return transferMode;
    }

    public void setTransferMode(TransferMode transformMode) {
        this.transferMode = transformMode;
    }

    public FrameTransfer getFrameTransfer() {
        return frameTransfer;
    }

    public void setFrameTransfer(FrameTransfer frameTransfer) {
        this.frameTransfer = frameTransfer;
    }

    public ViewPort getViewPort() {
        return ObjectsUtil.notNull(viewPort);
    }

    public RenderManager getRenderManager() {
        return ObjectsUtil.notNull(renderManager);
    }

    public MiniToJavaFXApplication getApplication() {
        return ObjectUtils.notNull(application);
    }

    public void setApplication(MiniToJavaFXApplication application) {
        this.application = application;
    }
}
