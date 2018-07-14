package mini.editor.ui.dialog;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.ui.css.CssClasses;
import mini.editor.ui.css.CssRegistry;
import mini.editor.ui.scene.EditorFXScene;
import mini.editor.util.EditorUtil;
import mini.editor.util.FXUtils;

import java.awt.*;

public abstract class EditorDialog {

    private static final Point DEFAULT_SIZE = new Point(0, 0);
    private static final CssRegistry CSS_REGISTRY = CssRegistry.getInstance();

    private final Stage dialog;
    /**
     * The content container
     */
    private final VBox container;

    /**
     * The last focus owner
     */
    private Node focusOwner;

    public EditorDialog() {
        container = new VBox();
        container.setAlignment(Pos.CENTER);

        Scene scene = new Scene(container);

        var stylesheets = scene.getStylesheets();
        stylesheets.addAll(CSS_REGISTRY.getAvailableCssFiles());

        createControls(container);

        dialog = new Stage();
        dialog.setTitle(getTitleText());
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(isResizable());
        dialog.setScene(scene);

        var fxStage = EditorUtil.getFXStage();
        var icons = dialog.getIcons();
        icons.addAll(fxStage.getIcons());

        configureSize(container);
    }

    private void createControls(VBox root) {
        var container = new VBox();
        container.getStyleClass().addAll(CssClasses.DEF_VBOX, CssClasses.DIALOG_CONTENT_ROOT);
        createContent(container);
        root.getChildren().add(container);

        var actionsContainer = new VBox();
        createActions(actionsContainer);
        actionsContainer.getStyleClass().add(CssClasses.DIALOG_ACTIONS_ROOT);
        root.getStyleClass().add(CssClasses.DIALOG_ROOT);

        root.getChildren().add(actionsContainer);
        root.addEventHandler(KeyEvent.KEY_RELEASED, this::processKey);
    }

    private void processKey(KeyEvent event) {
        event.consume();
        if (event.getCode() == KeyCode.ESCAPE) {
            hide();
        }
    }

    protected abstract void createActions(VBox root);

    protected abstract void createContent(VBox root);

    /**
     * Configure size of the root container
     *
     * @param container the root container
     */
    @FxThread
    protected void configureSize(VBox container) {
        configureSize(container, getSize());
    }

    @FxThread
    private void configureSize(VBox container, Point size) {
        var dialog = getDialog();

        var width = size.x;
        var height = size.y;

        if (width >= 1D) {
            FXUtils.setFixedWidth(container, width);
            dialog.setMinWidth(width);
            dialog.setMaxWidth(width);
        }

        if (height >= 1D) {
            FXUtils.setFixedHeight(container, height);
            dialog.setMinHeight(height);
            dialog.setMaxHeight(height);
        }
    }

    @FxThread
    public void show() {
        show(EditorUtil.getFXLastWindow());
    }

    @FxThread
    public void show(Window owner) {
        var scene = owner.getScene();
        if (scene instanceof EditorFXScene) {
            var editorFXScene = (EditorFXScene) scene;
            var container = editorFXScene.getContainer();
            container.setFocusTraversable(false);
        }

        focusOwner = scene.getFocusOwner();

        dialog.initOwner(owner);
        dialog.show();
        dialog.requestFocus();
        dialog.toFront();
        dialog.setOnCloseRequest(event -> hide());

        EditorUtil.addFXWindow(dialog);

        Platform.runLater(dialog::sizeToScene);
    }

    protected void hide() {
        var window = dialog.getOwner();
        var scene = window.getScene();

        if (scene instanceof EditorFXScene) {
            var editorFXScene = (EditorFXScene) scene;
            var container = editorFXScene.getContainer();
            container.setFocusTraversable(true);
        }

        if (focusOwner != null) {
            focusOwner.requestFocus();
        }

        dialog.hide();

        EditorUtil.removeFXWindow(window);
    }

    /**
     * @return the height property of this dialog
     */
    @FxThread
    protected ReadOnlyDoubleProperty heightProperty() {
        return getContainer().heightProperty();
    }

    /**
     * @return the width property of this dialog
     */
    @FxThread
    protected ReadOnlyDoubleProperty widthProperty() {
        return getContainer().widthProperty();
    }

    /**
     * @return the title of this dialog.
     */
    protected String getTitleText() {
        return "Title";
    }

    /**
     * Sets the dialog title
     *
     * @param title
     */
    public void setTitleText(String title) {
        dialog.setTitle(title);
    }

    /**
     * @return whether this dialog should be resizable
     */
    public boolean isResizable() {
        return true;
    }

    @FromAnyThread
    public Point getSize() {
        return DEFAULT_SIZE;
    }

    /**
     * @return the stage of this dialog
     */
    @FromAnyThread
    protected Stage getDialog() {
        return dialog;
    }

    /**
     * @return the content container
     */
    public VBox getContainer() {
        return container;
    }
}
