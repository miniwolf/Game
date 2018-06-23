package mini.editor.ui.component.editor;

import javafx.beans.property.BooleanProperty;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import mini.editor.annotation.FxThread;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface FileEditor {
    /**
     * Open the file
     */
    @FxThread
    void openFile(Path file);

    /**
     * @return file for editing
     */
    @FxThread
    Path getEditFile();

    /**
     * @return the file name of the current opened file.
     */
    @FxThread
    String getFileName();

    /**
     * @return the page for showing the editor
     */
    @FxThread
    Parent getPage();

    /**
     * @return the area to place 3D scene.
     */
    @FxThread
    BorderPane get3DArea();

    /**
     * @return the dirty property of this editor
     */
    @FxThread
    BooleanProperty dirtyProperty();

    /**
     * @return whether the current file was changed
     */
    @FxThread
    boolean isDirty();

    /**
     * @return the description of this editor
     */
    @FxThread
    EditorDescription getDescription();

    /**
     * Save new changes
     *
     * @param callback the callback
     */
    @FxThread
    void save(Consumer<FileEditor> callback);

    /**
     * Check the coords whether they are inside the editing area of this editor.
     *
     * @return if the point is inside the editing area.
     */
    boolean isInside(double sceneX, double sceneY, Class<? extends Event> eventType);
}
