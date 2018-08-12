package mini.editor;

import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.ObjectUtils;
import com.ss.rlib.common.util.Utils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.array.ConcurrentArray;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBoxBase;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.config.Config;
import mini.editor.config.EditorConfig;
import mini.editor.executor.impl.EditorThreadExecutor;
import mini.editor.file.converter.FileConverterRegistry;
import mini.editor.injfx.MiniToJfxIntegrator;
import mini.editor.injfx.processor.FrameTransferSceneProcessor;
import mini.editor.manager.ExecutorManager;
import mini.editor.manager.InitializationManager;
import mini.editor.manager.PluginManager;
import mini.editor.manager.ResourceManager;
import mini.editor.manager.WorkspaceManager;
import mini.editor.ui.builder.EditorFXSceneBuilder;
import mini.editor.ui.component.asset.tree.AssetTreeContextMenuFillerRegistry;
import mini.editor.ui.component.editor.EditorRegistry;
import mini.editor.ui.control.property.builder.PropertyBuilderRegistry;
import mini.editor.ui.control.tree.node.factory.TreeNodeFactoryRegistry;
import mini.editor.ui.css.CssRegistry;
import mini.editor.ui.scene.EditorFXScene;
import mini.editor.util.EditorUtil;
import mini.editor.util.ObjectsUtil;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import static mini.editor.config.DefaultSettingsProvider.Defaults.PREF_DEFAULT_OPEN_GL;
import static mini.editor.config.DefaultSettingsProvider.Preferences.PREF_OPEN_GL;

public class JavaFXApplication extends Application {
    private static JavaFXApplication instance;
    private final ConcurrentArray<Window> openedWindows;
    private volatile EditorFXScene scene;
    private volatile FrameTransferSceneProcessor sceneProcessor;
    private Stage stage;

    public JavaFXApplication() {
        EditorUtil.setJavaFXApplication(this);
        this.openedWindows = ArrayFactory.newConcurrentStampedLockArray(Window.class);
    }

    public static void main(String[] args) {
        var editorConfig = EditorConfig.getInstance();
        var openGLVersion = editorConfig.getEnum(PREF_OPEN_GL, PREF_DEFAULT_OPEN_GL);

        if (System.getProperty("jfx.background.render") == null) {
            System.setProperty("jfx.background.render", openGLVersion.getRender());
        }

        var editor = MiniEditor.prepareToStart();

        InitializationManager.register(WorkspaceManager.class);

        new EditorThread(new ThreadGroup("LWJGL"),
                         () -> startMiniEditor(editor),
                         "LWJGL Render").start();
    }

    private static void startMiniEditor(MiniEditor editor) {
        var initializationManager = InitializationManager.getInstance();
        initializationManager.onBeforeCreateEditorContext();

        editor.start();

        var context = editor.getContext();
        var renderer = context.getRenderer();
        if (renderer == null) {
            // Modify editorConfig to a new OPENGL version, 2.0
        }
    }

    public static JavaFXApplication getInstance() {
        return instance;
    }

    @FromAnyThread
    public static void start() {
        launch();
    }

