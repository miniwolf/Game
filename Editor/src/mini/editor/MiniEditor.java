package mini.editor;

import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.MiniThread;
import mini.editor.config.EditorConfig;
import mini.editor.injfx.MiniToJavaFXApplication;
import mini.editor.util.EditorUtil;
import mini.material.TechniqueDef;
import mini.math.ColorRGBA;
import mini.scene.Node;

import java.util.concurrent.locks.StampedLock;

public class MiniEditor extends MiniToJavaFXApplication {
    private static final MiniEditor MINI_EDITOR = new MiniEditor();
    private final StampedLock lock;
    private final Node previewNode;
    private boolean paused;

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

    @MiniThread
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void simpleInitApp() {
        super.simpleInitApp();

        renderManager.setPreferredLightMode(TechniqueDef.LightMode.SinglePass);
        renderManager.setSinglePassLightBatchSize(10);

//        assetManager.unregisterLocator(ClasspathLocator.class, "");
//        assetManager.unregisterLocator(ClasspathLocator.class, "/");
//        assetManager.registerLocator(FolderAssetLocator.class);

        viewPort.setBackgroundColor(new ColorRGBA(50 / 255F, 50 / 255F, 50 / 255F, 1));
        cam.setFrustumPerspective(55, (float) cam.getWidth() / cam.getHeight(), 1f,
                                  Integer.MAX_VALUE);

        guiNode.detachAllChildren();

        flyCam.setDragToRotate(true);
        flyCam.setEnabled(false);

        new EditorThread(new ThreadGroup("JavaFX"), JavaFXApplication::start, "Java Launch")
                .start();
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
}
