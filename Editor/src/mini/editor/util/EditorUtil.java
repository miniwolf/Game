package mini.editor.util;

import com.ss.rlib.common.util.ClassUtils;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.stage.Stage;
import javafx.stage.Window;
import mini.app.state.ApplicationStateManager;
import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.editor.JavaFXApplication;
import mini.editor.MiniEditor;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.EditorThread;
import mini.editor.config.EditorConfig;
import mini.editor.extension.scene.SceneLayer;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.model.undo.editor.SceneChangeConsumer;
import mini.editor.ui.component.editor.impl.scene.AbstractSceneEditor3DPart;
import mini.editor.ui.component.editor.impl.scene.AbstractSceneFileEditor;
import mini.editor.ui.component.editor.state.impl.BaseEditorSceneEditorState;
import mini.editor.ui.event.FXEventManager;
import mini.editor.ui.event.RequestedOpenFileEvent;
import mini.editor.ui.scene.EditorFXScene;
import mini.environment.generation.JobProgressAdapter;
import mini.input.InputManager;
import mini.light.LightProbe;
import mini.renderer.Camera;
import mini.renderer.RenderManager;
import mini.scene.Node;
import mini.scene.Spatial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class EditorUtil {
    private static MiniEditor miniEditor;
    private static JavaFXApplication javaFXApplication;

    public static void setMiniEditor(MiniEditor miniEditor) {
        EditorUtil.miniEditor = miniEditor;
    }

    public static Stage getFXStage() {
        return javaFXApplication.getStage();
    }

    public static void setJavaFXApplication(JavaFXApplication javaFXApplication) {
        EditorUtil.javaFXApplication = javaFXApplication;
    }

    @FromAnyThread
    public static AssetManager getAssetManager() {
        return miniEditor.getAssetManager();
    }

    @FromAnyThread
    public static RenderManager getRenderManager() {
        return miniEditor.getRenderManager();
    }

    public static Path getAssetFile(Path file) {
        var editorConfig = EditorConfig.getInstance();
        var currentAsset = editorConfig.getCurrentAsset();
        if (currentAsset == null) {
            return null;
        }

        try {
            return currentAsset.relativize(file);
        } catch (IllegalArgumentException e) {
            System.err.println(
                    "Warning: Can't create asset file of the " + file + " for asset folder "
                    + currentAsset);
            return null;
        }
    }

    /**
     * To asset path string
     *
     * @param path the path
     * @return the valid asset path for the file
     */
    public static String toAssetPath(Path path) {
        if (File.separatorChar == '/') {
            return path.toString();
        }

        return path.toString().replace("\\", "/");
    }

    @FromAnyThread
    public static <T> T deserialize(byte[] bytes) {
        var bin = new ByteArrayInputStream(bytes);

        try (var in = new ExtObjectInputStream(bin)) {
            return ClassUtils.unsafeCast(in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException();
        }
    }

    public static EditorFXScene getFXScene() {
        return javaFXApplication.getScene();
    }

    /**
     * @return the last opened window
     */
    public static Window getFXLastWindow() {
        return javaFXApplication.getLastWindow();
    }

    /**
     * Register the opened new window
     *
     * @param window the new window
     */
    @FromAnyThread
    public static void addFXWindow(Window window) {
        javaFXApplication.addWindow(window);
    }

    /**
     * Delete the closed window
     *
     * @param window the closed window
     */
    public static void removeFXWindow(Window window) {
        javaFXApplication.removeWindow(window);
    }

    /**
     * @return the camera.
     */
    @FromAnyThread
    public static Camera getGlobalCamera() {
        return miniEditor.getCamera();
    }

    /**
     * @return object converted to a byte array
     */
    public static byte[] serialize(Serializable object) {
        var bout = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bout)) {
            out.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bout.toByteArray();
    }

    public static ApplicationStateManager getStateManager() {
        return miniEditor.getStateManager();
    }

    @EditorThread
    public static void updateGlobalLightProbe(JobProgressAdapter<LightProbe> progressAdapter) {
        miniEditor.updateLightProbe(progressAdapter);
    }

    /**
     * @return the formatted float number.
     */
    public static float clipNumber(float value, float mod) {
        return (int) (value * mod) / mod;
    }

    public static List<File> getFiles(Dragboard dragboard) {
        List<File> files = ClassUtils.unsafeCast(dragboard.getContent(DataFormat.FILES));
        return files == null ? Collections.emptyList() : files;
    }

    public static void openInEditor(AssetKey<?> assetKey) {
        if (assetKey == null) {
            return;
        }

        var assetPath = assetKey.getName();
        if (assetPath == null || assetPath.isEmpty()) {
            return;
        }

        var assetFile = Paths.get(assetPath);
        var realFile = ObjectsUtil.notNull(getRealFile(assetFile));
        if (!Files.exists(realFile)) {
            return;
        }

        FXEventManager.getInstance().notify(new RequestedOpenFileEvent(realFile));
    }

    private static Path getRealFile(Path assetFile) {
        var editorConfig = EditorConfig.getInstance();
        var currentAsset = editorConfig.getCurrentAsset();
        if (currentAsset == null) {
            return null;
        }

        return currentAsset.resolve(assetFile);
    }

    public static <E extends Enum<?>> E[] getAvailableValues(E value) {
        var valueClass = value.getClass();
        if (!valueClass.isEnum()) {
            throw new RuntimeException("The class " + valueClass + "isn't an enum");
        }

        var enumConstant = valueClass.getEnumConstants();
        return ClassUtils.unsafeCast(enumConstant);
    }

    public static InputManager getInputManager() {
        return miniEditor.getInputManager();
    }

    public static Node getGlobalRootNode() {
        return miniEditor.getRootNode();
    }

    public static SceneLayer getDefaultLayer(ChangeConsumer consumer) {
        if (!(consumer instanceof SceneChangeConsumer)) {
            return null;
        }

        throw new UnsupportedOperationException();
    }
}
