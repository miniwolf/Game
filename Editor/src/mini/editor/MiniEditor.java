package mini.editor;

import mini.asset.plugins.ClasspathLocator;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.EditorThread;
import mini.editor.asset.locator.FileSystemAssetLocator;
import mini.editor.asset.locator.FolderAssetLocator;
import mini.editor.config.Config;
import mini.editor.config.EditorConfig;
import mini.editor.executor.impl.EditorThreadExecutor;
import mini.editor.extension.loader.SceneLoader;
import mini.editor.injfx.MiniToJavaFXApplication;
import mini.editor.manager.WorkspaceManager;
import mini.editor.util.EditorUtil;
import mini.environment.generation.JobProgressAdapter;
import mini.light.LightProbe;
import mini.material.TechniqueDef;
import mini.math.ColorRGBA;
import mini.renderer.Camera;
import mini.renderer.ViewPort;
import mini.scene.Node;

import java.util.concurrent.locks.StampedLock;

public class MiniEditor extends MiniToJavaFXApplication {
    private static final MiniEditor MINI_EDITOR = new MiniEditor();
    private final StampedLock lock;
    private final Node previewNode;
    private boolean paused;
    private Camera previewCamera;
    private ViewPort previewViewPort;

    private MiniEditor() {
        EditorUtil.setMiniEditor(this);
        lock = new StampedLock();
        previewNode = new Node("Preview Node");
    }

    public static MiniEditor getInstance() {
        return MINI_EDITOR;
    }

    public static MiniEditor prepareToStart() {
        var config = EditorConfig.getInstance();
        var settings = config.getSettings();

        MINI_EDITOR.setSettings(settings);
        return MINI_EDITOR;
    }

    @EditorThread
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    @EditorThread
    public void update() {
        super.update();
        var stamp = syncLock();

        try {
            var executor = EditorThreadExecutor.getInstance();
            executor.execute();

            if (Config.ENABLE_3D) {
                super.update();
            }
        } finally {
            syncUnlock(stamp);
        }
    }

    /**
     * Lock the render thread for doing actions with game scene.
     *
     * @return lock stamp
     */
    @FromAnyThread
    private long syncLock() {
        return lock.writeLock();
    }

    /**
     * Unlock the render thread
     */
    private void syncUnlock(long stamp) {
        lock.unlockWrite(stamp);
    }

    @Override
    public void simpleInitApp() {
        super.simpleInitApp();

        renderManager.setPreferredLightMode(TechniqueDef.LightMode.SinglePass);
        renderManager.setSinglePassLightBatchSize(10);

        assetManager.unregisterLocator(ClasspathLocator.class, "");
        assetManager.unregisterLocator(ClasspathLocator.class, "/");
        assetManager.registerLocator(FolderAssetLocator.class, "");
        assetManager.registerLocator(FileSystemAssetLocator.class, "");
        assetManager.registerLocator(ClasspathLocator.class, "");
        assetManager.addAssetEventListener(EditorConfig.getInstance());

        viewPort.setBackgroundColor(new ColorRGBA(50 / 255F, 50 / 255F, 50 / 255F, 1));
        cam.setFrustumPerspective(55, (float) cam.getWidth() / cam.getHeight(), 1f,
                                  Integer.MAX_VALUE);

        // create preview view port
        previewCamera = cam.clone();

//        previewViewPort = renderManager.createPostView("Preview viewport", previewCamera);
//        previewViewPort.setClearFlags(true, true, true);
//        previewViewPort.attachScene(previewNode);
//        previewViewPort.setBackgroundColor(viewPort.getBackgroundColor());

        guiNode.detachAllChildren();

        flyCam.setDragToRotate(true);
        flyCam.setEnabled(false);

        //SceneLoader.install(this, postProcessor);

        new mini.editor.EditorThread(new ThreadGroup("JavaFX"), JavaFXApplication::start, "Java Launch")
                .start();
    }

    @Override
    public void destroy() {
        super.destroy();

        var workspaceManager = WorkspaceManager.getInstance();
        workspaceManager.save();

        System.exit(0);
    }

    /**
     * Lock the render thread for other actions.
     */
    @FromAnyThread
    public long asyncLock() {
        return lock.readLock();
    }

    /**
     * Unlock the render thread.
     */
    @FromAnyThread
    public void asyncUnlock(long stamp) {
        lock.unlockRead(stamp);
    }

    @EditorThread
    public void updateLightProbe(JobProgressAdapter<LightProbe> progressAdapter) {
        progressAdapter.done(null);
    }
}