    @Override
    @FxThread
    public void start(Stage primaryStage) {
        instance = this;
        this.stage = primaryStage;

        addWindow(primaryStage);

        try {
            ArrayFactory.asArray(ComboBoxBase.ON_SHOWN);

            var resourceManager = ResourceManager.getInstance();
            resourceManager.reload();

            var initializationManager = InitializationManager.getInstance();
            initializationManager.onBeforeCreateJavaFXContext();

            var pluginManager = PluginManager.getInstance();
            pluginManager.handlePlugins(
                    editorPlugin -> editorPlugin.register(CssRegistry.getInstance()));

            var config = EditorConfig.getInstance();
            stage.initStyle(StageStyle.DECORATED);
            stage.setMinHeight(600);
            stage.setMinWidth(800);
            stage.setWidth(config.getScreenWidth());
            stage.setHeight(config.getScreenHeight());
            stage.setMaximized(config.isMaximized());
            stage.setTitle(Config.TITLE);
            stage.show();

            if (!stage.isMaximized()) {
                stage.centerOnScreen();
            }

            stage.widthProperty().addListener(((observable, oldValue, newValue) -> {
                if (stage.isMaximized()) {
                    return;
                }
                config.setScreenWidth(newValue.intValue());
            }));
            stage.heightProperty().addListener(((observable, oldValue, newValue) -> {
                if (stage.isMaximized()) {
                    return;
                }
                config.setScreenHeight(newValue.intValue());
            }));

            buildScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @FxThread
    public void stop() throws Exception {
        super.stop();
        onExit();
    }

    @FxThread
    private void onExit() {
        var config = EditorConfig.getInstance();
        config.save();

        var waiter = new CountDownLatch(1);

        var executor = EditorThreadExecutor.getInstance();
        executor.addToExecute(() -> {
            MiniEditor.getInstance().destroy();
            waiter.countDown();
        });

        ExecutorManager.getInstance().shutdown();

        Utils.run(waiter::await);
    }

    private void buildScene() {
        this.scene = EditorFXSceneBuilder.build(stage);

        var initializationManager = InitializationManager.getInstance();
        initializationManager.onAfterCreateJavaFXContext();

        var pluginManager = PluginManager.getInstance();
        pluginManager.handlePlugins(editorPlugin -> {
            editorPlugin.register(EditorRegistry.getInstance());
            editorPlugin.register(FileConverterRegistry.getInstance());
            editorPlugin.register(AssetTreeContextMenuFillerRegistry.getInstance());
            editorPlugin.register(TreeNodeFactoryRegistry.getInstance());
            editorPlugin.register(PropertyBuilderRegistry.getInstance());
        });

        var miniEditor = MiniEditor.getInstance();
        var executor = EditorThreadExecutor.getInstance();
        executor.addToExecute(() -> createSceneProcessor(scene, miniEditor));
    }

    private void createSceneProcessor(EditorFXScene scene, MiniEditor miniEditor) {
        var sceneProcessor = MiniToJfxIntegrator.bind(
                miniEditor,
                scene.getCanvas(),
                miniEditor.getViewPort());
        sceneProcessor.setEnabled(false);
        sceneProcessor.setTransferMode(FrameTransferSceneProcessor.TransferMode.ON_CHANGES);

        this.sceneProcessor = sceneProcessor;

        var stage = getStage();
        stage.focusedProperty().addListener(makeFocusedListener());

        Platform.runLater(scene::notifyFinishBuild);
    }

    /**
     * Add the new opened window
     *
     * @param window the new opened window
     */
    public void addWindow(Window window) {
        window.focusedProperty().addListener(makeFocusedListener());
        ArrayUtils.runInWriteLock(openedWindows, window, Collection::add);
    }

    /**
     * Remove the closed window
     *
     * @param window the closed window
     */
    public void removeWindow(Window window) {
        ArrayUtils.runInWriteLock(openedWindows, window, Array::slowRemove);
    }

    private ChangeListener<Boolean> makeFocusedListener() {
        return (observable, oldValue, newValue) -> {
            var editor = MiniEditor.getInstance();
            var stage = EditorUtil.getFXStage();

            if (newValue || stage.isFocused()) {
                editor.setPaused(false);
                return;
            }

            var editorConfig = EditorConfig.getInstance();
            // TODO: Config.getBoolean( lost focus keep or stop rendering

            var application = JavaFXApplication.getInstance();
            var window = ArrayUtils.getInReadLock(
                    application.openedWindows,
                    windows -> windows.search(Window::isFocused));

            editor.setPaused(window == null);
        };
    }

    /**
     * @return current stage of JavaFX
     */
    @FromAnyThread
    public Stage getStage() {
        return ObjectUtils.notNull(stage);
    }

    public EditorFXScene getScene() {
        return ObjectUtils.notNull(scene, "Scene cannot be null");
    }

    /**
     * @return the last opened window
     */
    public Window getLastWindow() {
        return ObjectsUtil.notNull(ArrayUtils.getInReadLock(openedWindows, Array::last));
    }

    public FrameTransferSceneProcessor getSceneProcessor() {
        return ObjectsUtil.notNull(sceneProcessor, "Scene processor cannot be null");
    }
}
